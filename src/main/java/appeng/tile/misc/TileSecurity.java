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

package appeng.tile.misc;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.features.IPlayerRegistry;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkSecurityChange;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.helpers.PlayerSecurityWrapper;
import appeng.me.GridAccessException;
import appeng.me.storage.SecurityInventory;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;


public class TileSecurity extends AENetworkTile implements ITerminalHost, IAEAppEngInventory, ILocatable, IConfigManagerHost, ISecurityProvider, IColorableTile
{

	private static int difference = 0;
	public final AppEngInternalInventory configSlot = new AppEngInternalInventory( this, 1 );
	private final IConfigManager cm = new ConfigManager( this );
	private final SecurityInventory inventory = new SecurityInventory( this );
	private final MEMonitorHandler<IAEItemStack> securityMonitor = new MEMonitorHandler<IAEItemStack>( this.inventory );
	public long securityKey;
	AEColor paintedColor = AEColor.Transparent;
	private boolean isActive = false;

	public TileSecurity()
	{
		this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		this.gridProxy.setIdlePowerUsage( 2.0 );
		difference++;

		this.securityKey = System.currentTimeMillis() * 10 + difference;
		if( difference > 10 )
		{
			difference = 0;
		}

		this.cm.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		this.cm.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		this.cm.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );
	}

	@Override
	public void onChangeInventory( IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack )
	{

	}

	@Override
	public void getDrops( World w, int x, int y, int z, ArrayList<ItemStack> drops )
	{
		if( !this.configSlot.isEmpty() )
		{
			drops.add( this.configSlot.getStackInSlot( 0 ) );
		}

		for( IAEItemStack ais : this.inventory.storedItems )
		{
			drops.add( ais.getItemStack() );
		}
	}

	IMEInventoryHandler<IAEItemStack> getSecurityInventory()
	{
		return this.inventory;
	}

	@TileEvent( TileEventType.NETWORK_READ )
	public boolean readFromStream_TileSecurity( ByteBuf data )
	{
		boolean wasActive = this.isActive;
		this.isActive = data.readBoolean();

		AEColor oldPaintedColor = this.paintedColor;
		this.paintedColor = AEColor.values()[data.readByte()];

		return oldPaintedColor != this.paintedColor || wasActive != this.isActive;
	}

	@TileEvent( TileEventType.NETWORK_WRITE )
	public void writeToStream_TileSecurity( ByteBuf data )
	{
		data.writeBoolean( this.gridProxy.isActive() );
		data.writeByte( this.paintedColor.ordinal() );
	}

	@TileEvent( TileEventType.WORLD_NBT_WRITE )
	public void writeToNBT_TileSecurity( NBTTagCompound data )
	{
		this.cm.writeToNBT( data );
		data.setByte( "paintedColor", (byte) this.paintedColor.ordinal() );

		data.setLong( "securityKey", this.securityKey );
		this.configSlot.writeToNBT( data, "config" );

		NBTTagCompound storedItems = new NBTTagCompound();

		int offset = 0;
		for( IAEItemStack ais : this.inventory.storedItems )
		{
			NBTTagCompound it = new NBTTagCompound();
			ais.getItemStack().writeToNBT( it );
			storedItems.setTag( String.valueOf( offset ), it );
			offset++;
		}

		data.setTag( "storedItems", storedItems );
	}

	@TileEvent( TileEventType.WORLD_NBT_READ )
	public void readFromNBT_TileSecurity( NBTTagCompound data )
	{
		this.cm.readFromNBT( data );
		if( data.hasKey( "paintedColor" ) )
		{
			this.paintedColor = AEColor.values()[data.getByte( "paintedColor" )];
		}

		this.securityKey = data.getLong( "securityKey" );
		this.configSlot.readFromNBT( data, "config" );

		NBTTagCompound storedItems = data.getCompoundTag( "storedItems" );
		for( Object key : storedItems.func_150296_c() )
		{
			NBTBase obj = storedItems.getTag( (String) key );
			if( obj instanceof NBTTagCompound )
			{
				this.inventory.storedItems.add( AEItemStack.create( ItemStack.loadItemStackFromNBT( (NBTTagCompound) obj ) ) );
			}
		}
	}

	public void inventoryChanged()
	{
		try
		{
			this.saveChanges();
			this.gridProxy.getGrid().postEvent( new MENetworkSecurityChange() );
		}
		catch( GridAccessException e )
		{
			// :P
		}
	}

	@MENetworkEventSubscribe
	public void bootUpdate( MENetworkChannelsChanged changed )
	{
		this.markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerUpdate( MENetworkPowerStatusChange changed )
	{
		this.markForUpdate();
	}

	@Override
	public AECableType getCableConnectionType( ForgeDirection dir )
	{
		return AECableType.SMART;
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		this.isActive = false;
	}

	@Override
	public void onReady()
	{
		super.onReady();
		if( Platform.isServer() )
		{
			this.isActive = true;
			MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Register ) );
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		this.isActive = false;
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return new DimensionalCoord( this );
	}

	public boolean isActive()
	{
		return this.isActive;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this.securityMonitor;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return null;
	}

	@Override
	public long getLocatableSerial()
	{
		return this.securityKey;
	}

	public boolean isPowered()
	{
		return this.gridProxy.isActive();
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.cm;
	}

	@Override
	public void updateSetting( IConfigManager manager, Enum settingName, Enum newValue )
	{

	}

	@Override
	public long getSecurityKey()
	{
		return this.securityKey;
	}

	@Override
	public void readPermissions( HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms )
	{
		IPlayerRegistry pr = AEApi.instance().registries().players();

		// read permissions
		for( IAEItemStack ais : this.inventory.storedItems )
		{
			ItemStack is = ais.getItemStack();
			Item i = is.getItem();
			if( i instanceof IBiometricCard )
			{
				IBiometricCard bc = (IBiometricCard) i;
				bc.registerPermissions( new PlayerSecurityWrapper( playerPerms ), pr, is );
			}
		}

		// make sure thea admin is Boss.
		playerPerms.put( this.gridProxy.getNode().getPlayerID(), EnumSet.allOf( SecurityPermissions.class ) );
	}

	@Override
	public boolean isSecurityEnabled()
	{
		return this.isActive && this.gridProxy.isActive();
	}

	@Override
	public int getOwner()
	{
		return this.gridProxy.getNode().getPlayerID();
	}

	@Override
	public AEColor getColor()
	{
		return this.paintedColor;
	}

	@Override
	public boolean recolourBlock( ForgeDirection side, AEColor newPaintedColor, EntityPlayer who )
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
}
