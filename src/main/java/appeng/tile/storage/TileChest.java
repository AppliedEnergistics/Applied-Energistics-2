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


import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;

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
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellGuiHandler;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.capabilities.Capabilities;
import appeng.core.sync.GuiBridge;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.MEMonitorHandler;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import appeng.util.item.AEItemStack;


public class TileChest extends AENetworkPowerTile implements IMEChest, ITerminalHost, IPriorityHost, IConfigManagerHost, IColorableTile, ITickable
{
	private final AppEngInternalInventory inputInventory = new AppEngInternalInventory( this, 1 );
	private final AppEngInternalInventory cellInventory = new AppEngInternalInventory( this, 1 );
	private final IItemHandler internalInventory = new WrapperChainedItemHandler( this.inputInventory, this.cellInventory );

	private final IActionSource mySrc = new MachineSource( this );
	private final IConfigManager config = new ConfigManager( this );
	private long lastStateChange = 0;
	private int priority = 0;
	private int state = 0;
	private boolean wasActive = false;
	private AEColor paintedColor = AEColor.TRANSPARENT;
	private boolean isCached = false;
	private ChestMonitorHandler cellHandler;
	private Accessor accessor;
	private IFluidHandler fluidHandler;

	public TileChest()
	{
		this.setInternalMaxPower( PowerMultiplier.CONFIG.multiply( 40 ) );
		this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );
		this.config.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.config.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.config.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		this.setInternalPublicPowerStorage( true );
		this.setInternalPowerFlow( AccessRestriction.WRITE );

		this.inputInventory.setFilter( new InputInventoryFilter() );
		this.cellInventory.setFilter( new CellInventoryFilter() );
	}

	public ItemStack getCell()
	{
		return this.cellInventory.getStackInSlot( 0 );
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

	@SuppressWarnings( "unchecked" )
	private void updateHandler()
	{
		if( !this.isCached )
		{
			this.cellHandler = null;
			this.accessor = null;
			this.fluidHandler = null;

			final ItemStack is = this.getCell();
			if( !is.isEmpty() )
			{
				this.isCached = true;
				ICellHandler cellHandler = AEApi.instance().registries().cell().getHandler( is );
				if( cellHandler != null )
				{
					double power = 1.0;

					for( IStorageChannel channel : AEApi.instance().storage().storageChannels() )
					{
						final ICellInventoryHandler<IAEItemStack> newCell = cellHandler.getCellInventory( is, this, channel );
						if( newCell != null )
						{
							power += cellHandler.cellIdleDrain( is, newCell );
							this.cellHandler = this.wrap( newCell );
							break;
						}
					}

					this.getProxy().setIdlePowerUsage( power );
					this.accessor = new Accessor();

					if( this.cellHandler != null && this.cellHandler
							.getChannel() == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
					{
						this.fluidHandler = new FluidHandler();
					}
				}
			}
		}
	}

	private <T extends IAEStack<T>> ChestMonitorHandler<T> wrap( final IMEInventoryHandler<T> h )
	{
		if( h == null )
		{
			return null;
		}

		final MEInventoryHandler<T> ih = new MEInventoryHandler<T>( h, h.getChannel() );
		ih.setPriority( this.priority );

		final ChestMonitorHandler<T> g = new ChestMonitorHandler<T>( ih );
		g.addListener( new ChestNetNotifier<T>( h.getChannel() ), g );

		return g;
	}

	@Override
	public int getCellStatus( final int slot )
	{
		if( Platform.isClient() )
		{
			return ( this.state >> ( slot * 3 ) ) & 3;
		}

		this.updateHandler();

		final ItemStack cell = this.getCell();
		final ICellHandler ch = AEApi.instance().registries().cell().getHandler( cell );

		if( this.cellHandler != null && ch != null )
		{
			return ch.getStatusForCell( cell, this.cellHandler.getInternalHandler() );
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
		final long now = this.world.getTotalWorldTime();
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

	@Override
	public void update()
	{
		if( this.world.isRemote )
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

		if( !ItemHandlerUtil.isEmpty( this.inputInventory ) )
		{
			this.tryToStoreContents();
		}
	}

	@Override
	protected void writeToStream( final ByteBuf data ) throws IOException
	{
		super.writeToStream( data );

		if( this.world.getTotalWorldTime() - this.lastStateChange > 8 )
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
	}

	@Override
	protected boolean readFromStream( final ByteBuf data ) throws IOException
	{
		final boolean c = super.readFromStream( data );

		final int oldState = this.state;

		this.state = data.readByte();
		final AEColor oldPaintedColor = this.paintedColor;
		this.paintedColor = AEColor.values()[data.readByte()];

		this.lastStateChange = this.world.getTotalWorldTime();

		return oldPaintedColor != this.paintedColor || ( this.state & 0xDB6DB6DB ) != ( oldState & 0xDB6DB6DB ) || c;
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.config.readFromNBT( data );
		this.priority = data.getInteger( "priority" );
		if( data.hasKey( "paintedColor" ) )
		{
			this.paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
		}
	}

	@Override
	public NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.config.writeToNBT( data );
		data.setInteger( "priority", this.priority );
		data.setByte( "paintedColor", (byte) this.paintedColor.ordinal() );
		return data;
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

	@SuppressWarnings( "unchecked" )
	@Override
	public <T extends IAEStack<T>> IMEMonitor<T> getInventory( IStorageChannel<T> channel )
	{
		this.updateHandler();

		if( this.cellHandler != null && this.cellHandler.getChannel() == channel )
		{
			return this.cellHandler;
		}
		return null;
	}

	@Override
	public IItemHandler getInternalInventory()
	{
		return this.internalInventory;
	}

	@Override
	public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added )
	{
		if( inv == this.cellInventory )
		{
			this.cellHandler = null;
			this.isCached = false; // recalculate the storage cell.

			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
				if( this.getProxy().isActive() )
				{
					final IStorageGrid gs = this.getProxy().getStorage();
					Platform.postChanges( gs, removed, added, this.mySrc );
				}
			}
			catch( final GridAccessException ignored )
			{
			}

			// update the neighbors
			if( this.world != null )
			{
				Platform.notifyBlocksOfNeighbors( this.world, this.pos );
				this.markForUpdate();
			}
		}
		if( inv == this.inputInventory && mc == InvOperation.INSERT )
		{
			this.tryToStoreContents();
		}
	}

	@Override
	protected IItemHandler getItemHandlerForSide( @Nonnull EnumFacing side )
	{
		if( side == this.getForward() )
		{
			return this.cellInventory;
		}
		else
		{
			return this.inputInventory;
		}
	}

	private void tryToStoreContents()
	{
		if( !ItemHandlerUtil.isEmpty( this.inputInventory ) )
		{
			this.updateHandler();

			if( this.cellHandler != null && this.cellHandler.getChannel() == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) )
			{
				final IAEItemStack returns = Platform.poweredInsert( this, this.cellHandler,
						AEItemStack.fromItemStack( this.inputInventory.getStackInSlot( 0 ) ), this.mySrc );

				if( returns == null )
				{
					this.inputInventory.setStackInSlot( 0, ItemStack.EMPTY );
				}
				else
				{
					this.inputInventory.setStackInSlot( 0, returns.createItemStack() );
				}
			}
		}
	}

	@Override
	public List<IMEInventoryHandler> getCellArray( final IStorageChannel channel )
	{
		this.updateHandler();
		if( this.cellHandler != null && this.cellHandler.getChannel() == channel )
		{
			return Collections.singletonList( this.cellHandler );
		}
		return Collections.emptyList();
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
		this.cellHandler = null;
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
		final long now = this.world.getTotalWorldTime();
		if( now - this.lastStateChange > 8 )
		{
			this.state = 0;
		}
		this.lastStateChange = now;

		this.state |= 1 << ( slot * 3 + 2 );

		this.recalculateDisplay();
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

	public boolean openGui( final EntityPlayer p )
	{
		this.updateHandler();
		if( this.cellHandler != null )
		{
			final ICellHandler ch = AEApi.instance().registries().cell().getHandler( this.getCell() );

			if( ch != null )
			{
				final ICellGuiHandler chg = AEApi.instance().registries().cell().getGuiHandler( this.cellHandler.getChannel(), this.getCell() );
				if( chg != null )
				{
					chg.openChestGui( p, this, ch, this.cellHandler, this.getCell(), this.cellHandler.getChannel() );
					return true;
				}
			}

		}

		return false;
	}

	@Override
	public AEColor getColor()
	{
		return this.paintedColor;
	}

	@Override
	public boolean recolourBlock( final EnumFacing side, final AEColor newPaintedColor, final EntityPlayer who )
	{
		if( this.paintedColor == newPaintedColor )
		{
			return false;
		}

		this.paintedColor = newPaintedColor;
		this.saveChanges();
		this.markForUpdate();
		return true;
	}

	@Override
	public void saveChanges( final ICellInventory<?> cellInventory )
	{
		if( cellInventory != null )
		{
			cellInventory.persist();
		}
		this.world.markChunkDirty( this.pos, this );
	}

	private class ChestNetNotifier<T extends IAEStack<T>> implements IMEMonitorHandlerReceiver<T>
	{

		private final IStorageChannel<T> chan;

		public ChestNetNotifier( final IStorageChannel<T> chan )
		{
			this.chan = chan;
		}

		@Override
		public boolean isValid( final Object verificationToken )
		{
			TileChest.this.updateHandler();
			if( TileChest.this.cellHandler != null && this.chan == TileChest.this.cellHandler.getChannel() )
			{
				return verificationToken == TileChest.this.cellHandler;
			}
			return false;
		}

		@Override
		public void postChange( final IBaseMonitor<T> monitor, final Iterable<T> change, final IActionSource source )
		{
			try
			{
				if( TileChest.this.getProxy().isActive() )
				{
					TileChest.this.getProxy().getStorage().postAlterationOfStoredItems( this.chan, change, TileChest.this.mySrc );
				}
			} catch ( final GridAccessException e )
			{
				// :(
			}
			TileChest.this.blinkCell( 0 );
		}

		@Override
		public void onListUpdate()
		{
			// not used here
		}
	}

	private class ChestMonitorHandler<T extends IAEStack<T>> extends MEMonitorHandler<T>
	{

		public ChestMonitorHandler( final IMEInventoryHandler<T> t )
		{
			super( t );
		}

		private ICellInventoryHandler<T> getInternalHandler()
		{
			final IMEInventoryHandler<T> h = this.getHandler();
			if( h instanceof MEInventoryHandler )
			{
				return (ICellInventoryHandler<T>) ( (MEInventoryHandler<T>) h ).getInternal();
			}
			return (ICellInventoryHandler<T>) this.getHandler();
		}

		@Override
		public T injectItems( final T input, final Actionable mode, final IActionSource src )
		{
			if( src.player().map( player -> !this.securityCheck( player, SecurityPermissions.INJECT ) ).orElse( false ) )
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
		public T extractItems( final T request, final Actionable mode, final IActionSource src )
		{
			if( src.player().map( player -> !this.securityCheck( player, SecurityPermissions.EXTRACT ) ).orElse( false ) )
			{
				return null;
			}
			return super.extractItems( request, mode, src );
		}
	}

	@Override
	public boolean hasCapability( Capability<?> capability, EnumFacing facing )
	{
		this.updateHandler();
		if( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.fluidHandler != null && facing != this.getForward() )
		{
			return true;
		}
		if( capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR && this.accessor != null && facing != this.getForward() )
		{
			return true;
		}
		return super.hasCapability( capability, facing );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T> T getCapability( Capability<T> capability, @Nullable EnumFacing facing )
	{
		this.updateHandler();
		if( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.fluidHandler != null && facing != this.getForward() )
		{
			return (T) this.fluidHandler;
		}
		if( capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR && this.accessor != null && facing != this.getForward() )
		{
			return (T) this.accessor;
		}
		return super.getCapability( capability, facing );
	}

	private class Accessor implements IStorageMonitorableAccessor
	{
		@Nullable
		@Override
		public IStorageMonitorable getInventory( IActionSource src )
		{
			if( Platform.canAccess( TileChest.this.getProxy(), src ) )
			{
				return TileChest.this;
			}
			return null;
		}
	}

	private class FluidHandler implements IFluidHandler
	{
		private final IFluidTankProperties[] TANK_PROPS = new IFluidTankProperties[] { new FluidTankProperties( null, Fluid.BUCKET_VOLUME ) };

		@Override
		public int fill( final FluidStack resource, final boolean doFill )
		{
			TileChest.this.updateHandler();
			if( TileChest.this.cellHandler != null && TileChest.this.cellHandler
					.getChannel() == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
			{
				final IAEFluidStack results = Platform.poweredInsert( TileChest.this, TileChest.this.cellHandler, AEFluidStack.fromFluidStack( resource ),
						TileChest.this.mySrc, doFill ? Actionable.MODULATE : Actionable.SIMULATE );

				if( results == null )
				{
					return resource.amount;
				}
				return resource.amount - (int) results.getStackSize();
			}
			return 0;
		}

		@Override
		public FluidStack drain( final FluidStack resource, final boolean doDrain )
		{
			return null;
		}

		@Override
		public FluidStack drain( final int maxDrain, final boolean doDrain )
		{
			return null;
		}

		@Override
		public IFluidTankProperties[] getTankProperties()
		{
			TileChest.this.updateHandler();

			if( TileChest.this.cellHandler != null && TileChest.this.cellHandler
					.getChannel() == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
			{
				return this.TANK_PROPS;
			}
			return null;
		}
	}

	private class InputInventoryFilter implements IAEItemFilter
	{
		@Override
		public boolean allowExtract( IItemHandler inv, int slot, int amount )
		{
			return false;
		}

		@Override
		public boolean allowInsert( IItemHandler inv, int slot, ItemStack stack )
		{
			if( TileChest.this.isPowered() )
			{
				TileChest.this.updateHandler();
				return TileChest.this.cellHandler != null && TileChest.this.cellHandler
						.getChannel() == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class );
			}
			return false;
		}
	}

	private class CellInventoryFilter implements IAEItemFilter
	{

		@Override
		public boolean allowExtract( IItemHandler inv, int slot, int amount )
		{
			return true;
		}

		@Override
		public boolean allowInsert( IItemHandler inv, int slot, ItemStack stack )
		{
			return AEApi.instance().registries().cell().getHandler( stack ) != null;
		}

	}

	@Override
	public ItemStack getItemStackRepresentation()
	{
		return AEApi.instance().definitions().blocks().chest().maybeStack( 1 ).orElse( ItemStack.EMPTY );
	}

	@Override
	public GuiBridge getGuiBridge()
	{
		this.updateHandler();
		if( this.cellHandler != null )
		{
			if( this.cellHandler.getChannel() == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) )
			{
				return GuiBridge.GUI_ME;
			}
			if( this.cellHandler.getChannel() == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
			{
				return GuiBridge.GUI_FLUID_TERMINAL;
			}
		}
		return null;
	}

}
