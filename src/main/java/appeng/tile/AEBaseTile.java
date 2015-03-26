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

package appeng.tile;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.ICommonTile;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IOrientable;
import appeng.core.AELog;
import appeng.core.features.ItemStackSrc;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public class AEBaseTile extends TileEntity implements IOrientable, ICommonTile, ICustomNameObject
{

	static private final HashMap<Class, EnumMap<TileEventType, List<AETileEventHandler>>> HANDLERS = new HashMap<Class, EnumMap<TileEventType, List<AETileEventHandler>>>();
	static private final HashMap<Class, ItemStackSrc> ITEM_STACKS = new HashMap<Class, ItemStackSrc>();

	private ForgeDirection forward = ForgeDirection.UNKNOWN;
	private ForgeDirection up = ForgeDirection.UNKNOWN;

	public static final ThreadLocal<WeakReference<AEBaseTile>> DROP_NO_ITEMS = new ThreadLocal<WeakReference<AEBaseTile>>();

	public void disableDrops()
	{
		DROP_NO_ITEMS.set( new WeakReference<AEBaseTile>( this ) );
	}

	public boolean dropItems()
	{
		WeakReference<AEBaseTile> what = DROP_NO_ITEMS.get();
		return what == null || what.get() != this;
	}

	public int renderFragment = 0;
	public String customName;

	public boolean notLoaded()
	{
		return !this.worldObj.blockExists( this.xCoord, this.yCoord, this.zCoord );
	}

	public TileEntity getTile()
	{
		return this;
	}

	static public void registerTileItem(Class c, ItemStackSrc wat)
	{
		ITEM_STACKS.put( c, wat );
	}

	protected ItemStack getItemFromTile(Object obj)
	{
		ItemStackSrc src = ITEM_STACKS.get( obj.getClass() );
		if ( src == null )
			return null;
		return src.stack( 1 );
	}

	protected boolean hasHandlerFor(TileEventType type)
	{
		List<AETileEventHandler> list = this.getHandlerListFor( type );
		return list != null && !list.isEmpty();
	}

	protected List<AETileEventHandler> getHandlerListFor(TileEventType type)
	{
		Class clz = this.getClass();
		EnumMap<TileEventType, List<AETileEventHandler>> handlerSet = HANDLERS.get( clz );

		if ( handlerSet == null )
		{
			HANDLERS.put( clz, handlerSet = new EnumMap<TileEventType, List<AETileEventHandler>>( TileEventType.class ) );

			for (Method m : clz.getMethods())
			{
				TileEvent te = m.getAnnotation( TileEvent.class );
				if ( te != null )
				{
					this.addHandler( handlerSet, te.value(), m );
				}
			}
		}

		List<AETileEventHandler> list = handlerSet.get( type );

		if ( list == null )
			handlerSet.put( type, list = new LinkedList<AETileEventHandler>() );

		return list;
	}

	private void addHandler(EnumMap<TileEventType, List<AETileEventHandler>> handlerSet, TileEventType value, Method m)
	{
		List<AETileEventHandler> list = handlerSet.get( value );

		if ( list == null )
			handlerSet.put( value, list = new ArrayList<AETileEventHandler>() );

		list.add( new AETileEventHandler( m, value ) );
	}

	@Override
	final public boolean canUpdate()
	{
		return this.hasHandlerFor( TileEventType.TICK );
	}

	final public void Tick()
	{

	}

	@Override
	final public void updateEntity()
	{
		for (AETileEventHandler h : this.getHandlerListFor( TileEventType.TICK ))
			h.Tick( this );
	}

	@Override
	public void onChunkUnload()
	{
		if ( !this.isInvalid() )
			this.invalidate();
	}

	/**
	 * for dormant chunk cache.
	 */
	public void onChunkLoad()
	{
		if ( this.isInvalid() )
			this.validate();
	}

	@Override
	// NOTE: WAS FINAL, changed for Immibis
	final public void writeToNBT(NBTTagCompound data)
	{
		super.writeToNBT( data );

		if ( this.canBeRotated() )
		{
			data.setString( "orientation_forward", this.forward.name() );
			data.setString( "orientation_up", this.up.name() );
		}

		if ( this.customName != null )
			data.setString( "customName", this.customName );

		for (AETileEventHandler h : this.getHandlerListFor( TileEventType.WORLD_NBT_WRITE ))
			h.writeToNBT( this, data );
	}

	@Override
	// NOTE: WAS FINAL, changed for Immibis
	final public void readFromNBT(NBTTagCompound data)
	{
		super.readFromNBT( data );

		if ( data.hasKey( "customName" ) )
			this.customName = data.getString( "customName" );
		else
			this.customName = null;

		try
		{
			if ( this.canBeRotated() )
			{
				this.forward = ForgeDirection.valueOf( data.getString( "orientation_forward" ) );
				this.up = ForgeDirection.valueOf( data.getString( "orientation_up" ) );
			}
		}
		catch (IllegalArgumentException ignored)
		{
		}

		for (AETileEventHandler h : this.getHandlerListFor( TileEventType.WORLD_NBT_READ ))
		{
			h.readFromNBT( this, data );
		}
	}

	final public void writeToStream(ByteBuf data)
	{
		try
		{
			if ( this.canBeRotated() )
			{
				byte orientation = (byte) ((this.up.ordinal() << 3) | this.forward.ordinal());
				data.writeByte( orientation );
			}

			for (AETileEventHandler h : this.getHandlerListFor( TileEventType.NETWORK_WRITE ))
				h.writeToStream( this, data );
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}
	}

	final public boolean readFromStream(ByteBuf data)
	{
		boolean output = false;

		try
		{

			if ( this.canBeRotated() )
			{
				ForgeDirection old_Forward = this.forward;
				ForgeDirection old_Up = this.up;

				byte orientation = data.readByte();
				this.forward = ForgeDirection.getOrientation( orientation & 0x7 );
				this.up = ForgeDirection.getOrientation( orientation >> 3 );

				output = this.forward != old_Forward || this.up != old_Up;
			}

			this.renderFragment = 100;
			for (AETileEventHandler h : this.getHandlerListFor( TileEventType.NETWORK_READ ))
				if ( h.readFromStream( this, data ) )
					output = true;

			if ( (this.renderFragment & 1) == 1 )
				output = true;
			this.renderFragment = 0;
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}

		return output;
	}

	/**
	 * By default all blocks can have orientation, this handles saving, and loading, as well as synchronization.
	 *
	 * @return true if tile can be rotated
	 */
	@Override
	public boolean canBeRotated()
	{
		return true;
	}

	@Override
	public ForgeDirection getForward()
	{
		return this.forward;
	}

	@Override
	public ForgeDirection getUp()
	{
		return this.up;
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		this.forward = inForward;
		this.up = inUp;
		this.markForUpdate();
		Platform.notifyBlocksOfNeighbors( this.worldObj, this.xCoord, this.yCoord, this.zCoord );
	}

	public void onPlacement(ItemStack stack, EntityPlayer player, int side)
	{
		if ( stack.hasTagCompound() )
		{
			this.uploadSettings( SettingsFrom.DISMANTLE_ITEM, stack.getTagCompound() );
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound data = new NBTTagCompound();

		ByteBuf stream = Unpooled.buffer();

		try
		{
			this.writeToStream( stream );
			if ( stream.readableBytes() == 0 )
				return null;
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}

		stream.capacity( stream.readableBytes() );
		data.setByteArray( "X", stream.array() );
		return new S35PacketUpdateTileEntity( this.xCoord, this.yCoord, this.zCoord, 64, data );
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		// / pkt.actionType
		if ( pkt.func_148853_f() == 64 )
		{
			ByteBuf stream = Unpooled.copiedBuffer( pkt.func_148857_g().getByteArray( "X" ) );
			if ( this.readFromStream( stream ) )
				this.markForUpdate();
		}
	}

	public void markForUpdate()
	{
		if ( this.renderFragment > 0 )
			this.renderFragment |= 1;
		else
		{
			// TODO: Optimize Network Load
			if ( this.worldObj != null )
			{
				AELog.blockUpdate( this.xCoord, this.yCoord, this.zCoord, this );
				this.worldObj.markBlockForUpdate( this.xCoord, this.yCoord, this.zCoord );
			}
		}
	}

	/**
	 * returns the contents of the tile entity, into the world, defaults to dropping everything in the inventory.
	 *
	 * @param w world
	 * @param x x pos of tile entity
	 * @param y y pos of tile entity
	 * @param z z pos of tile entity
	 * @param drops drops of tile entity
	 */
	@Override
	public void getDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{
		if ( this instanceof IInventory )
		{
			IInventory inv = (IInventory) this;

			for (int l = 0; l < inv.getSizeInventory(); l++)
			{
				ItemStack is = inv.getStackInSlot( l );
				if ( is != null )
					drops.add( is );
			}
		}

	}

	public void getNoDrops(World w, int x, int y, int z, ArrayList<ItemStack> drops)
	{

	}

	public void onReady()
	{

	}

	/**
	 * depending on the from, different settings will be accepted, don't call this with null
	 *
	 * @param from source of settings
	 * @param compound compound of source
	 */
	public void uploadSettings(SettingsFrom from, NBTTagCompound compound)
	{
		if ( compound != null && this instanceof IConfigurableObject )
		{
			IConfigManager cm = ((IConfigurableObject) this).getConfigManager();
			if ( cm != null )
				cm.readFromNBT( compound );
		}

		if ( this instanceof IPriorityHost )
		{
			IPriorityHost pHost = (IPriorityHost) this;
			pHost.setPriority( compound.getInteger( "priority" ) );
		}

		if ( this instanceof ISegmentedInventory )
		{
			IInventory inv = ((ISegmentedInventory) this).getInventoryByName( "config" );
			if ( inv != null && inv instanceof AppEngInternalAEInventory )
			{
				AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
				AppEngInternalAEInventory tmp = new AppEngInternalAEInventory( null, target.getSizeInventory() );
				tmp.readFromNBT( compound, "config" );
				for (int x = 0; x < tmp.getSizeInventory(); x++)
					target.setInventorySlotContents( x, tmp.getStackInSlot( x ) );
			}
		}
	}

	/**
	 * null means nothing to store...
	 *
	 * @param from source of settings
	 * @return compound of source
	 */
	public NBTTagCompound downloadSettings(SettingsFrom from)
	{
		NBTTagCompound output = new NBTTagCompound();

		if ( this.hasCustomName() )
		{
			NBTTagCompound dsp = new NBTTagCompound();
			dsp.setString( "Name", this.getCustomName() );
			output.setTag( "display", dsp );
		}

		if ( this instanceof IConfigurableObject )
		{
			IConfigManager cm = ((IConfigurableObject) this).getConfigManager();
			if ( cm != null )
				cm.writeToNBT( output );
		}

		if ( this instanceof IPriorityHost )
		{
			IPriorityHost pHost = (IPriorityHost) this;
			output.setInteger( "priority", pHost.getPriority() );
		}

		if ( this instanceof ISegmentedInventory )
		{
			IInventory inv = ((ISegmentedInventory) this).getInventoryByName( "config" );
			if ( inv != null && inv instanceof AppEngInternalAEInventory )
			{
				((AppEngInternalAEInventory) inv).writeToNBT( output, "config" );
			}
		}

		return output.hasNoTags() ? null : output;
	}

	public void securityBreak()
	{
		this.worldObj.func_147480_a( this.xCoord, this.yCoord, this.zCoord, true );
		this.disableDrops();
	}

	public void saveChanges()
	{
		super.markDirty();
	}

	public boolean requiresTESR()
	{
		return false;
	}

	public void setName(String name)
	{
		this.customName = name;
	}

	@Override
	public String getCustomName()
	{
		return this.hasCustomName() ? this.customName : this.getClass().getSimpleName();
	}

	@Override
	public boolean hasCustomName()
	{
		return this.customName != null && this.customName.length() > 0;
	}

}
