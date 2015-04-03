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

package appeng.container.implementations;


import java.io.IOException;
import java.nio.BufferOverflowException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public class ContainerMEMonitorable extends AEBaseContainer implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEItemStack>
{

	public final SlotRestrictedInput[] cellView = new SlotRestrictedInput[5];
	final IMEMonitor<IAEItemStack> monitor;
	final IItemList<IAEItemStack> items = AEApi.instance().storage().createItemList();
	final IConfigManager clientCM;
	private final ITerminalHost host;
	@GuiSync( 99 )
	public boolean canAccessViewCells = false;
	@GuiSync( 98 )
	public boolean hasPower = false;
	public IConfigManagerHost gui;
	IConfigManager serverCM;
	private IGridNode networkNode;

	public ContainerMEMonitorable( InventoryPlayer ip, ITerminalHost monitorable )
	{
		this( ip, monitorable, true );
	}

	protected ContainerMEMonitorable( InventoryPlayer ip, ITerminalHost monitorable, boolean bindInventory )
	{
		super( ip, monitorable instanceof TileEntity ? (TileEntity) monitorable : null, monitorable instanceof IPart ? (IPart) monitorable : null );

		this.host = monitorable;
		this.clientCM = new ConfigManager( this );

		this.clientCM.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.clientCM.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.clientCM.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		if( Platform.isServer() )
		{
			this.serverCM = monitorable.getConfigManager();

			this.monitor = monitorable.getItemInventory();
			if( this.monitor != null )
			{
				this.monitor.addListener( this, null );

				this.cellInv = this.monitor;

				if( monitorable instanceof IPortableCell )
					this.powerSrc = (IPortableCell) monitorable;
				else if( monitorable instanceof IMEChest )
					this.powerSrc = (IMEChest) monitorable;
				else if( monitorable instanceof IGridHost )
				{
					IGridNode node = ( (IGridHost) monitorable ).getGridNode( ForgeDirection.UNKNOWN );
					if( node != null )
					{
						this.networkNode = node;
						IGrid g = node.getGrid();
						if( g != null )
							this.powerSrc = new ChannelPowerSrc( this.networkNode, (IEnergyGrid) g.getCache( IEnergyGrid.class ) );
					}
				}
			}
			else
				this.isContainerValid = false;
		}
		else
			this.monitor = null;

		this.canAccessViewCells = false;
		if( monitorable instanceof IViewCellStorage )
		{
			for( int y = 0; y < 5; y++ )
			{
				this.cellView[y] = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.VIEW_CELL, ( (IViewCellStorage) monitorable ).getViewCellStorage(), y, 206, y * 18 + 8, this.invPlayer );
				this.cellView[y].allowEdit = this.canAccessViewCells;
				this.addSlotToContainer( this.cellView[y] );
			}
		}

		if( bindInventory )
			this.bindPlayerInventory( ip, 0, 0 );
	}

	public IGridNode getNetworkNode()
	{
		return this.networkNode;
	}

	@Override
	public void detectAndSendChanges()
	{
		if( Platform.isServer() )
		{
			if( this.monitor != this.host.getItemInventory() )
				this.isContainerValid = false;

			for( Settings set : this.serverCM.getSettings() )
			{
				Enum<?> sideLocal = this.serverCM.getSetting( set );
				Enum<?> sideRemote = this.clientCM.getSetting( set );

				if( sideLocal != sideRemote )
				{
					this.clientCM.putSetting( set, sideLocal );
					for( Object crafter : this.crafters )
					{
						try
						{
							NetworkHandler.instance.sendTo( new PacketValueConfig( set.name(), sideLocal.name() ), (EntityPlayerMP) crafter );
						}
						catch( IOException e )
						{
							AELog.error( e );
						}
					}
				}
			}

			if( !this.items.isEmpty() )
			{
				try
				{
					IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

					PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();

					for( IAEItemStack is : this.items )
					{
						IAEItemStack send = monitorCache.findPrecise( is );
						if( send == null )
						{
							is.setStackSize( 0 );
							piu.appendItem( is );
						}
						else
							piu.appendItem( send );
					}

					if( !piu.isEmpty() )
					{
						this.items.resetStatus();

						for( Object c : this.crafters )
						{
							if( c instanceof EntityPlayer )
								NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );
						}
					}
				}
				catch( IOException e )
				{
					AELog.error( e );
				}
			}

			this.updatePowerStatus();

			boolean oldCanAccessViewCells = this.canAccessViewCells;
			this.canAccessViewCells = this.hasAccess( SecurityPermissions.BUILD, false );
			if( this.canAccessViewCells != oldCanAccessViewCells )
			{
				for( int y = 0; y < 5; y++ )
				{
					if( this.cellView[y] != null )
						this.cellView[y].allowEdit = this.canAccessViewCells;
				}
			}

			super.detectAndSendChanges();
		}
	}

	protected void updatePowerStatus()
	{
		try
		{
			if( this.networkNode != null )
				this.hasPower = this.networkNode.isActive();
			else if( this.powerSrc instanceof IEnergyGrid )
				this.hasPower = ( (IEnergyGrid) this.powerSrc ).isNetworkPowered();
			else
				this.hasPower = this.powerSrc.extractAEPower( 1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0.8;
		}
		catch( Throwable t )
		{
			// :P
		}
	}

	@Override
	public void onUpdate( String field, Object oldValue, Object newValue )
	{
		if( field.equals( "canAccessViewCells" ) )
		{
			for( int y = 0; y < 5; y++ )
				if( this.cellView[y] != null )
					this.cellView[y].allowEdit = this.canAccessViewCells;
		}

		super.onUpdate( field, oldValue, newValue );
	}

	@Override
	public void addCraftingToCrafters( ICrafting c )
	{
		super.addCraftingToCrafters( c );
		this.queueInventory( c );
	}

	public void queueInventory( ICrafting c )
	{
		if( Platform.isServer() && c instanceof EntityPlayer && this.monitor != null )
		{
			try
			{
				PacketMEInventoryUpdate piu = new PacketMEInventoryUpdate();
				IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

				for( IAEItemStack send : monitorCache )
				{
					try
					{
						piu.appendItem( send );
					}
					catch( BufferOverflowException boe )
					{
						NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );

						piu = new PacketMEInventoryUpdate();
						piu.appendItem( send );
					}
				}

				NetworkHandler.instance.sendTo( piu, (EntityPlayerMP) c );
			}
			catch( IOException e )
			{
				AELog.error( e );
			}
		}
	}

	@Override
	public void removeCraftingFromCrafters( ICrafting c )
	{
		super.removeCraftingFromCrafters( c );

		if( this.crafters.isEmpty() && this.monitor != null )
			this.monitor.removeListener( this );
	}

	@Override
	public void onContainerClosed( EntityPlayer player )
	{
		super.onContainerClosed( player );
		if( this.monitor != null )
			this.monitor.removeListener( this );
	}

	@Override
	public boolean isValid( Object verificationToken )
	{
		return true;
	}

	@Override
	public void postChange( IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change, BaseActionSource source )
	{
		for( IAEItemStack is : change )
			this.items.add( is );
	}

	@Override
	public void onListUpdate()
	{
		for( Object c : this.crafters )
		{
			if( c instanceof ICrafting )
			{
				ICrafting cr = (ICrafting) c;
				this.queueInventory( cr );
			}
		}
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{
		if( this.gui != null )
			this.gui.updateSetting( manager, settingName, newValue );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		if( Platform.isServer() )
			return this.serverCM;
		return this.clientCM;
	}

	public ItemStack[] getViewCells()
	{
		ItemStack[] list = new ItemStack[this.cellView.length];

		for( int x = 0; x < this.cellView.length; x++ )
			list[x] = this.cellView[x].getStack();

		return list;
	}
}
