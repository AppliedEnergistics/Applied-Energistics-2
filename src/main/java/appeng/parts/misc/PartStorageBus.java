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

package appeng.parts.misc;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile.PipeType;

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
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.core.stats.Achievements;
import appeng.core.sync.GuiBridge;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEMonitorIInventory;
import appeng.parts.automation.PartUpgradeable;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.InvOperation;
import appeng.transformer.annotations.Integration.Interface;
import appeng.transformer.annotations.Integration.Method;
import appeng.util.Platform;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.PrecisePriorityList;


@Interface( iname = "BC", iface = "buildcraft.api.transport.IPipeConnection" )
public class PartStorageBus extends PartUpgradeable implements IGridTickable, ICellContainer, IMEMonitorHandlerReceiver<IAEItemStack>, IPipeConnection, IPriorityHost
{
	final BaseActionSource mySrc;
	final AppEngInternalAEInventory Config = new AppEngInternalAEInventory( this, 63 );
	int priority = 0;
	boolean cached = false;
	MEMonitorIInventory monitor = null;
	MEInventoryHandler handler = null;
	int handlerHash = 0;
	boolean wasActive = false;
	private byte resetCacheLogic = 0;

	@Reflected
	public PartStorageBus( ItemStack is )
	{
		super( is );
		this.getConfigManager().registerSetting( Settings.ACCESS, AccessRestriction.READ_WRITE );
		this.getConfigManager().registerSetting( Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL );
		this.getConfigManager().registerSetting( Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY );
		this.mySrc = new MachineSource( this );
	}

	@Override
	@MENetworkEventSubscribe
	public void powerRender( MENetworkPowerStatusChange c )
	{
		this.updateStatus();
	}

	private void updateStatus()
	{
		boolean currentActive = this.proxy.isActive();
		if( this.wasActive != currentActive )
		{
			this.wasActive = currentActive;
			try
			{
				this.proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
				this.host.markForUpdate();
			}
			catch( GridAccessException e )
			{
				// :P
			}
		}
	}

	@MENetworkEventSubscribe
	public void updateChannels( MENetworkChannelsChanged changedChannels )
	{
		this.updateStatus();
	}

	@Override
	protected int getUpgradeSlots()
	{
		return 5;
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		this.resetCache( true );
		this.host.markForSave();
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
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
	public void readFromNBT( NBTTagCompound data )
	{
		super.readFromNBT( data );
		this.Config.readFromNBT( data, "config" );
		this.priority = data.getInteger( "priority" );
	}

	@Override
	public void writeToNBT( NBTTagCompound data )
	{
		super.writeToNBT( data );
		this.Config.writeToNBT( data, "config" );
		data.setInteger( "priority", this.priority );
	}

	@Override
	public IInventory getInventoryByName( String name )
	{
		if( name.equals( "config" ) )
		{
			return this.Config;
		}

		return super.getInventoryByName( name );
	}

	private void resetCache( boolean fullReset )
	{
		if( this.host == null || this.host.getTile() == null || this.host.getTile().getWorldObj() == null || this.host.getTile().getWorldObj().isRemote )
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
			this.proxy.getTick().alertDevice( this.proxy.getNode() );
		}
		catch( GridAccessException e )
		{
			// :P
		}
	}

	@Override
	public boolean isValid( Object verificationToken )
	{
		return this.handler == verificationToken;
	}

	@Override
	public void postChange( IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, BaseActionSource source )
	{
		try
		{
			if( this.proxy.isActive() )
			{
				this.proxy.getStorage().postAlterationOfStoredItems( StorageChannel.ITEMS, change, this.mySrc );
			}
		}
		catch( GridAccessException e )
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
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 3, 3, 15, 13, 13, 16 );
		bch.addBox( 2, 2, 14, 14, 14, 15 );
		bch.addBox( 5, 5, 12, 11, 11, 14 );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderInventory( IPartRenderHelper rh, RenderBlocks renderer )
	{
		rh.setTexture( CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 2, 2, 14, 14, 14, 15 );
		rh.renderInventoryBox( renderer );

		rh.setBounds( 5, 5, 12, 11, 11, 14 );
		rh.renderInventoryBox( renderer );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void renderStatic( int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer )
	{
		this.renderCache = rh.useSimplifiedRendering( x, y, z, this, this.renderCache );
		rh.setTexture( CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon() );

		rh.setBounds( 3, 3, 15, 13, 13, 16 );
		rh.renderBlock( x, y, z, renderer );

		rh.setBounds( 2, 2, 14, 14, 14, 15 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartStorageSides.getIcon(), CableBusTextures.PartStorageSides.getIcon() );

		rh.setBounds( 5, 5, 12, 11, 11, 13 );
		rh.renderBlock( x, y, z, renderer );

		rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), this.is.getIconIndex(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

		rh.setBounds( 5, 5, 13, 11, 11, 14 );
		rh.renderBlock( x, y, z, renderer );

		this.renderLights( x, y, z, rh, renderer );
	}

	@Override
	public void onNeighborChanged()
	{
		this.resetCache( false );
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 4;
	}

	@Override
	public boolean onPartActivate( EntityPlayer player, Vec3 pos )
	{
		if( !player.isSneaking() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( player, this.getHost().getTile(), this.side, GuiBridge.GUI_STORAGEBUS );
			return true;
		}

		return false;
	}

	@Override
	public TickingRequest getTickingRequest( IGridNode node )
	{
		return new TickingRequest( TickRates.StorageBus.min, TickRates.StorageBus.max, this.monitor == null, true );
	}

	@Override
	public TickRateModulation tickingRequest( IGridNode node, int TicksSinceLastCall )
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
		boolean fullReset = this.resetCacheLogic == 2;
		this.resetCacheLogic = 0;

		IMEInventory<IAEItemStack> in = this.getInternalHandler();
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

		IMEInventory<IAEItemStack> out = this.getInternalHandler();

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

	public MEInventoryHandler getInternalHandler()
	{
		if( this.cached )
		{
			return this.handler;
		}

		boolean wasSleeping = this.monitor == null;

		this.cached = true;
		TileEntity self = this.getHost().getTile();
		TileEntity target = self.getWorldObj().getTileEntity( self.xCoord + this.side.offsetX, self.yCoord + this.side.offsetY, self.zCoord + this.side.offsetZ );

		int newHandlerHash = Platform.generateTileHash( target );

		if( this.handlerHash == newHandlerHash && this.handlerHash != 0 )
		{
			return this.handler;
		}

		try
		{
			// force grid to update handlers...
			this.proxy.getGrid().postEvent( new MENetworkCellArrayUpdate() );
		}
		catch( GridAccessException e )
		{
			// :3
		}

		this.handlerHash = newHandlerHash;
		this.handler = null;
		this.monitor = null;
		if( target != null )
		{
			IExternalStorageHandler esh = AEApi.instance().registries().externalStorage().getHandler( target, this.side.getOpposite(), StorageChannel.ITEMS, this.mySrc );
			if( esh != null )
			{
				IMEInventory inv = esh.getInventory( target, this.side.getOpposite(), StorageChannel.ITEMS, this.mySrc );

				if( inv instanceof MEMonitorIInventory )
				{
					MEMonitorIInventory h = (MEMonitorIInventory) inv;
					h.mode = (StorageFilter) this.getConfigManager().getSetting( Settings.STORAGE_FILTER );
					h.mySource = new MachineSource( this );
				}

				if( inv instanceof MEMonitorIInventory )
				{
					this.monitor = (MEMonitorIInventory) inv;
				}

				if( inv != null )
				{
					this.checkInterfaceVsStorageBus( target, this.side.getOpposite() );

					this.handler = new MEInventoryHandler( inv, StorageChannel.ITEMS );

					this.handler.setBaseAccess( (AccessRestriction) this.getConfigManager().getSetting( Settings.ACCESS ) );
					this.handler.setWhitelist( this.getInstalledUpgrades( Upgrades.INVERTER ) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST );
					this.handler.setPriority( this.priority );

					IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();

					int slotsToUse = 18 + this.getInstalledUpgrades( Upgrades.CAPACITY ) * 9;
					for( int x = 0; x < this.Config.getSizeInventory() && x < slotsToUse; x++ )
					{
						IAEItemStack is = this.Config.getAEStackInSlot( x );
						if( is != null )
						{
							priorityList.add( is );
						}
					}

					if( this.getInstalledUpgrades( Upgrades.FUZZY ) > 0 )
					{
						this.handler.setPartitionList( new FuzzyPriorityList( priorityList, (FuzzyMode) this.getConfigManager().getSetting( Settings.FUZZY_MODE ) ) );
					}
					else
					{
						this.handler.setPartitionList( new PrecisePriorityList( priorityList ) );
					}

					if( inv instanceof IMEMonitor )
					{
						( (IMEMonitor) inv ).addListener( this, this.handler );
					}
				}
			}
		}

		// update sleep state...
		if( wasSleeping != ( this.monitor == null ) )
		{
			try
			{
				ITickManager tm = this.proxy.getTick();
				if( this.monitor == null )
				{
					tm.sleepDevice( this.proxy.getNode() );
				}
				else
				{
					tm.wakeDevice( this.proxy.getNode() );
				}
			}
			catch( GridAccessException e )
			{
				// :(
			}
		}

		return this.handler;
	}

	private void checkInterfaceVsStorageBus( TileEntity target, ForgeDirection side )
	{
		IInterfaceHost achievement = null;

		if( target instanceof IInterfaceHost )
		{
			achievement = (IInterfaceHost) target;
		}

		if( target instanceof IPartHost )
		{
			Object part = ( (IPartHost) target ).getPart( side );
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
	public List<IMEInventoryHandler> getCellArray( StorageChannel channel )
	{
		if( channel == StorageChannel.ITEMS )
		{
			IMEInventoryHandler out = this.proxy.isActive() ? this.getInternalHandler() : null;
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
	public void setPriority( int newValue )
	{
		this.priority = newValue;
		this.host.markForSave();
		this.resetCache( true );
	}

	@Override
	public void blinkCell( int slot )
	{
	}

	@Override
	@Method( iname = "BC" )
	public ConnectOverride overridePipeConnection( PipeType type, ForgeDirection with )
	{
		return type == PipeType.ITEM && with == this.side ? ConnectOverride.CONNECT : ConnectOverride.DISCONNECT;
	}

	@Override
	public void saveChanges( IMEInventory cellInventory )
	{
		// nope!
	}
}
