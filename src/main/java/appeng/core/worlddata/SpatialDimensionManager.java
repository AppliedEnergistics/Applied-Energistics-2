/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.worlddata;


import java.util.HashMap;
import java.util.Map;

import appeng.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import appeng.api.storage.ISpatialDimension;
import appeng.capabilities.Capabilities;


public class SpatialDimensionManager implements ISpatialDimension, ICapabilitySerializable<CompoundNBT>
{
	private static final String NBT_SPATIAL_DATA_KEY = "spatial_data";
	private static final String NBT_SPATIAL_ID_KEY = "id";

	private World world;
	private Map<Integer, StorageCellData> spatialData = new HashMap<>();

	private static final int MAX_CELL_DIMENSION = 512;

	public SpatialDimensionManager( World world )
	{
		this.world = world;
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public int createNewCellDimension( BlockPos contentSize, int owner )
	{
		int newId = this.getNextId();

		StorageCellData data = new StorageCellData();
		data.contentDimension = contentSize;
		data.owner = owner;

		this.spatialData.put( newId, data );

		return newId;
	}

	@Override
	public void deleteCellDimension( int cellStorageId )
	{
		StorageCellData removed = this.spatialData.remove( cellStorageId );
		if( removed != null )
		{
			this.clearCellArea( cellStorageId, removed );
		}
	}

	@Override
	public boolean isCellDimension( int cellStorageId )
	{
		return this.spatialData.containsKey( cellStorageId );
	}

	@Override
	public int getCellDimensionOwner( int cellStorageId )
	{
		StorageCellData cell = this.spatialData.get( cellStorageId );
		if( cell != null )
		{
			return cell.owner;
		}
		return -1;
	}

	@Override
	public BlockPos getCellDimensionOrigin( int cellStorageId )
	{
		if( this.isCellDimension( cellStorageId ) )
		{
			return this.getBlockPosFromId( cellStorageId );
		}
		return null;
	}

	@Override
	public BlockPos getCellContentSize( int cellStorageId )
	{
		StorageCellData cell = this.spatialData.get( cellStorageId );
		if( cell != null )
		{
			return cell.contentDimension;
		}
		return null;
	}

	@Override
	public boolean hasCapability( Capability<?> capability, Direction facing )
	{
		return capability == Capabilities.SPATIAL_DIMENSION;
	}

	@Override
	public <T> T getCapability( Capability<T> capability, Direction facing )
	{
		if( capability == Capabilities.SPATIAL_DIMENSION )
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public CompoundNBT serializeNBT()
	{
		final CompoundNBT ret = new CompoundNBT();
		final ListNBT list = new ListNBT();

		for( Map.Entry<Integer, StorageCellData> entry : this.spatialData.entrySet() )
		{
			final CompoundNBT nbt = entry.getValue().serializeNBT();
			nbt.putInt( NBT_SPATIAL_ID_KEY, entry.getKey() );
			list.add( nbt );
		}
		ret.put( NBT_SPATIAL_DATA_KEY, list );
		return ret;
	}

	@Override
	public void deserializeNBT( CompoundNBT nbt )
	{
		if( nbt.contains( NBT_SPATIAL_DATA_KEY ) )
		{
			final ListNBT list = nbt.getList( NBT_SPATIAL_DATA_KEY, NBTHelper.DEFAULT_LIST_TYPE );

			this.spatialData.clear();
			for( int i = 0; i < list.size(); ++i )
			{
				final CompoundNBT entry = list.getCompound( i );
				final StorageCellData data = new StorageCellData();
				final int id = entry.getInt( NBT_SPATIAL_ID_KEY );
				data.deserializeNBT( entry );
				this.spatialData.put( id, data );
			}
		}
	}

	private int getNextId()
	{
		return this.spatialData.keySet().stream().max( Integer::compare ).orElse( -1 ) + 1;
	}

	private BlockPos getBlockPosFromId( int id )
	{
		int signBits = id & 0b11;
		int offsetBits = id >> 2;
		int offsetScale = 1;
		int posx = MAX_CELL_DIMENSION / 2;
		int posz = MAX_CELL_DIMENSION / 2;

		// find quadrant
		while( offsetBits != 0 )
		{
			posx += MAX_CELL_DIMENSION * offsetScale * ( offsetBits & 0b01 );
			posz += MAX_CELL_DIMENSION * offsetScale * ( offsetBits >> 1 & 0b01 );

			offsetBits >>= 2;
			offsetScale <<= 1;
		}

		// mirror in one of 4 directions
		if( ( signBits & 0b01 ) == 0 )
		{
			posx *= -1;
		}
		if( ( signBits & 0b10 ) == 0 )
		{
			posz *= -1;
		}

		// offset from cell center
		posx -= 64;
		posz -= 64;

		return new BlockPos( posx, 64, posz );
	}

	private void clearCellArea( int cellId, StorageCellData cell )
	{
		// TODO reset chunks?
	}

	private static class StorageCellData implements INBTSerializable<CompoundNBT>
	{
		private static final String NBT_OWNER_KEY = "owner";
		private static final String NBT_DIM_X_KEY = "dim_x";
		private static final String NBT_DIM_Y_KEY = "dim_y";
		private static final String NBT_DIM_Z_KEY = "dim_z";

		public BlockPos contentDimension;
		public int owner;

		@Override
		public CompoundNBT serializeNBT()
		{
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt( NBT_DIM_X_KEY, this.contentDimension.getX() );
			nbt.putInt( NBT_DIM_Y_KEY, this.contentDimension.getY() );
			nbt.putInt( NBT_DIM_Z_KEY, this.contentDimension.getZ() );
			nbt.putInt( NBT_OWNER_KEY, this.owner );
			return nbt;
		}

		@Override
		public void deserializeNBT( CompoundNBT nbt )
		{
			this.contentDimension = new BlockPos( nbt.getInt( NBT_DIM_X_KEY ), nbt.getInt( NBT_DIM_Y_KEY ), nbt.getInt( NBT_DIM_Z_KEY ) );
			this.owner = nbt.getInt( NBT_OWNER_KEY );
		}
	}
}
