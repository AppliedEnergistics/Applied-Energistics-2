/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.parts.misc;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.capabilities.Capabilities;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.stats.Achievements;
import appeng.core.sync.GuiBridge;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEMonitorIInventory;
import appeng.parts.PartModel;
import appeng.parts.automation.PartUpgradeable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;


public class PartStorageBus extends PartUpgradeable implements IGridTickable, ICellContainer, IMEMonitorHandlerReceiver<IAEItemStack>, IPriorityHost
{

	public static final ResourceLocation MODEL_BASE = new ResourceLocation( AppEng.MOD_ID, "part/storage_bus_base" );

	@PartModels
	public static final IPartModel MODELS_OFF =  new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/storage_bus_off" ) ) ;

	@PartModels
	public static final IPartModel MODELS_ON = new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/storage_bus_on" ) ) ;

	@PartModels
	public static final IPartModel MODELS_HAS_CHANNEL =new PartModel( MODEL_BASE, new ResourceLocation( AppEng.MOD_ID, "part/storage_bus_has_channel"  ) );

	private final BaseActionSource mySrc;
	private final AppEngInternalAEInventory Config = new AppEngInternalAEInventory( this, 63 );
	private int priority = 0;
	private boolean cached = false;
	private ITickingMonitor monitor = null;
	private MEInventoryHandler<? extends IAEStack> handler = null;
	private int handlerHash = 0;
	private boolean wasActive = false;
	private byte resetCacheLogic = 0;

	@Reflected
	public PartStorageBus( final ItemStack is )
	{
		super( is );
		this.getConfigManager().registerSetting( Settings.ACCESS, AccessRestriction.READ_WRITE );
		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY );
		this.mySrc = new MachineSource( this );
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender( final MENetworkPowerStatusChange c )
	{
		this.updateStatus();
	}

	private void updateStatus()
	{
		final boolean currentActive = this.getProxy().isActive();
		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			try
			{
				this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
				this.getHost().markForUpdate();
			}
			catch( final GridAccessException e )
			{
				// :P
			}
		}
	}

	@MENetworkEventSubscribe
	public void updateChannels( final MENetworkChannelsChanged changedChannels )
	{
		this.updateStatus();
	}

	@Override
	protected int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
		this.resetCache( true );
		this.getHost().markForSave();
	}

	@Override
	public void onChangeInventory( final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{
		super.onChangeInventory( inv, slot, mc, removedStack, newStack );

		if( inv == this.Config )
		{
			this.resetCache( true );
		}
	}

	@Override
	public void upgradesChanged()
	{
		super.upgradesChanged();
		this.resetCache( true );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.Config.readFromNBT( data, "config" );
		this.priority = data.getInteger( "priority" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.Config.writeToNBT( data, "config" );
		data.setInteger( "priority", this.priority );
	}

	@Override
	public IInventory getInventoryByName( final String name )
	{
		if( name.equals( "config" ) )
		{
			return this.Config;
		}

		return super.getInventoryByName( name );
	}

	private void resetCache( final boolean fullReset )
	{
		if( this.getHost() == null || this.getHost().getTile() == null || this.getHost().getTile().getWorld() == null || this.getHost()
				.getTile()
				.getWorld().isRemote )
		{
			return;
		}

		if( fullReset )
		{
			this.resetCacheLogic = 2;
		}
		else
		{
			this.resetCacheLogic = 1;
		}

		try
		{
			this.getProxy().getTick().alertDevice( this.getProxy().getNode() );
		}
		catch( final GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public boolean isValid( final Object verificationToken )
	{
		return this.handler == verificationToken;
	}

	@Override
	public void postChange( final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final BaseActionSource source )
	{
		try
		{
			if( this.getProxy().isActive() )
			{
				this.getProxy().getStorage().postAlterationOfStoredItems( StorageChannel.ITEMS, change, this.mySrc );
			}
		}
		catch( final GridAccessException e )
		{
			// :(
		}
	}

	@Override
	public void onListUpdate()
	{
		// not used here.
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 3, 3, 15, 13, 13, 16 );
		bch.addBox( 2, 2, 14, 14, 14, 15 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	public void onNeighborChanged()
	{
		this.resetCache( false );
	}

	@Override
	public float getCableConnectionLength( AECableType cable )
	{
		return 4;
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( !player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_STORAGEBUS );
			return true;
		}

		return false;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode node )
	{
		return new TickingRequest( TickRates.StorageBus.getMin(), TickRates.StorageBus.getMax(), this.monitor == null, true );
	}

	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int ticksSinceLastCall )
	{
		if( this.resetCacheLogic != 0 )
		{
			this.resetCache();
		}

		if( this.monitor != null )
		{
			return this.monitor.onTick();
		}

		return TickRateModulation.SLEEP;
	}

	private void resetCache()
	{
		final boolean fullReset = this.resetCacheLogic == 2;
		this.resetCacheLogic = 0;

		final IMEInventory<IAEItemStack> in = this.getInternalHandler();
		IItemList<IAEItemStack> before = AEApi.instance().storage().createItemList();
		if( in != null )
		{
			before = in.getAvailableItems( before );
		}

		this.cached = false;
		if( fullReset )
		{
			this.handlerHash = 0;
		}

		final IMEInventory<IAEItemStack> out = this.getInternalHandler();

		if( this.monitor != null )
		{
			this.monitor.onTick();
		}

		IItemList<IAEItemStack> after = AEApi.instance().storage().createItemList();
		if( out != null )
		{
			after = out.getAvailableItems( after );
		}

		Platform.postListChanges( before, after, this, this.mySrc );
	}

	@SuppressWarnings( "unchecked" )
	private IMEInventory<? extends IAEItemStack> getInventoryWrapper( TileEntity target )
	{

		EnumFacing targetSide = this.getSide().getFacing().getOpposite();

		// Prioritize a handler to directly link to another ME network
		IStorageMonitorableAccessor accessor = target.getCapability( Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide );

		if( accessor != null )
		{
			IStorageMonitorable inventory = accessor.getInventory( mySrc );
			if( inventory != null )
			{
				return inventory.getItemInventory();
			}

			// So this could / can be a design decision. If the tile does support our custom capability,
			// but it does not return an inventory for the action source, we do NOT fall back to using
			// IItemHandler's, as that might circumvent the security setings, and might also cause
			// performance issues.
			return null;
		}

		// Check via cap for IItemHandler
		IItemHandler handlerExt = target.getCapability( CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, targetSide );
		if( handlerExt != null )
		{
			return new ItemHandlerAdapter( handlerExt );
		}

		return null;

	}

	public MEInventoryHandler getInternalHandler()
	{
		if( this.cached )
		{
			return this.handler;
		}

		final boolean wasSleeping = this.monitor == null;

		this.cached = true;
		final TileEntity self = this.getHost().getTile();
		final TileEntity target = self.getWorld().getTileEntity( self.getPos().offset( this.getSide().getFacing() ) );
		final int newHandlerHash = Platform.generateTileHash( target );

		if( this.handlerHash == newHandlerHash && this.handlerHash != 0 )
		{
			return this.handler;
		}

		this.handlerHash = newHandlerHash;
		this.handler = null;
		this.monitor = null;
		if( target != null )
		{
			IMEInventory<? extends IAEStack> inv = getInventoryWrapper( target );

			if( inv instanceof MEMonitorIInventory )
			{
				final MEMonitorIInventory h = (MEMonitorIInventory) inv;
				h.setMode( (StorageFilter) this.getConfigManager().getSetting( Settings.STORAGE_FILTER ) );
			}

			if( inv instanceof ITickingMonitor )
			{
				this.monitor = (ITickingMonitor) inv;
				this.monitor.setActionSource( new MachineSource( this ) );
			}

			if( inv != null )
			{
				this.checkInterfaceVsStorageBus( target, this.getSide().getOpposite() );

				this.handler = new MEInventoryHandler<>( inv, StorageChannel.ITEMS );

				this.handler.setBaseAccess( (AccessRestriction) this.getConfigManager().getSetting( Settings.ACCESS ) );
				this.handler.setWhitelist( this.getInstalledUpgrades( Upgrades.INVERTER ) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST );
				this.handler.setPriority( this.priority );

				final IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

				final int slotsToUse = 18 + this.getInstalledUpgrades( Upgrades.CAPACITY ) * 9;
				for( int x = 0; x < this.Config.getSizeInventory() && x < slotsToUse; x++ )
				{
					final IAEItemStack is = this.Config.getAEStackInSlot( x );
					if( is != null )
					{
						priorityList.add( is );
					}
				}

				if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
				{
					this.handler
							.setPartitionList( new FuzzyPriorityList( priorityList, (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE ) ) );
				}
				else
				{
					this.handler.setPartitionList( new PrecisePriorityList( priorityList ) );
				}

				if( inv instanceof IBaseMonitor )
				{
					( (IBaseMonitor) inv ).addListener( this, this.handler );
				}
			}
		}

		// update sleep state...
		if( wasSleeping != ( this.monitor == null ) )
		{
			try
			{
				final ITickManager tm = this.getProxy().getTick();
				if( this.monitor == null )
				{
					tm.sleepDevice( this.getProxy().getNode() );
				}
				else
				{
					tm.wakeDevice( this.getProxy().getNode() );
				}
			}
			catch( final GridAccessException e )
			{
				// :(
			}
		}

		try
		{
			// force grid to update handlers...
			this.getProxy().getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch( final GridAccessException e )
		{
			// :3
		}

		return this.handler;
	}

	private void checkInterfaceVsStorageBus( final TileEntity target, final AEPartLocation side )
	{
		IInterfaceHost achievement = null;

		if( target instanceof IInterfaceHost )
		{
			achievement = (IInterfaceHost) target;
		}

		if( target instanceof IPartHost )
		{
			final Object part = ( (IPartHost) target ).getPart( side );
			if( part instanceof IInterfaceHost )
			{
				achievement = (IInterfaceHost) part;
			}
		}

		if( achievement != null && achievement.getActionableNode() != null )
		{
			Platform.addStat( achievement.getActionableNode().getPlayerID(), Achievements.Recursive.getAchievement() );
			// Platform.addStat( getActionableNode().getPlayerID(), Achievements.Recursive.getAchievement() );
		}
	}

	@Override
	public List<IMEInventoryHandler> getCellArray( final StorageChannel channel )
	{
		if( channel == StorageChannel.ITEMS )
		{
			final IMEInventoryHandler out = this.getProxy().isActive() ? this.getInternalHandler() : null;
			if( out != null )
			{
				return Collections.singletonList( out );
			}
		}
		return Arrays.asList( new IMEInventoryHandler[] {} );
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
		this.getHost().markForSave();
		this.resetCache( true );
	}

	@Override
	public void blinkCell( final int slot )
	{
	}

	// TODO: BC PIPE INTEGRATION
	/*
	 * @Override
	 * @Method( iname = IntegrationType.BuildCraftTransport )
	 * public ConnectOverride overridePipeConnection( PipeType type, ForgeDirection with )
	 * {
	 * return type == PipeType.ITEM && with == this.getSide() ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT;
	 * }
	 */
	@Override
	public void saveChanges( final IMEInventory cellInventory )
	{
		// nope!
	}

	@Override
	public IPartModel getStaticModels()
	{
		if( isActive() && isPowered() )
		{
			return MODELS_HAS_CHANNEL;
		}
		else if( isPowered() )
		{
			return MODELS_ON;
		}
		else
		{
			return MODELS_OFF;
		}
	}

}
