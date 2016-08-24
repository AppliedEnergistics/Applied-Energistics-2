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
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.util.ICommonTile;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IOrientable;
import appeng.core.AELog;
import appeng.core.features.IStackSrc;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public class AEBaseTile extends TileEntity implements ITickable, IOrientable, ICommonTile, ICustomNameObject
{

	private static final ThreadLocal<WeakReference<AEBaseTile>> DROP_NO_ITEMS = new ThreadLocal<WeakReference<AEBaseTile>>();
	private static final Map<Class<? extends AEBaseTile>, Map<TileEventType, List<AETileEventHandler>>> HANDLERS = new HashMap<Class<? extends AEBaseTile>, Map<TileEventType, List<AETileEventHandler>>>();
	private static final Map<Class<? extends TileEntity>, IStackSrc> ITEM_STACKS = new HashMap<>();
	private int renderFragment = 0;
	@Nullable
	private String customName;
	private EnumFacing forward = null;
	private EnumFacing up = null;

	private IBlockState state;

	@Override
	public boolean shouldRefresh( final World world, final BlockPos pos, final IBlockState oldState, final IBlockState newSate )
	{
		return newSate.getBlock() != oldState.getBlock(); // state dosn't change tile entities in AE2.
	}

	public static void registerTileItem( final Class<? extends TileEntity> c, final IStackSrc wat )
	{
		ITEM_STACKS.put( c, wat );
	}

	public boolean dropItems()
	{
		final WeakReference<AEBaseTile> what = DROP_NO_ITEMS.get();
		return what == null || what.get() != this;
	}

	public boolean notLoaded()
	{
		return !this.worldObj.isBlockLoaded( this.pos );
	}

	@Nonnull
	public TileEntity getTile()
	{
		return this;
	}

	@Nullable
	protected ItemStack getItemFromTile( final Object obj )
	{
		final IStackSrc src = ITEM_STACKS.get( obj.getClass() );
		if( src == null )
		{
			return null;
		}
		return src.stack( 1 );
	}

	@Nonnull
	public IBlockState getBlockState(){
		if( state == null )
		{
			state = worldObj.getBlockState( getPos() );
		}
		return state;
	}
	
	/**
	 * for dormant chunk cache.
	 */
	public void onChunkLoad()
	{
		if( this.isInvalid() )
		{
			this.validate();
		}
	}

	@Override
	// NOTE: WAS FINAL, changed for Immibis
	public final void readFromNBT( final NBTTagCompound data )
	{
		super.readFromNBT( data );

		if( data.hasKey( "customName" ) )
		{
			this.customName = data.getString( "customName" );
		}
		else
		{
			this.customName = null;
		}

		try
		{
			if( this.canBeRotated() )
			{
				this.forward = EnumFacing.valueOf( data.getString( "forward" ) );
				this.up = EnumFacing.valueOf( data.getString( "up" ) );
			}
		}
		catch( final IllegalArgumentException ignored )
		{
		}

		for( final AETileEventHandler h : this.getHandlerListFor( TileEventType.WORLD_NBT_READ ) )
		{
			h.readFromNBT( this, data );
		}
	}

	@Override
	// NOTE: WAS FINAL, changed for Immibis
	public final NBTTagCompound writeToNBT( final NBTTagCompound data )
	{
		super.writeToNBT( data );

		if( this.canBeRotated() )
		{
			data.setString( "forward", this.getForward().name() );
			data.setString( "up", this.getUp().name() );
		}

		if( this.customName != null )
		{
			data.setString( "customName", this.customName );
		}

		for( final AETileEventHandler h : this.getHandlerListFor( TileEventType.WORLD_NBT_WRITE ) )
		{
			h.writeToNBT( this, data );
		}

		return data;
	}

	public final void update()
	{
		for( final AETileEventHandler h : this.getHandlerListFor( TileEventType.TICK ) )
		{
			h.tick( this );
		}
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		final NBTTagCompound data = new NBTTagCompound();

		final ByteBuf stream = Unpooled.buffer();

		try
		{
			this.writeToStream( stream );
			if( stream.readableBytes() == 0 )
			{
				return null;
			}
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}

		stream.capacity( stream.readableBytes() );
		data.setByteArray( "X", stream.array() );
		return new SPacketUpdateTileEntity( this.pos, 64, data );
	}

	private boolean hasHandlerFor( final TileEventType type )
	{
		final List<AETileEventHandler> list = this.getHandlerListFor( type );

		return !list.isEmpty();
	}

	@Override
	public void onDataPacket( final NetworkManager net, final SPacketUpdateTileEntity pkt )
	{
		// / pkt.actionType
		if( pkt.getTileEntityType() == 64 )
		{
			final ByteBuf stream = Unpooled.copiedBuffer( pkt.getNbtCompound().getByteArray( "X" ) );
			if( this.readFromStream( stream ) )
			{
				this.markForUpdate();
			}
		}
	}

	@Override
	public void onChunkUnload()
	{
		if( !this.isInvalid() )
		{
			this.invalidate();
		}
	}

	private final boolean readFromStream( final ByteBuf data )
	{
		boolean output = false;

		try
		{

			if( this.canBeRotated() )
			{
				final EnumFacing old_Forward = this.forward;
				final EnumFacing old_Up = this.up;

				final byte orientation = data.readByte();
				this.forward = EnumFacing.VALUES[orientation & 0x7];
				this.up = EnumFacing.VALUES[orientation >> 3];

				output = this.forward != old_Forward || this.up != old_Up;
			}

			this.renderFragment = 100;
			for( final AETileEventHandler h : this.getHandlerListFor( TileEventType.NETWORK_READ ) )
			{
				if( h.readFromStream( this, data ) )
				{
					output = true;
				}
			}

			if( ( this.renderFragment & 1 ) == 1 )
			{
				output = true;
			}
			this.renderFragment = 0;
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}

		return output;
	}

	public void markForUpdate()
	{
		if( this.renderFragment > 0 )
		{
			this.renderFragment |= 1;
		}
		else
		{
			// TODO: Optimize Network Load
			if( this.worldObj != null )
			{
				AELog.blockUpdate( this.pos, this );
				this.worldObj.notifyBlockUpdate( this.pos, getBlockState(), getBlockState(), 3 );
			}
		}
	}

	private final void writeToStream( final ByteBuf data )
	{
		try
		{
			if( this.canBeRotated() )
			{
				final byte orientation = (byte) ( ( this.up.ordinal() << 3 ) | this.forward.ordinal() );
				data.writeByte( orientation );
			}

			for( final AETileEventHandler h : this.getHandlerListFor( TileEventType.NETWORK_WRITE ) )
			{
				h.writeToStream( this, data );
			}
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
		}
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

	@Nonnull
	private List<AETileEventHandler> getHandlerListFor( final TileEventType type )
	{
		final Map<TileEventType, List<AETileEventHandler>> eventToHandlers = this.getEventToHandlers();
		final List<AETileEventHandler> handlers = this.getHandlers( eventToHandlers, type );

		return handlers;
	}

	@Nonnull
	private Map<TileEventType, List<AETileEventHandler>> getEventToHandlers()
	{
		final Class<? extends AEBaseTile> clazz = this.getClass();
		final Map<TileEventType, List<AETileEventHandler>> storedHandlers = HANDLERS.get( clazz );

		if( storedHandlers == null )
		{
			final Map<TileEventType, List<AETileEventHandler>> newStoredHandlers = new EnumMap<TileEventType, List<AETileEventHandler>>( TileEventType.class );

			HANDLERS.put( clazz, newStoredHandlers );

			for( final Method method : clazz.getMethods() )
			{
				final TileEvent event = method.getAnnotation( TileEvent.class );
				if( event != null )
				{
					this.addHandler( newStoredHandlers, event.value(), method );
				}
			}

			return newStoredHandlers;
		}
		else
		{
			return storedHandlers;
		}
	}

	@Nonnull
	private List<AETileEventHandler> getHandlers( final Map<TileEventType, List<AETileEventHandler>> eventToHandlers, final TileEventType event )
	{
		final List<AETileEventHandler> oldHandlers = eventToHandlers.get( event );

		if( oldHandlers == null )
		{
			final List<AETileEventHandler> newHandlers = new LinkedList<AETileEventHandler>();
			eventToHandlers.put( event, newHandlers );

			return newHandlers;
		}
		else
		{
			return oldHandlers;
		}
	}

	private void addHandler( final Map<TileEventType, List<AETileEventHandler>> handlerSet, final TileEventType value, final Method m )
	{
		List<AETileEventHandler> list = handlerSet.get( value );

		if( list == null )
		{
			list = new ArrayList<AETileEventHandler>();
			handlerSet.put( value, list );
		}

		list.add( new AETileEventHandler( m ) );
	}

	@Override
	public EnumFacing getForward()
	{
		if( this.forward == null )
		{
			return EnumFacing.NORTH;
		}
		return this.forward;
	}

	@Override
	public EnumFacing getUp()
	{
		if( this.up == null )
		{
			return EnumFacing.UP;
		}
		return this.up;
	}

	@Override
	public void setOrientation( final EnumFacing inForward, final EnumFacing inUp )
	{
		this.forward = inForward;
		this.up = inUp;
		this.markForUpdate();
		Platform.notifyBlocksOfNeighbors( this.worldObj, this.pos );
	}

	public void onPlacement( final ItemStack stack, final EntityPlayer player, final EnumFacing side )
	{
		if( stack.hasTagCompound() )
		{
			this.uploadSettings( SettingsFrom.DISMANTLE_ITEM, stack.getTagCompound() );
		}
	}

	/**
	 * depending on the from, different settings will be accepted, don't call this with null
	 *
	 * @param from source of settings
	 * @param compound compound of source
	 */
	public void uploadSettings( final SettingsFrom from, final NBTTagCompound compound )
	{
		if( compound != null && this instanceof IConfigurableObject )
		{
			final IConfigManager cm = ( (IConfigurableObject) this ).getConfigManager();
			if( cm != null )
			{
				cm.readFromNBT( compound );
			}
		}

		if( this instanceof IPriorityHost )
		{
			final IPriorityHost pHost = (IPriorityHost) this;
			pHost.setPriority( compound.getInteger( "priority" ) );
		}

		if( this instanceof ISegmentedInventory )
		{
			final IInventory inv = ( (ISegmentedInventory) this ).getInventoryByName( "config" );
			if( inv instanceof AppEngInternalAEInventory )
			{
				final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
				final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory( null, target.getSizeInventory() );
				tmp.readFromNBT( compound, "config" );
				for( int x = 0; x < tmp.getSizeInventory(); x++ )
				{
					target.setInventorySlotContents( x, tmp.getStackInSlot( x ) );
				}
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
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{
		if( this instanceof IInventory )
		{
			final IInventory inv = (IInventory) this;

			for( int l = 0; l < inv.getSizeInventory(); l++ )
			{
				final ItemStack is = inv.getStackInSlot( l );
				if( is != null )
				{
					drops.add( is );
				}
			}
		}
	}

	public void getNoDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{

	}

	public void onReady()
	{

	}

	/**
	 * null means nothing to store...
	 *
	 * @param from source of settings
	 *
	 * @return compound of source
	 */
	public NBTTagCompound downloadSettings( final SettingsFrom from )
	{
		final NBTTagCompound output = new NBTTagCompound();

		if( this.hasCustomName() )
		{
			final NBTTagCompound dsp = new NBTTagCompound();
			dsp.setString( "Name", this.getCustomName() );
			output.setTag( "display", dsp );
		}

		if( this instanceof IConfigurableObject )
		{
			final IConfigManager cm = ( (IConfigurableObject) this ).getConfigManager();
			if( cm != null )
			{
				cm.writeToNBT( output );
			}
		}

		if( this instanceof IPriorityHost )
		{
			final IPriorityHost pHost = (IPriorityHost) this;
			output.setInteger( "priority", pHost.getPriority() );
		}

		if( this instanceof ISegmentedInventory )
		{
			final IInventory inv = ( (ISegmentedInventory) this ).getInventoryByName( "config" );
			if( inv instanceof AppEngInternalAEInventory )
			{
				( (AppEngInternalAEInventory) inv ).writeToNBT( output, "config" );
			}
		}

		return output.hasNoTags() ? null : output;
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

	public void securityBreak()
	{
		this.worldObj.destroyBlock( this.pos, true );
		this.disableDrops();
	}

	public void disableDrops()
	{
		DROP_NO_ITEMS.set( new WeakReference<AEBaseTile>( this ) );
	}

	public void saveChanges()
	{
		super.markDirty();
	}

	public boolean requiresTESR()
	{
		return false;
	}

	public void setName( final String name )
	{
		this.customName = name;
	}
}
