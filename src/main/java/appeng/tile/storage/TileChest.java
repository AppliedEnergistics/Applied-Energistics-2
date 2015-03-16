/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.tile.storage;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;

public class TileChest extends AENetworkPowerTile implements IMEChest, IFluidHandler, ITerminalHost, IPriorityHost, IConfigManagerHost, IColorableTile
{

	static private class ChestNoHandler extends Exception
	{

		private static final long serialVersionUID = 7995805326136526631L;

	}

	static final ChestNoHandler NO_HANDLER = new ChestNoHandler();

	static final int[] SIDES = new int[] { 0 };
	static final int[] FRONT = new int[] { 1 };
	static final int[] NO_SLOTS = new int[] { };

	final AppEngInternalInventory inv = new AppEngInternalInventory( this, 2 );
	final BaseActionSource mySrc = new MachineSource( this );
	final IConfigManager config = new ConfigManager( this );

	ItemStack storageType;
	long lastStateChange = 0;
	int priority = 0;
	int state = 0;
	boolean wasActive = false;

	AEColor paintedColor = AEColor.Transparent;

	private void recalculateDisplay()
	{
		int oldState = this.state;

		for (int x = 0; x < this.getCellCount(); x++)
			this.state |= (this.getCellStatus( x ) << (3 * x));

		if ( this.isPowered() )
			this.state |= 0x40;
		else
			this.state &= ~0x40;

		boolean currentActive = this.gridProxy.isActive();
		if ( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			try
			{
				this.gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}

		if ( oldState != this.state )
			this.markForUpdate();
	}

	@Override
	protected void PowerEvent(PowerEventType x)
	{
		if ( x == PowerEventType.REQUEST_POWER )
		{
			try
			{
				this.gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, PowerEventType.REQUEST_POWER ) );
			}
			catch (GridAccessException e)
			{
				// :(
			}
		}
		else
			this.recalculateDisplay();
	}

	@TileEvent(TileEventType.TICK)
	public void Tick_TileChest()
	{
		if ( this.worldObj.isRemote )
			return;

		double idleUsage = this.gridProxy.getIdlePowerUsage();

		try
		{
			if ( !this.gridProxy.getEnergy().isNetworkPowered() )
			{
				double powerUsed = this.extractAEPower( idleUsage, Actionable.MODULATE, PowerMultiplier.CONFIG ); // drain
				if ( powerUsed + 0.1 >= idleUsage != (this.state & 0x40) > 0 )
					this.recalculateDisplay();
			}
		}
		catch (GridAccessException e)
		{
			double powerUsed = this.extractAEPower( this.gridProxy.getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG ); // drain
			if ( powerUsed + 0.1 >= idleUsage != (this.state & 0x40) > 0 )
				this.recalculateDisplay();
		}

		if ( this.inv.getStackInSlot( 0 ) != null )
		{
			this.tryToStoreContents();
		}
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void writeToStream_TileChest(ByteBuf data)
	{
		if ( this.worldObj.getTotalWorldTime() - this.lastStateChange > 8 )
			this.state = 0;
		else
			this.state &= 0x24924924; // just keep the blinks...

		for (int x = 0; x < this.getCellCount(); x++)
			this.state |= (this.getCellStatus( x ) << (3 * x));

		if ( this.isPowered() )
			this.state |= 0x40;
		else
			this.state &= ~0x40;

		data.writeByte( this.state );
		data.writeByte( this.paintedColor.ordinal() );

		ItemStack is = this.inv.getStackInSlot( 1 );

		if ( is == null )
		{
			data.writeInt( 0 );
		}
		else
		{
			data.writeInt( (is.getItemDamage() << Platform.DEF_OFFSET) | Item.getIdFromItem( is.getItem() ) );
		}
	}

	@TileEvent(TileEventType.NETWORK_READ)
	public boolean readFromStream_TileChest(ByteBuf data)
	{
		int oldState = this.state;
		ItemStack oldType = this.storageType;

		this.state = data.readByte();
		AEColor oldPaintedColor = this.paintedColor;
		this.paintedColor = AEColor.values()[data.readByte()];

		int item = data.readInt();

		if ( item == 0 )
			this.storageType = null;
		else
			this.storageType = new ItemStack( Item.getItemById( item & 0xffff ), 1, item >> Platform.DEF_OFFSET );

		this.lastStateChange = this.worldObj.getTotalWorldTime();

		return oldPaintedColor != this.paintedColor || (this.state & 0xDB6DB6DB) != (oldState & 0xDB6DB6DB) || !Platform.isSameItemPrecise( oldType, this.storageType );
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void readFromNBT_TileChest(NBTTagCompound data)
	{
		this.config.readFromNBT( data );
		this.priority = data.getInteger( "priority" );
		if ( data.hasKey( "paintedColor" ) )
			this.paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void writeToNBT_TileChest(NBTTagCompound data)
	{
		this.config.writeToNBT( data );
		data.setInteger( "priority", this.priority );
		data.setByte( "paintedColor", (byte) this.paintedColor.ordinal() );
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		this.recalculateDisplay();
	}

	@MENetworkEventSubscribe
	public void channelRender(MENetworkChannelsChanged c)
	{
		this.recalculateDisplay();
	}

	public TileChest()
	{
		this.internalMaxPower = PowerMultiplier.CONFIG.multiply( 40 );
		this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		this.config.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.config.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.config.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		this.internalPublicPowerStorage = true;
		this.internalPowerFlow = AccessRestriction.WRITE;
	}

	boolean isCached = false;

	private ICellHandler cellHandler;
	private MEMonitorHandler itemCell;
	private MEMonitorHandler fluidCell;

	@Override
	public IMEMonitor getItemInventory()
	{
		return this.itemCell;
	}

	@Override
	public IMEMonitor getFluidInventory()
	{
		return this.fluidCell;
	}

	class ChestNetNotifier<T extends IAEStack<T>> implements IMEMonitorHandlerReceiver<T>
	{

		final StorageChannel chan;

		public ChestNetNotifier(StorageChannel chan)
		{
			this.chan = chan;
		}

		@Override
		public void postChange(IBaseMonitor<T> monitor, Iterable<T> change, BaseActionSource source)
		{
			if ( source == TileChest.this.mySrc || (source instanceof PlayerSource && ((PlayerSource) source).via == TileChest.this) )
			{
				try
				{
					if ( TileChest.this.gridProxy.isActive() )
						TileChest.this.gridProxy.getStorage().postAlterationOfStoredItems( this.chan, change, TileChest.this.mySrc );
				}
				catch (GridAccessException e)
				{
					// :(
				}
			}

			TileChest.this.blinkCell( 0 );
		}

		@Override
		public boolean isValid(Object verificationToken)
		{
			if ( this.chan == StorageChannel.ITEMS )
				return verificationToken == TileChest.this.itemCell;
			if ( this.chan == StorageChannel.FLUIDS )
				return verificationToken == TileChest.this.fluidCell;
			return false;
		}

		@Override
		public void onListUpdate()
		{
			// not used here
		}

	}

	class ChestMonitorHandler<T extends IAEStack> extends MEMonitorHandler<T>
	{

		public ChestMonitorHandler(IMEInventoryHandler<T> t)
		{
			super( t );
		}

		public IMEInventoryHandler<T> getInternalHandler()
		{
			IMEInventoryHandler<T> h = this.getHandler();
			if ( h instanceof MEInventoryHandler )
				return (IMEInventoryHandler<T>) ((MEInventoryHandler) h).getInternal();
			return this.getHandler();
		}

		private boolean securityCheck(EntityPlayer player, SecurityPermissions requiredPermission)
		{
			if ( TileChest.this.getTile() instanceof IActionHost && requiredPermission != null )
			{
				boolean requirePower = false;

				IGridNode gn = ((IActionHost) TileChest.this.getTile()).getActionableNode();
				if ( gn != null )
				{
					IGrid g = gn.getGrid();
					if ( g != null )
					{
						if ( requirePower )
						{
							IEnergyGrid eg = g.getCache( IEnergyGrid.class );
							if ( !eg.isNetworkPowered() )
							{
								return false;
							}
						}

						ISecurityGrid sg = g.getCache( ISecurityGrid.class );
						if ( sg.hasPermission( player, requiredPermission ) )
							return true;
					}
				}

				return false;
			}
			return true;
		}

		@Override
		public T injectItems(T input, Actionable mode, BaseActionSource src)
		{
			if ( src.isPlayer() && !this.securityCheck(((PlayerSource) src).player, SecurityPermissions.INJECT) )
				return input;
			return super.injectItems(input, mode, src);
		}

		@Override
		public T extractItems(T request, Actionable mode, BaseActionSource src)
		{
			if ( src.isPlayer() && !this.securityCheck(((PlayerSource) src).player, SecurityPermissions.EXTRACT) )
				return null;
			return super.extractItems(request, mode, src);
		}
	}

	private <StackType extends IAEStack> MEMonitorHandler<StackType> wrap(IMEInventoryHandler h)
	{
		if ( h == null )
			return null;

		MEInventoryHandler ih = new MEInventoryHandler( h, h.getChannel() );
		ih.setPriority( this.priority );

		MEMonitorHandler<StackType> g = new ChestMonitorHandler<StackType>( ih );
		g.addListener( new ChestNetNotifier( h.getChannel() ), g );

		return g;
	}

	public IMEInventoryHandler getHandler(StorageChannel channel) throws ChestNoHandler
	{
		if ( !this.isCached )
		{
			this.itemCell = null;
			this.fluidCell = null;

			ItemStack is = this.inv.getStackInSlot( 1 );
			if ( is != null )
			{
				this.isCached = true;
				this.cellHandler = AEApi.instance().registries().cell().getHandler( is );
				if ( this.cellHandler != null )
				{
					double power = 1.0;

					IMEInventoryHandler<IAEItemStack> itemCell = this.cellHandler.getCellInventory( is, this, StorageChannel.ITEMS );
					IMEInventoryHandler<IAEFluidStack> fluidCell = this.cellHandler.getCellInventory( is, this, StorageChannel.FLUIDS );

					if ( itemCell != null )
						power += this.cellHandler.cellIdleDrain( is, itemCell );
					else if ( fluidCell != null )
						power += this.cellHandler.cellIdleDrain( is, fluidCell );

					this.gridProxy.setIdlePowerUsage( power );

					this.itemCell = this.wrap( itemCell );
					this.fluidCell = this.wrap( fluidCell );
				}
			}
		}

		switch (channel)
		{
		case FLUIDS:
			if ( this.fluidCell == null )
				throw NO_HANDLER;
			return this.fluidCell;
		case ITEMS:
			if ( this.itemCell == null )
				throw NO_HANDLER;
			return this.itemCell;
		default:
		}

		return null;
	}

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added)
	{
		if ( slot == 1 )
		{
			this.itemCell = null;
			this.fluidCell = null;
			this.isCached = false; // recalculate the storage cell.

			try
			{
				this.gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );

				IStorageGrid gs = this.gridProxy.getStorage();
				Platform.postChanges( gs, removed, added, this.mySrc );
			}
			catch (GridAccessException ignored)
			{

			}

			// update the neighbors
			if ( this.worldObj != null )
			{
				Platform.notifyBlocksOfNeighbors( this.worldObj, this.xCoord, this.yCoord, this.zCoord );
				this.markForUpdate();
			}
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inv.setInventorySlotContents( i, itemstack );
		this.tryToStoreContents();
	}

	private void tryToStoreContents()
	{
		try
		{
			if ( this.getStackInSlot( 0 ) != null )
			{
				IMEInventory<IAEItemStack> cell = this.getHandler( StorageChannel.ITEMS );

				IAEItemStack returns = Platform.poweredInsert( this, cell, AEApi.instance().storage().createItemStack( this.inv.getStackInSlot( 0 ) ), this.mySrc );

				if ( returns == null )
					this.inv.setInventorySlotContents( 0, null );
				else
					this.inv.setInventorySlotContents( 0, returns.getItemStack() );
			}
		}
		catch (ChestNoHandler ignored)
		{
		}
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 1;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		if ( i == 1 )
		{
			if ( AEApi.instance().registries().cell().getCellInventory( itemstack, this, StorageChannel.ITEMS ) != null )
				return true;
			if ( AEApi.instance().registries().cell().getCellInventory( itemstack, this, StorageChannel.FLUIDS ) != null )
				return true;
		}
		else
		{
			try
			{
				IMEInventory<IAEItemStack> cell = this.getHandler( StorageChannel.ITEMS );
				IAEItemStack returns = cell.injectItems( AEApi.instance().storage().createItemStack( this.inv.getStackInSlot( 0 ) ), Actionable.SIMULATE, this.mySrc );
				return returns == null || returns.getStackSize() != itemstack.stackSize;
			}
			catch (ChestNoHandler ignored)
			{
			}
		}
		return false;
	}

	@Override
	public int[] getAccessibleSlotsBySide(ForgeDirection side)
	{
		if ( ForgeDirection.SOUTH == side )
			return FRONT;

		if ( this.isPowered() )
		{
			try
			{
				if ( this.getHandler( StorageChannel.ITEMS ) != null )
					return SIDES;
			}
			catch (ChestNoHandler e)
			{
				// nope!
			}
		}
		return NO_SLOTS;
	}

	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel)
	{
		if ( this.gridProxy.isActive() )
		{
			try
			{
				return Collections.singletonList( this.getHandler( channel ) );
			}
			catch (ChestNoHandler e)
			{
				// :P
			}
		}
		return new ArrayList<IMEInventoryHandler>();
	}

	@Override
	public int getPriority()
	{
		return this.priority;
	}

	@Override
	public int getCellCount()
	{
		return 1;
	}

	@Override
	public void blinkCell(int slot)
	{
		long now = this.worldObj.getTotalWorldTime();
		if ( now - this.lastStateChange > 8 )
			this.state = 0;
		this.lastStateChange = now;

		this.state |= 1 << (slot * 3 + 2);

		this.recalculateDisplay();
	}

	@Override
	public boolean isCellBlinking(int slot)
	{
		long now = this.worldObj.getTotalWorldTime();
		if ( now - this.lastStateChange > 8 )
			return false;

		return ((this.state >> (slot * 3 + 2)) & 0x01) == 0x01;
	}

	@Override
	public int getCellStatus(int slot)
	{
		if ( Platform.isClient() )
			return (this.state >> (slot * 3)) & 3;

		ItemStack cell = this.inv.getStackInSlot( 1 );
		ICellHandler ch = AEApi.instance().registries().cell().getHandler( cell );

		if ( ch != null )
		{
			try
			{
				IMEInventoryHandler handler = this.getHandler( StorageChannel.ITEMS );
				if ( handler instanceof ChestMonitorHandler )
					return ch.getStatusForCell( cell, ((ChestMonitorHandler) handler).getInternalHandler() );
			}
			catch (ChestNoHandler ignored)
			{
			}

			try
			{
				IMEInventoryHandler handler = this.getHandler( StorageChannel.FLUIDS );
				if ( handler instanceof ChestMonitorHandler )
					return ch.getStatusForCell( cell, ((ChestMonitorHandler) handler).getInternalHandler() );
			}
			catch (ChestNoHandler ignored)
			{
			}
		}

		return 0;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		double req = resource.amount / 500.0;
		double available = this.extractAEPower( req, Actionable.SIMULATE, PowerMultiplier.CONFIG );
		if ( available >= req - 0.01 )
		{
			try
			{
				IMEInventoryHandler h = this.getHandler( StorageChannel.FLUIDS );

				this.extractAEPower( req, Actionable.MODULATE, PowerMultiplier.CONFIG );
				IAEStack results = h.injectItems( AEFluidStack.create( resource ), doFill ? Actionable.MODULATE : Actionable.SIMULATE, this.mySrc );

				if ( results == null )
					return resource.amount;

				return resource.amount - (int) results.getStackSize();
			}
			catch (ChestNoHandler ignored)
			{
			}
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		try
		{
			IMEInventoryHandler h = this.getHandler( StorageChannel.FLUIDS );
			return h.canAccept( AEFluidStack.create( new FluidStack( fluid, 1 ) ) );
		}
		catch (ChestNoHandler ignored)
		{
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		try
		{
			IMEInventoryHandler h = this.getHandler( StorageChannel.FLUIDS );
			if ( h.getChannel() == StorageChannel.FLUIDS )
				return new FluidTankInfo[] { new FluidTankInfo( null, 1 ) }; // eh?
		}
		catch (ChestNoHandler ignored)
		{
		}

		return null;
	}

	@Override
	protected double extractAEPower(double amt, Actionable mode)
	{
		double stash = 0.0;

		IEnergyGrid eg;
		try
		{
			eg = this.gridProxy.getEnergy();
			stash = eg.extractAEPower( amt, mode, PowerMultiplier.ONE );
			if ( stash >= amt )
				return stash;
		}
		catch (GridAccessException e)
		{
			// no grid :(
		}

		// local battery!
		return super.extractAEPower( amt - stash, mode ) + stash;
	}

	@Override
	public boolean isPowered()
	{
		if ( Platform.isClient() )
			return (this.state & 0x40) == 0x40;

		boolean gridPowered = this.getAECurrentPower() > 64;

		if ( !gridPowered )
		{
			try
			{
				gridPowered = this.gridProxy.getEnergy().isNetworkPowered();
			}
			catch (GridAccessException ignored)
			{
			}
		}

		return super.getAECurrentPower() > 1 || gridPowered;
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src)
	{
		if ( Platform.canAccess( this.gridProxy, src ) && side != this.getForward() )
			return this;
		return null;
	}

	public ItemStack getStorageType()
	{
		if ( this.isPowered() )
			return this.storageType;
		return null;
	}

	@Override
	public void setPriority(int newValue)
	{
		this.priority = newValue;

		this.itemCell = null;
		this.fluidCell = null;
		this.isCached = false; // recalculate the storage cell.

		try
		{
			this.gridProxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch (GridAccessException e)
		{
			// :P
		}
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.config;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
	{

	}

	public boolean openGui(EntityPlayer p, ICellHandler ch, ItemStack cell, int side)
	{
		try
		{
			IMEInventoryHandler invHandler = this.getHandler( StorageChannel.ITEMS );
			if ( ch != null && invHandler != null )
			{
				ch.openChestGui( p, this, ch, invHandler, cell, StorageChannel.ITEMS );
				return true;
			}

		}
		catch (ChestNoHandler e)
		{
			// :P
		}

		try
		{
			IMEInventoryHandler invHandler = this.getHandler( StorageChannel.FLUIDS );
			if ( ch != null && invHandler != null )
			{
				ch.openChestGui( p, this, ch, invHandler, cell, StorageChannel.FLUIDS );
				return true;
			}
		}
		catch (ChestNoHandler e)
		{
			// :P
		}

		return false;
	}

	@Override
	public AEColor getColor()
	{
		return this.paintedColor;
	}

	@Override
	public boolean recolourBlock(ForgeDirection side, AEColor newPaintedColor, EntityPlayer who)
	{
		if ( this.paintedColor == newPaintedColor )
			return false;

		this.paintedColor = newPaintedColor;
		this.markDirty();
		this.markForUpdate();
		return true;
	}

	@Override
	public void saveChanges(IMEInventory cellInventory)
	{
		this.worldObj.markTileEntityChunkModified( this.xCoord, this.yCoord, this.zCoord, this );
	}
}
