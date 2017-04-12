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


import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.*;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.api.networking.security.*;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.*;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TileChest extends AENetworkPowerTile implements IMEChest, IFluidHandler, ITerminalHost, IPriorityHost, IConfigManagerHost, IColorableTile
{

	private static final ChestNoHandler NO_HANDLER = new ChestNoHandler();
	private static final int[] SIDES = { 0 };
	private static final int[] FRONT = { 1 };
	private static final int[] NO_SLOTS = {};
	private final AppEngInternalInventory inv = new AppEngInternalInventory( this, 2 );
	private final BaseActionSource mySrc = new MachineSource( this );
	private final IConfigManager config = new ConfigManager( this );
	private ItemStack storageType;
	private long lastStateChange = 0;
	private int priority = 0;
	private int state = 0;
	private boolean wasActive = false;
	private AEColor paintedColor = AEColor.Transparent;
	private boolean isCached = false;
	private ICellHandler cellHandler;
	private MEMonitorHandler itemCell;
	private MEMonitorHandler fluidCell;

	public TileChest()
	{
		this.setInternalMaxPower( PowerMultiplier.CONFIG.multiply( 40 ) );
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
		this.config.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.config.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.config.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		this.setInternalPublicPowerStorage( true );
		this.setInternalPowerFlow( AccessRestriction.WRITE );
	}

	@Override
	protected void PowerEvent( final PowerEventType x )
	{
		if( x == PowerEventType.REQUEST_POWER )
		{
			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkPowerStorage( this, PowerEventType.REQUEST_POWER ) );
			}
			catch( final GridAccessException e )
			{
				// :(
			}
		}
		else
		{
			this.recalculateDisplay();
		}
	}

	private void recalculateDisplay()
	{
		final int oldState = this.state;

		for( int x = 0; x < this.getCellCount(); x++ )
		{
			this.state |= ( this.getCellStatus( x ) << ( 3 * x ) );
		}

		if( this.isPowered() )
		{
			this.state |= 0x40;
		}
		else
		{
			this.state &= ~0x40;
		}

		final boolean currentActive = this.getProxy().isActive();
		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}

		if( oldState != this.state )
		{
			this.markForUpdate();
		}
	}

	@Override
	public int getCellCount()
	{
		return 1;
	}

	private IMEInventoryHandler getHandler( final StorageChannel channel ) throws ChestNoHandler
	{
		if( !this.isCached )
		{
			this.itemCell = null;
			this.fluidCell = null;

			final ItemStack is = this.inv.getStackInSlot( 1 );
			if( is != null )
			{
				this.isCached = true;
				this.cellHandler = AEApi.instance().registries().cell().getHandler( is );
				if( this.cellHandler != null )
				{
					double power = 1.0;

					final IMEInventoryHandler<IAEItemStack> itemCell = this.cellHandler.getCellInventory( is, this, StorageChannel.ITEMS );
					final IMEInventoryHandler<IAEFluidStack> fluidCell = this.cellHandler.getCellInventory( is, this, StorageChannel.FLUIDS );

					if( itemCell != null )
					{
						power += this.cellHandler.cellIdleDrain( is, itemCell );
					}
					else if( fluidCell != null )
					{
						power += this.cellHandler.cellIdleDrain( is, fluidCell );
					}

					this.getProxy().setIdlePowerUsage( power );

					this.itemCell = this.wrap( itemCell );
					this.fluidCell = this.wrap( fluidCell );
				}
			}
		}

		switch( channel )
		{
			case FLUIDS:
				if( this.fluidCell == null )
				{
					throw NO_HANDLER;
				}
				return this.fluidCell;
			case ITEMS:
				if( this.itemCell == null )
				{
					throw NO_HANDLER;
				}
				return this.itemCell;
			default:
		}

		return null;
	}

	private <StackType extends IAEStack> MEMonitorHandler<StackType> wrap( final IMEInventoryHandler h )
	{
		if( h == null )
		{
			return null;
		}

		final MEInventoryHandler ih = new MEInventoryHandler( h, h.getChannel() );
		ih.setPriority( this.priority );

		final MEMonitorHandler<StackType> g = new ChestMonitorHandler<StackType>( ih );
		g.addListener( new ChestNetNotifier( h.getChannel() ), g );

		return g;
	}

	@Override
	public int getCellStatus( final int slot )
	{
		if( Platform.isClient() )
		{
			return ( this.state >> ( slot * 3 ) ) & 3;
		}

		final ItemStack cell = this.inv.getStackInSlot( 1 );
		final ICellHandler ch = AEApi.instance().registries().cell().getHandler( cell );

		if( ch != null )
		{
			try
			{
				final IMEInventoryHandler handler = this.getHandler( StorageChannel.ITEMS );
				if( handler instanceof ChestMonitorHandler )
				{
					return ch.getStatusForCell( cell, ( (ChestMonitorHandler) handler ).getInternalHandler() );
				}
			}
			catch( final ChestNoHandler ignored )
			{
			}

			try
			{
				final IMEInventoryHandler handler = this.getHandler( StorageChannel.FLUIDS );
				if( handler instanceof ChestMonitorHandler )
				{
					return ch.getStatusForCell( cell, ( (ChestMonitorHandler) handler ).getInternalHandler() );
				}
			}
			catch( final ChestNoHandler ignored )
			{
			}
		}

		return 0;
	}

	@Override
	public boolean isPowered()
	{
		if( Platform.isClient() )
		{
			return ( this.state & 0x40 ) == 0x40;
		}

		boolean gridPowered = this.getAECurrentPower() > 64;

		if( !gridPowered )
		{
			try
			{
				gridPowered = this.getProxy().getEnergy().isNetworkPowered();
			}
			catch( final GridAccessException ignored )
			{
			}
		}

		return super.getAECurrentPower() > 1 || gridPowered;
	}

	@Override
	public boolean isCellBlinking( final int slot )
	{
		final long now = this.worldObj.getTotalWorldTime();
		if( now - this.lastStateChange > 8 )
		{
			return false;
		}

		return ( ( this.state >> ( slot * 3 + 2 ) ) & 0x01 ) == 0x01;
	}

	@Override
	protected double extractAEPower( final double amt, final Actionable mode )
	{
		double stash = 0.0;

		try
		{
			final IEnergyGrid eg = this.getProxy().getEnergy();
			stash = eg.extractAEPower( amt, mode, PowerMultiplier.ONE );
			if( stash >= amt )
			{
				return stash;
			}
		}
		catch( final GridAccessException e )
		{
			// no grid :(
		}

		// local battery!
		return super.extractAEPower( amt - stash, mode ) + stash;
	}

	@TileEvent( TileEventType.TICK )
	public void Tick_TileChest()
	{
		if( this.worldObj.isRemote )
		{
			return;
		}

		final double idleUsage = this.getProxy().getIdlePowerUsage();

		try
		{
			if( !this.getProxy().getEnergy().isNetworkPowered() )
			{
				final double powerUsed = this.extractAEPower( idleUsage, Actionable.MODULATE, PowerMultiplier.CONFIG ); // drain
				if( powerUsed + 0.1 >= idleUsage != ( this.state & 0x40 ) > 0 )
				{
					this.recalculateDisplay();
				}
			}
		}
		catch( final GridAccessException e )
		{
			final double powerUsed = this.extractAEPower( this.getProxy().getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG ); // drain
			if( powerUsed + 0.1 >= idleUsage != ( this.state & 0x40 ) > 0 )
			{
				this.recalculateDisplay();
			}
		}

		if( this.inv.getStackInSlot( 0 ) != null )
		{
			this.tryToStoreContents();
		}
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileChest( final ByteBuf data )
	{
		if( this.worldObj.getTotalWorldTime() - this.lastStateChange > 8 )
		{
			this.state = 0;
		}
		else
		{
			this.state &= 0x24924924; // just keep the blinks...
		}

		for( int x = 0; x < this.getCellCount(); x++ )
		{
			this.state |= ( this.getCellStatus( x ) << ( 3 * x ) );
		}

		if( this.isPowered() )
		{
			this.state |= 0x40;
		}
		else
		{
			this.state &= ~0x40;
		}

		data.writeByte( this.state );
		data.writeByte( this.paintedColor.ordinal() );

		final ItemStack is = this.inv.getStackInSlot( 1 );

		if( is == null )
		{
			data.writeInt( 0 );
		}
		else
		{
			data.writeInt( ( is.getItemDamage() << Platform.DEF_OFFSET ) | Item.getIdFromItem( is.getItem() ) );
		}
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileChest( final ByteBuf data )
	{
		final int oldState = this.state;
		final ItemStack oldType = this.storageType;

		this.state = data.readByte();
		final AEColor oldPaintedColor = this.paintedColor;
		this.paintedColor = AEColor.values()[data.readByte()];

		final int item = data.readInt();

		if( item == 0 )
		{
			this.storageType = null;
		}
		else
		{
			this.storageType = new ItemStack( Item.getItemById( item & 0xffff ), 1, item >> Platform.DEF_OFFSET );
		}

		this.lastStateChange = this.worldObj.getTotalWorldTime();

		return oldPaintedColor != this.paintedColor || ( this.state & 0xDB6DB6DB ) != ( oldState & 0xDB6DB6DB ) || !Platform.isSameItemPrecise( oldType, this.storageType );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileChest( final NBTTagCompound data )
	{
		this.config.readFromNBT( data );
		this.priority = data.getInteger( "priority" );
		if( data.hasKey( "paintedColor" ) )
		{
			this.paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
		}
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileChest( final NBTTagCompound data )
	{
		this.config.writeToNBT( data );
		data.setInteger( "priority", this.priority );
		data.setByte( "paintedColor", (byte) this.paintedColor.ordinal() );
	}

	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.recalculateDisplay();
	}

	@MENetworkEventSubscribe
	public void channelRender( final MENetworkChannelsChanged c )
	{
		this.recalculateDisplay();
	}

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

	@Override
	public IInventory getInternalInventory()
	{
		return this.inv;
	}

	@Override
	public void setInventorySlotContents( final int i, final ItemStack itemstack )
	{
		this.inv.setInventorySlotContents( i, itemstack );
		this.tryToStoreContents();
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( slot == 1 )
		{
			this.itemCell = null;
			this.fluidCell = null;
			this.isCached = false; // recalculate the storage cell.

			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );

				final IStorageGrid gs = this.getProxy().getStorage();
				Platform.postChanges( gs, removed, added, this.mySrc );
			}
			catch( final GridAccessException ignored )
			{

			}

			// update the neighbors
			if( this.worldObj != null )
			{
				Platform.notifyBlocksOfNeighbors( this.worldObj, this.xCoord, this.yCoord, this.zCoord );
				this.markForUpdate();
			}
		}
	}

	@Override
	public boolean canInsertItem( final int slotIndex, final ItemStack insertingItem, final int side )
	{
		if( slotIndex == 1 )
		{
			if( AEApi.instance().registries().cell().getCellInventory( insertingItem, this, StorageChannel.ITEMS ) != null )
			{
				return true;
			}
			if( AEApi.instance().registries().cell().getCellInventory( insertingItem, this, StorageChannel.FLUIDS ) != null )
			{
				return true;
			}
		}
		else
		{
			try
			{
				final IMEInventory<IAEItemStack> cell = this.getHandler( StorageChannel.ITEMS );
				final IAEItemStack returns = cell.injectItems( AEApi.instance().storage().createItemStack( this.inv.getStackInSlot( 0 ) ), Actionable.SIMULATE, this.mySrc );
				return returns == null || returns.getStackSize() != insertingItem.stackSize;
			}
			catch( final ChestNoHandler ignored )
			{
			}
		}
		return false;
	}

	@Override
	public boolean canExtractItem( final int slotIndex, final ItemStack extractedItem, final int side )
	{
		return slotIndex == 1;
	}

	@Override
	public int[] getAccessibleSlotsBySide( final ForgeDirection side )
	{
		if( ForgeDirection.SOUTH == side )
		{
			return FRONT;
		}

		if( this.isPowered() )
		{
			try
			{
				if( this.getHandler( StorageChannel.ITEMS ) != null )
				{
					return SIDES;
				}
			}
			catch( final ChestNoHandler e )
			{
				// nope!
			}
		}
		return NO_SLOTS;
	}

	private void tryToStoreContents()
	{
		try
		{
			if( this.getStackInSlot( 0 ) != null )
			{
				final IMEInventory<IAEItemStack> cell = this.getHandler( StorageChannel.ITEMS );

				final IAEItemStack returns = Platform.poweredInsert( this, cell, AEApi.instance().storage().createItemStack( this.inv.getStackInSlot( 0 ) ), this.mySrc );

				if( returns == null )
				{
					this.inv.setInventorySlotContents( 0, null );
				}
				else
				{
					this.inv.setInventorySlotContents( 0, returns.getItemStack() );
				}
			}
		}
		catch( final ChestNoHandler ignored )
		{
		}
	}

	@Override
	public List<IMEInventoryHandler> getCellArray( final StorageChannel channel )
	{
		if( this.getProxy().isActive() )
		{
			try
			{
				return Collections.singletonList( this.getHandler( channel ) );
			}
			catch( final ChestNoHandler e )
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
	public void setPriority( final int newValue )
	{
		this.priority = newValue;

		this.itemCell = null;
		this.fluidCell = null;
		this.isCached = false; // recalculate the storage cell.

		try
		{
			this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public void blinkCell( final int slot )
	{
		final long now = this.worldObj.getTotalWorldTime();
		if( now - this.lastStateChange > 8 )
		{
			this.state = 0;
		}
		this.lastStateChange = now;

		this.state |= 1 << ( slot * 3 + 2 );

		this.recalculateDisplay();
	}

	@Override
	public int fill( final ForgeDirection from, final FluidStack resource, final boolean doFill )
	{
		final double req = resource.amount / 500.0;
		final double available = this.extractAEPower( req, Actionable.SIMULATE, PowerMultiplier.CONFIG );
		if( available >= req - 0.01 )
		{
			try
			{
				final IMEInventoryHandler h = this.getHandler( StorageChannel.FLUIDS );

				this.extractAEPower( req, Actionable.MODULATE, PowerMultiplier.CONFIG );
				final IAEStack results = h.injectItems( AEFluidStack.create( resource ), doFill ? Actionable.MODULATE : Actionable.SIMULATE, this.mySrc );

				if( results == null )
				{
					return resource.amount;
				}

				return resource.amount - (int) results.getStackSize();
			}
			catch( final ChestNoHandler ignored )
			{
			}
		}
		return 0;
	}

	@Override
	public FluidStack drain( final ForgeDirection from, final FluidStack resource, final boolean doDrain )
	{
		return null;
	}

	@Override
	public FluidStack drain( final ForgeDirection from, final int maxDrain, final boolean doDrain )
	{
		return null;
	}

	@Override
	public boolean canFill( final ForgeDirection from, final Fluid fluid )
	{
		try
		{
			final IMEInventoryHandler h = this.getHandler( StorageChannel.FLUIDS );
			return h.canAccept( AEFluidStack.create( new FluidStack( fluid, 1 ) ) );
		}
		catch( final ChestNoHandler ignored )
		{
		}
		return false;
	}

	@Override
	public boolean canDrain( final ForgeDirection from, final Fluid fluid )
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo( final ForgeDirection from )
	{
		try
		{
			final IMEInventoryHandler h = this.getHandler( StorageChannel.FLUIDS );
			if( h.getChannel() == StorageChannel.FLUIDS )
			{
				return new FluidTankInfo[] { new FluidTankInfo( null, 1 ) }; // eh?
			}
		}
		catch( final ChestNoHandler ignored )
		{
		}

		return null;
	}

	@Override
	public IStorageMonitorable getMonitorable( final ForgeDirection side, final BaseActionSource src )
	{
		if( Platform.canAccess( this.getProxy(), src ) && side != this.getForward() )
		{
			return this;
		}
		return null;
	}

	public ItemStack getStorageType()
	{
		if( this.isPowered() )
		{
			return this.storageType;
		}
		return null;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.config;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{

	}

	public boolean openGui( final EntityPlayer p, final ICellHandler ch, final ItemStack cell, final int side )
	{
		try
		{
			final IMEInventoryHandler invHandler = this.getHandler( StorageChannel.ITEMS );
			if( ch != null && invHandler != null )
			{
				ch.openChestGui( p, this, ch, invHandler, cell, StorageChannel.ITEMS );
				return true;
			}
		}
		catch( final ChestNoHandler e )
		{
			// :P
		}

		try
		{
			final IMEInventoryHandler invHandler = this.getHandler( StorageChannel.FLUIDS );
			if( ch != null && invHandler != null )
			{
				ch.openChestGui( p, this, ch, invHandler, cell, StorageChannel.FLUIDS );
				return true;
			}
		}
		catch( final ChestNoHandler e )
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
	public boolean recolourBlock( final ForgeDirection side, final AEColor newPaintedColor, final EntityPlayer who )
	{
		if( this.paintedColor == newPaintedColor )
		{
			return false;
		}

		this.paintedColor = newPaintedColor;
		this.markDirty();
		this.markForUpdate();
		return true;
	}

	@Override
	public void saveChanges( final IMEInventory cellInventory )
	{
		this.worldObj.markTileEntityChunkModified( this.xCoord, this.yCoord, this.zCoord, this );
	}

	private static class ChestNoHandler extends Exception
	{
		private static final long serialVersionUID = 7995805326136526631L;
	}


	private class ChestNetNotifier<T extends IAEStack<T>> implements IMEMonitorHandlerReceiver<T>
	{

		private final StorageChannel chan;

		public ChestNetNotifier( final StorageChannel chan )
		{
			this.chan = chan;
		}

		@Override
		public boolean isValid( final Object verificationToken )
		{
			if( this.chan == StorageChannel.ITEMS )
			{
				return verificationToken == TileChest.this.itemCell;
			}
			if( this.chan == StorageChannel.FLUIDS )
			{
				return verificationToken == TileChest.this.fluidCell;
			}
			return false;
		}

		@Override
		public void postChange( final IBaseMonitor<T> monitor, final Iterable<T> change, final BaseActionSource source )
		{
			if( source == TileChest.this.mySrc || ( source instanceof PlayerSource && ( (PlayerSource) source ).via == TileChest.this ) )
			{
				try
				{
					if( TileChest.this.getProxy().isActive() )
					{
						TileChest.this.getProxy().getStorage().postAlterationOfStoredItems( this.chan, change, TileChest.this.mySrc );
					}
				}
				catch( final GridAccessException e )
				{
					// :(
				}
			}

			TileChest.this.blinkCell( 0 );
		}

		@Override
		public void onListUpdate()
		{
			// not used here
		}
	}


	private class ChestMonitorHandler<T extends IAEStack> extends MEMonitorHandler<T>
	{

		public ChestMonitorHandler( final IMEInventoryHandler<T> t )
		{
			super( t );
		}

		private IMEInventoryHandler<T> getInternalHandler()
		{
			final IMEInventoryHandler<T> h = this.getHandler();
			if( h instanceof MEInventoryHandler )
			{
				return (IMEInventoryHandler<T>) ( (MEInventoryHandler) h ).getInternal();
			}
			return this.getHandler();
		}

		@Override
		public T injectItems( final T input, final Actionable mode, final BaseActionSource src )
		{
			if( src.isPlayer() && !this.securityCheck( ( (PlayerSource) src ).player, SecurityPermissions.INJECT ) )
			{
				return input;
			}
			return super.injectItems( input, mode, src );
		}

		private boolean securityCheck( final EntityPlayer player, final SecurityPermissions requiredPermission )
		{
			if( TileChest.this.getTile() instanceof IActionHost && requiredPermission != null )
			{
				final IGridNode gn = ( (IActionHost) TileChest.this.getTile() ).getActionableNode();
				if( gn != null )
				{
					final IGrid g = gn.getGrid();
					if( g != null )
					{
						final boolean requirePower = false;
						if( requirePower )
						{
							final IEnergyGrid eg = g.getCache( IEnergyGrid.class );
							if( !eg.isNetworkPowered() )
							{
								return false;
							}
						}

						final ISecurityGrid sg = g.getCache( ISecurityGrid.class );
						if( sg.hasPermission( player, requiredPermission ) )
						{
							return true;
						}
					}
				}

				return false;
			}
			return true;
		}

		@Override
		public T extractItems( final T request, final Actionable mode, final BaseActionSource src )
		{
			if( src.isPlayer() && !this.securityCheck( ( (PlayerSource) src ).player, SecurityPermissions.EXTRACT ) )
			{
				return null;
			}
			return super.extractItems( request, mode, src );
		}
	}
}
