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


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import appeng.core.AELog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.ICommonTile;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IOrientable;
import appeng.core.features.IStackSrc;
import appeng.helpers.ICustomNameObject;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public class AEBaseTile extends TileEntity implements IOrientable, ICommonTile, ICustomNameObject
{

	private static final ThreadLocal<WeakReference<AEBaseTile>> DROP_NO_ITEMS = new ThreadLocal<>();
	private static final Map<Class<? extends TileEntity>, IStackSrc> ITEM_STACKS = new HashMap<>();
	private int renderFragment = 0;
	@Nullable
	private String customName;
	private Direction forward = null;
	private Direction up = null;
	private BlockState state;
	private boolean markDirtyQueued = false;

	public AEBaseTile(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
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
		return !this.world.isBlockLoaded( this.pos );
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
			return ItemStack.EMPTY;
		}
		return src.stack( 1 );
	}

	@Override
	public void read( final CompoundNBT data )
	{
		super.read( data );

		if( data.contains( "customName" ) )
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
				this.forward = Direction.valueOf( data.getString( "forward" ) );
				this.up = Direction.valueOf( data.getString( "up" ) );
			}
		}
		catch( final IllegalArgumentException ignored )
		{
		}
	}

	@Override
	public CompoundNBT write( final CompoundNBT data )
	{
		super.write( data );

		if( this.canBeRotated() )
		{
			data.putString( "forward", this.getForward().name() );
			data.putString( "up", this.getUp().name() );
		}

		if( this.customName != null )
		{
			data.putString( "customName", this.customName );
		}

		return data;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket( this.pos, 64, this.getUpdateTag() );
	}

	@Override
	public void onDataPacket( final NetworkManager net, final SUpdateTileEntityPacket pkt )
	{
		// / pkt.actionType
		if( pkt.getTileEntityType() == 64 )
		{
			this.handleUpdateTag( pkt.getNbtCompound() );
		}
	}

	public void onReady()
	{
	}

	/**
	 * This builds a tag with the actual data that should be sent to the client for update syncs.
	 * If the tile entity doesn't need update syncs, it returns null.
	 */
	private CompoundNBT writeUpdateData()
	{
		final CompoundNBT data = new CompoundNBT();

		final PacketBuffer stream = new PacketBuffer( Unpooled.buffer() );

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
		data.putByteArray( "X", stream.array() );
		return data;
	}

	private boolean readUpdateData( ByteBuf stream )
	{
		boolean output = false;

		try
		{
			this.renderFragment = 100;

			output = this.readFromStream( stream );

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

	/**
	 * Handles tile entites that are being sent to the client as part of a full chunk.
	 */
	@Override
	public CompoundNBT getUpdateTag()
	{
		final CompoundNBT data = this.writeUpdateData();

		if( data == null )
		{
			return new CompoundNBT();
		}

		data.putInt( "x", this.pos.getX() );
		data.putInt( "y", this.pos.getY() );
		data.putInt( "z", this.pos.getZ() );
		return data;
	}

	/**
	 * Handles tile entites that are being received by the client as part of a full chunk.
	 */
	@Override
	public void handleUpdateTag( CompoundNBT tag )
	{
		final ByteBuf stream = Unpooled.copiedBuffer( tag.getByteArray( "X" ) );

		if( this.readUpdateData( stream ) )
		{
			this.markForUpdate();
		}
	}

	protected boolean readFromStream( final ByteBuf data ) throws IOException
	{
		if( this.canBeRotated() )
		{
			final Direction old_Forward = this.forward;
			final Direction old_Up = this.up;

			final byte orientation = data.readByte();
			this.forward = Direction.values()[orientation & 0x7];
			this.up = Direction.values()[orientation >> 3];

			return this.forward != old_Forward || this.up != old_Up;
		}
		return false;
	}

	protected void writeToStream( final PacketBuffer data ) throws IOException
	{
		if( this.canBeRotated() )
		{
			final byte orientation = (byte) ( ( this.up.ordinal() << 3 ) | this.forward.ordinal() );
			data.writeByte( orientation );
		}
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
			if( this.world != null )
			{
				AELog.blockUpdate( this.pos, this );
				this.world.notifyBlockUpdate( this.pos, this.getBlockState(), this.getBlockState(), 3 );
			}
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

	@Override
	public Direction getForward()
	{
		if( this.forward == null )
		{
			return Direction.NORTH;
		}
		return this.forward;
	}

	@Override
	public Direction getUp()
	{
		if( this.up == null )
		{
			return Direction.UP;
		}
		return this.up;
	}

	@Override
	public void setOrientation( final Direction inForward, final Direction inUp )
	{
		this.forward = inForward;
		this.up = inUp;
		this.markForUpdate();
		Platform.notifyBlocksOfNeighbors( this.world, this.pos );
	}

	public void onPlacement( final ItemStack stack, final PlayerEntity player, final Direction side )
	{
		if( stack.hasTag() )
		{
			this.uploadSettings( SettingsFrom.DISMANTLE_ITEM, stack.getTag() );
		}
	}

	/**
	 * depending on the from, different settings will be accepted, don't call this with null
	 *
	 * @param from source of settings
	 * @param compound compound of source
	 */
	public void uploadSettings( final SettingsFrom from, final CompoundNBT compound )
	{
		if( this instanceof IConfigurableObject )
		{
			final IConfigManager cm = ( (IConfigurableObject) this ).getConfigManager();
			if( cm != null )
			{
				cm.readFromNBT( compound );
			}
		}
// FIXME
//		if( this instanceof IPriorityHost )
//		{
//			final IPriorityHost pHost = (IPriorityHost) this;
//			pHost.setPriority( compound.getInt( "priority" ) );
//		}
//
//		if( this instanceof ISegmentedInventory )
//		{
//			final IItemHandler inv = ( (ISegmentedInventory) this ).getInventoryByName( "config" );
//			if( inv instanceof AppEngInternalAEInventory )
//			{
//				final AppEngInternalAEInventory target = (AppEngInternalAEInventory) inv;
//				final AppEngInternalAEInventory tmp = new AppEngInternalAEInventory( null, target.getSlots() );
//				tmp.readFromNBT( compound, "config" );
//				for( int x = 0; x < tmp.getSlots(); x++ )
//				{
//					target.setStackInSlot( x, tmp.getStackInSlot( x ) );
//				}
//			}
//		}
	}

	/**
	 * returns the contents of the tile entity, into the world, defaults to dropping everything in the inventory.
	 *
	 * @param w world
	 * @param pos block position
	 * @param drops drops of tile entity
	 */
	@Override
	public void getDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{

	}

	public void getNoDrops( final World w, final BlockPos pos, final List<ItemStack> drops )
	{

	}

	/**
	 * null means nothing to store...
	 *
	 * @param from source of settings
	 *
	 * @return compound of source
	 */
	public CompoundNBT downloadSettings( final SettingsFrom from )
	{
		final CompoundNBT output = new CompoundNBT();

		if( this.hasCustomInventoryName() )
		{
			final CompoundNBT dsp = new CompoundNBT();
			dsp.putString( "Name", this.getCustomInventoryName() );
			output.put( "display", dsp );
		}

		if( this instanceof IConfigurableObject )
		{
			final IConfigManager cm = ( (IConfigurableObject) this ).getConfigManager();
			if( cm != null )
			{
				cm.writeToNBT( output );
			}
		}

// FIXME
//		if( this instanceof IPriorityHost )
//		{
//			final IPriorityHost pHost = (IPriorityHost) this;
//			output.putInt( "priority", pHost.getPriority() );
//		}
//
//		if( this instanceof ISegmentedInventory )
//		{
//			final IItemHandler inv = ( (ISegmentedInventory) this ).getInventoryByName( "config" );
//			if( inv instanceof AppEngInternalAEInventory )
//			{
//				( (AppEngInternalAEInventory) inv ).writeToNBT( output, "config" );
//			}
//		}

		return output.isEmpty() ? null : output;
	}

	@Override
	public String getCustomInventoryName()
	{
		return this.hasCustomInventoryName() ? this.customName : this.getClass().getSimpleName();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return this.customName != null && this.customName.length() > 0;
	}

	public void securityBreak()
	{
		this.world.destroyBlock( this.pos, true );
		this.disableDrops();
	}

	public void disableDrops()
	{
		DROP_NO_ITEMS.set( new WeakReference<>( this ) );
	}

	public void saveChanges()
	{
		if( this.world != null )
		{
			this.world.markChunkDirty( this.pos, this );
			if( !this.markDirtyQueued )
			{
				// FIXME TickHandler.INSTANCE.addCallable( null, this::markDirtyAtEndOfTick );
				this.markDirtyQueued = true;
			}
		}
	}

	private Object markDirtyAtEndOfTick( final World w )
	{
		this.markDirty();
		this.markDirtyQueued = false;
		return null;
	}

	public void setName( final String name )
	{
		this.customName = name;
	}
}
