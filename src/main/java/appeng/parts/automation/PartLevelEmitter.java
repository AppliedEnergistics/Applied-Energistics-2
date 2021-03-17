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

package appeng.parts.automation;


import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Random;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.LevelType;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherHost;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.Reflected;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.parts.PartModel;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;


public class PartLevelEmitter extends PartUpgradeable implements IEnergyWatcherHost, IStackWatcherHost, ICraftingWatcherHost, IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingProvider, IGridTickable
{

	@PartModels
	public static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation( AppEng.MOD_ID, "part/level_emitter_base_off" );
	@PartModels
	public static final ResourceLocation MODEL_BASE_ON = new ResourceLocation( AppEng.MOD_ID, "part/level_emitter_base_on" );
	@PartModels
	public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation( AppEng.MOD_ID, "part/level_emitter_status_off" );
	@PartModels
	public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation( AppEng.MOD_ID, "part/level_emitter_status_on" );
	@PartModels
	public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation( AppEng.MOD_ID, "part/level_emitter_status_has_channel" );

	public static final PartModel MODEL_OFF_OFF = new PartModel( MODEL_BASE_OFF, MODEL_STATUS_OFF );
	public static final PartModel MODEL_OFF_ON = new PartModel( MODEL_BASE_OFF, MODEL_STATUS_ON );
	public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel( MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL );
	public static final PartModel MODEL_ON_OFF = new PartModel( MODEL_BASE_ON, MODEL_STATUS_OFF );
	public static final PartModel MODEL_ON_ON = new PartModel( MODEL_BASE_ON, MODEL_STATUS_ON );
	public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel( MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL );

	private static final int FLAG_ON = 4;

	private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 1 );

	private boolean prevState = false;

	private long lastReportedValue = 0;
	private long reportingValue = 0;

	private IStackWatcher myWatcher;
	private IEnergyWatcher myEnergyWatcher;
	private ICraftingWatcher myCraftingWatcher;
	private double centerX;
	private double centerY;
	private double centerZ;
	private IBaseMonitor<IAEItemStack> changedMonitor;

	@Reflected
	public PartLevelEmitter( final ItemStack is )
	{
		super( is );

		this.getConfigManager().registerSetting( Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL );
		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.LEVEL_TYPE, LevelType.ITEM_LEVEL );
		this.getConfigManager().registerSetting( Settings.CRAFT_VIA_REDSTONE, YesNo.NO );
	}

	public long getReportingValue()
	{
		return this.reportingValue;
	}

	public void setReportingValue( final long v )
	{
		this.reportingValue = v;
		if( this.getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
		{
			this.configureWatchers();
		}
		else
		{
			this.updateState();
		}
	}

	@MENetworkEventSubscribe
	public void powerChanged( final MENetworkPowerStatusChange c )
	{
		this.updateState();
	}

	private void updateState()
	{
		final boolean isOn = this.isLevelEmitterOn();
		if( this.prevState != isOn )
		{
			this.getHost().markForUpdate();
			final TileEntity te = this.getHost().getTile();
			this.prevState = isOn;
			Platform.notifyBlocksOfNeighbors( te.getWorld(), te.getPos() );
			Platform.notifyBlocksOfNeighbors( te.getWorld(), te.getPos().offset( this.getSide().getFacing() ) );
		}
	}

	// TODO: Make private again
	public boolean isLevelEmitterOn()
	{
		if( Platform.isClient() )
		{
			return ( this.getClientFlags() & FLAG_ON ) == FLAG_ON;
		}

		if( !this.getProxy().isActive() )
		{
			return false;
		}

		if( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			try
			{
				return this.getProxy().getCrafting().isRequesting( this.config.getAEStackInSlot( 0 ) );
			}
			catch( final GridAccessException e )
			{
				// :P
			}

			return this.prevState;
		}

		final boolean flipState = this.getConfigManager().getSetting( Settings.REDSTONE_EMITTER ) == RedstoneMode.LOW_SIGNAL;
		return flipState ? this.reportingValue >= this.lastReportedValue + 1 : this.reportingValue < this.lastReportedValue + 1;
	}

	@MENetworkEventSubscribe
	public void channelChanged( final MENetworkChannelsChanged c )
	{
		this.updateState();
	}

	@Override
	protected int populateFlags( final int cf )
	{
		return cf | ( this.prevState ? FLAG_ON : 0 );
	}

	@Override
	public void updateWatcher( final ICraftingWatcher newWatcher )
	{
		this.myCraftingWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void onRequestChange( final ICraftingGrid craftingGrid, final IAEItemStack what )
	{
		this.updateState();
	}

	// update the system...
	private void configureWatchers()
	{
		final IAEItemStack myStack = this.config.getAEStackInSlot( 0 );

		if( this.myWatcher != null )
		{
			this.myWatcher.reset();
		}

		if( this.myEnergyWatcher != null )
		{
			this.myEnergyWatcher.reset();
		}

		if( this.myCraftingWatcher != null )
		{
			this.myCraftingWatcher.reset();
		}

		try
		{
			this.getProxy().getGrid().postEvent( new MENetworkCraftingPatternChange( this, this.getProxy().getNode() ) );
		}
		catch( final GridAccessException e1 )
		{
			// :/
		}

		if( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if( this.myCraftingWatcher != null && myStack != null )
			{
				this.myCraftingWatcher.add( myStack );
			}

			return;
		}

		if( this.getConfigManager().getSetting( Settings.LEVEL_TYPE ) == LevelType.ENERGY_LEVEL )
		{
			if( this.myEnergyWatcher != null )
			{
				this.myEnergyWatcher.add( this.reportingValue );
			}

			try
			{
				// update to power...
				this.lastReportedValue = (long) this.getProxy().getEnergy().getStoredPower();
				this.updateState();

				// no more item stuff..
				this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ).removeListener( this );
			}
			catch( final GridAccessException e )
			{
				// :P
			}

			return;
		}

		try
		{
			if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 || myStack == null )
			{
				this.getProxy()
						.getStorage()
						.getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) )
						.addListener( this,
								this.getProxy().getGrid() );
			}
			else
			{
				this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ).removeListener( this );

				if( this.myWatcher != null )
				{
					this.myWatcher.add( myStack );
				}
			}

			this.updateReportingValue( this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ) );
		}
		catch( final GridAccessException e )
		{
			// >.>
		}
	}

	private void updateReportingValue( final IMEMonitor<IAEItemStack> monitor )
	{
		final IAEItemStack myStack = this.config.getAEStackInSlot( 0 );

		if( myStack == null )
		{
			this.lastReportedValue = 0;
			for( final IAEItemStack st : monitor.getStorageList() )
			{
				this.lastReportedValue += st.getStackSize();
			}
		}
		else if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
		{
			this.lastReportedValue = 0;
			final FuzzyMode fzMode = (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE );
			final Collection<IAEItemStack> fuzzyList = monitor.getStorageList().findFuzzy( myStack, fzMode );
			for( final IAEItemStack st : fuzzyList )
			{
				this.lastReportedValue += st.getStackSize();
			}
		}
		else
		{
			final IAEItemStack r = monitor.getStorageList().findPrecise( myStack );
			if( r == null )
			{
				this.lastReportedValue = 0;
			}
			else
			{
				this.lastReportedValue = r.getStackSize();
			}
		}

		this.updateState();
	}

	@Override
	public void updateWatcher( final IStackWatcher newWatcher )
	{
		this.myWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void onStackChange( final IItemList o, final IAEStack fullStack, final IAEStack diffStack, final IActionSource src, final IStorageChannel chan )
	{
		if( chan == AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) && fullStack.equals( this.config.getAEStackInSlot( 0 ) ) && this
				.getInstalledUpgrades( Upgrades.FUZZY ) == 0 )
		{
			this.lastReportedValue = fullStack.getStackSize();
			this.updateState();
		}
	}

	@Override
	public void updateWatcher( final IEnergyWatcher newWatcher )
	{
		this.myEnergyWatcher = newWatcher;
		this.configureWatchers();
	}

	@Override
	public void onThresholdPass( final IEnergyGrid energyGrid )
	{
		this.lastReportedValue = (long) energyGrid.getStoredPower();
		this.updateState();
	}

	@Override
	public boolean isValid( final Object effectiveGrid )
	{
		try
		{
			return this.getProxy().getGrid() == effectiveGrid;
		}
		catch( final GridAccessException e )
		{
			return false;
		}
	}

	@Override
	public void postChange( final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change, final IActionSource actionSource )
	{
		this.changedMonitor = monitor;
	}

	@Override
	public void onListUpdate()
	{
		try
		{
			this.updateReportingValue( this.getProxy().getStorage().getInventory( AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ) ) );
		}
		catch( final GridAccessException e )
		{
			// ;P
		}
	}

	@Override
	public AECableType getCableConnectionType( final AEPartLocation dir )
	{
		return AECableType.SMART;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 7, 7, 11, 9, 9, 16 );
	}

	@Override
	public int isProvidingStrongPower()
	{
		return this.prevState ? 15 : 0;
	}

	@Override
	public int isProvidingWeakPower()
	{
		return this.prevState ? 15 : 0;
	}

	@Override
	public void randomDisplayTick( final World world, final BlockPos pos, final Random r )
	{
		if( this.isLevelEmitterOn() )
		{
			final AEPartLocation d = this.getSide();

			final double d0 = d.xOffset * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;
			final double d1 = d.yOffset * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;
			final double d2 = d.zOffset * 0.45F + ( r.nextFloat() - 0.5F ) * 0.2D;

			world.spawnParticle( EnumParticleTypes.REDSTONE, 0.5 + pos.getX() + d0, 0.5 + pos.getY() + d1, 0.5 + pos.getZ() + d2, 0.0D, 0.0D, 0.0D,
					new int[0] );
		}
	}

	@Override
	public float getCableConnectionLength( AECableType cable )
	{
		return 16;
	}

	@Override
	public boolean onPartActivate( final EntityPlayer player, final EnumHand hand, final Vec3d pos )
	{
		if( Platform.isServer() )
		{
			Platform.openGUI( player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_LEVEL_EMITTER );
		}
		return true;
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum settingName, final Enum newValue )
	{
		this.configureWatchers();
	}

	@Override
	public void onChangeInventory( final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
	{
		if( inv == this.config )
		{
			this.configureWatchers();
		}

		super.onChangeInventory( inv, slot, mc, removedStack, newStack );
	}

	@Override
	public void upgradesChanged()
	{
		this.configureWatchers();
	}

	@Override
	public boolean canConnectRedstone()
	{
		return true;
	}

	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.lastReportedValue = data.getLong( "lastReportedValue" );
		this.reportingValue = data.getLong( "reportingValue" );
		this.prevState = data.getBoolean( "prevState" );
		this.config.readFromNBT( data, "config" );
	}

	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );
		data.setLong( "lastReportedValue", this.lastReportedValue );
		data.setLong( "reportingValue", this.reportingValue );
		data.setBoolean( "prevState", this.prevState );
		this.config.writeToNBT( data, "config" );
	}

	@Override
	public IItemHandler getInventoryByName( final String name )
	{
		if( name.equals( "config" ) )
		{
			return this.config;
		}

		return super.getInventoryByName( name );
	}

	@Override
	public boolean pushPattern( final ICraftingPatternDetails patternDetails, final InventoryCrafting table )
	{
		return false;
	}

	@Override
	public boolean isBusy()
	{
		return true;
	}

	@Override
	public void provideCrafting( final ICraftingProviderHelper craftingTracker )
	{
		if( this.getInstalledUpgrades( Upgrades.CRAFTING ) > 0 )
		{
			if( this.getConfigManager().getSetting( Settings.CRAFT_VIA_REDSTONE ) == YesNo.YES )
			{
				final IAEItemStack what = this.config.getAEStackInSlot( 0 );
				if( what != null )
				{
					craftingTracker.setEmitable( what );
				}
			}
		}
	}

	@Override
	public IPartModel getStaticModels()
	{
		if( this.isActive() && this.isPowered() )
		{
			return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
		}
		else if( this.isPowered() )
		{
			return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
		}
		else
		{
			return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
		}
	}

	@Nonnull
	@Override
	public TickingRequest getTickingRequest( @Nonnull IGridNode node )
	{
		return new TickingRequest( 1,1,false,false );
	}

	@Nonnull
	@Override
	public TickRateModulation tickingRequest( @Nonnull IGridNode node, int ticksSinceLastCall )
	{
		if (this.changedMonitor != null)
		{
			this.updateReportingValue((IMEMonitor<IAEItemStack>) changedMonitor);
		}
		return TickRateModulation.SAME;
	}
}
