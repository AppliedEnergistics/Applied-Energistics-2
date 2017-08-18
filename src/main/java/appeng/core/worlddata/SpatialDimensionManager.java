
package appeng.core.worlddata;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
	public int createNewCellStorage( EntityPlayer owner )
	{
		int newId = this.getNextId();

		StorageCellData data = new StorageCellData();
		data.offset = this.getBlockPosFromId( newId );
		if( owner != null )
		{
			data.owner = owner.getPersistentID();
		}

		this.initCellArea( data );

		this.spatialData.put( newId, data );

		return newId;
	}

	@Override
	public void deleteCellStorage( int cellStorageId )
	{
		// TODO cleanup blocks
		StorageCellData removed = spatialData.remove( cellStorageId );
		if( removed != null )
		{
			this.clearCellArea( removed );
		}
	}

	@Override
	public UUID getCellStorageOwner( int cellStorageId )
	{
		StorageCellData cell = this.spatialData.get( cellStorageId );
		if( cell != null )
		{
			return cell.owner;
		}
		return null;
	}

	@Override
	public BlockPos getCellStorageOffset( int cellStorageId )
	{
		StorageCellData cell = this.spatialData.get( cellStorageId );
		if( cell != null )
		{
			return cell.offset;
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
		// TODO
		return this.spatialData.keySet().stream().max( Integer::compare ).orElse( -1 ) + 1;
	}

	private BlockPos getBlockPosFromId( int id )
	{
		// TODO need a better algorithm
		return new BlockPos( MAX_CELL_DIMENSION * id, 64, 0 );
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
		public BlockPos offset;
		public UUID owner;

		@Override
		public NBTTagCompound serializeNBT()
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger( "offset_x", offset.getX() );
			nbt.setInteger( "offset_y", offset.getY() );
			nbt.setInteger( "offset_z", offset.getZ() );
			if( owner != null )
			{
				nbt.setUniqueId( "owner", owner );
			}
			return nbt;
		}

		@Override
		public void deserializeNBT( NBTTagCompound nbt )
		{
			offset = new BlockPos( nbt.getInteger( "offset_x" ), nbt.getInteger( "offset_y" ), nbt.getInteger( "offset_z" ) );
			if( nbt.hasKey( "owner" ) )
			{
				owner = nbt.getUniqueId( "owner" );
			}
		}
	}
}
