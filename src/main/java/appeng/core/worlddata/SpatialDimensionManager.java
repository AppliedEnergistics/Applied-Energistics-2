
package appeng.core.worlddata;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import appeng.api.storage.ISpatialDimension;
import appeng.capabilities.Capabilities;


public class SpatialDimensionManager implements ISpatialDimension, ICapabilitySerializable<NBTTagCompound>
{
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
	public int createNewCellDimension( Vec3i contentSize, int owner )
	{
		int newId = this.getNextId();

		StorageCellData data = new StorageCellData();
		data.contentDimension = contentSize;
		data.owner = owner;

		this.initCellArea( data );

		this.spatialData.put( newId, data );

		return newId;
	}

	@Override
	public void deleteCellDimension( int cellStorageId )
	{
		// TODO cleanup blocks
		StorageCellData removed = spatialData.remove( cellStorageId );
		if( removed != null )
		{
			this.clearCellArea( removed );
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
			return getBlockPosFromId( cellStorageId );
		}
		return null;
	}

	@Override
	public Vec3i getCellContentSize( int cellStorageId )
	{
		StorageCellData cell = this.spatialData.get( cellStorageId );
		if( cell != null )
		{
			return cell.contentDimension;
		}
		return null;
	}

	@Override
	public boolean hasCapability( Capability<?> capability, EnumFacing facing )
	{
		return capability == Capabilities.SPATIAL_DIMENSION;
	}

	@Override
	public <T> T getCapability( Capability<T> capability, EnumFacing facing )
	{
		if( capability == Capabilities.SPATIAL_DIMENSION )
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		final NBTTagCompound ret = new NBTTagCompound();
		final NBTTagList list = new NBTTagList();

		for( Map.Entry<Integer, StorageCellData> entry : this.spatialData.entrySet() )
		{
			final NBTTagCompound nbt = entry.getValue().serializeNBT();
			nbt.setInteger( "id", entry.getKey() );
			list.appendTag( nbt );
		}
		ret.setTag( "spatial_data", list );
		return ret;
	}

	@Override
	public void deserializeNBT( NBTTagCompound nbt )
	{
		if( nbt.hasKey( "spatial_data" ) )
		{
			final NBTTagList list = (NBTTagList) nbt.getTag( "spatial_data" );

			this.spatialData.clear();
			for( int i = 0; i < list.tagCount(); ++i )
			{
				final NBTTagCompound entry = list.getCompoundTagAt( i );
				final StorageCellData data = new StorageCellData();
				final int id = entry.getInteger( "id" );
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
		posx *= ( signBits & 0x01 ) - 1;
		posz *= ( signBits >> 1 & 0x01 ) - 1;

		// offset from cell center
		posx -= 64;
		posz -= 64;

		return new BlockPos( posx, 64, posz );
	}

	private void initCellArea( StorageCellData cell )
	{
		// TODO lets build a wall
	}

	private void clearCellArea( StorageCellData cell )
	{
		// TODO clear all blocks in area?
	}

	private static class StorageCellData implements INBTSerializable<NBTTagCompound>
	{
		public Vec3i contentDimension;
		public int owner;

		@Override
		public NBTTagCompound serializeNBT()
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger( "dim_x", contentDimension.getX() );
			nbt.setInteger( "dim_y", contentDimension.getY() );
			nbt.setInteger( "dim_z", contentDimension.getZ() );
			nbt.setInteger( "owner", owner );
			return nbt;
		}

		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			contentDimension = new Vec3i( nbt.getInteger( "dim_x" ), nbt.getInteger( "dim_y" ), nbt.getInteger( "dim_z" ) );
			owner = nbt.getInteger( "owner" );
		}
	}
}
