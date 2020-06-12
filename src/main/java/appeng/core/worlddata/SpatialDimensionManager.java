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


import appeng.api.storage.ISpatialDimension;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.spatial.StorageCellModDimension;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.server.ServerLifecycleHooks;


public class SpatialDimensionManager implements ISpatialDimension
{
	private static final int MAX_DIM_PER_PLAYER = 999;

	private static final int MAX_CELL_DIMENSION = 512;

	@Override
	public ServerWorld getWorld(DimensionType cellDim) {
		return DimensionManager.getWorld(getServer(), cellDim, true, true);
	}

	@Override
	public DimensionType createNewCellDimension( BlockPos contentSize, int owner )
	{
		// Try to find a free dimension ID for the player
		ResourceLocation dimKey = null;
		for (int i = 1; i <= MAX_DIM_PER_PLAYER; i++) {
			dimKey = new ResourceLocation(AppEng.MOD_ID, "spatial_cell_" + owner + "_" + i);
			if (DimensionType.byName(dimKey) == null) {
				break;
			}
			dimKey = null;
		}
		if (dimKey == null) {
			return null;
		}

		StorageCellData data = new StorageCellData();
		data.contentDimension = contentSize;
		data.owner = owner;

		PacketBuffer extraData = new PacketBuffer(Unpooled.buffer());
		extraData.writeInt(owner);
		extraData.writeBlockPos(contentSize);

		return DimensionManager.registerDimension(dimKey, StorageCellModDimension.INSTANCE, extraData, true);
	}

	@Override
	public void deleteCellDimension( DimensionType cellDim )
	{
		AELog.info("Unregistering storage cell dimension %s", cellDim.getRegistryName());
		MinecraftServer server = getServer();
		ServerWorld world = DimensionManager.getWorld(server, cellDim, false, false);
		if (world != null) {
			DimensionManager.unloadWorld(world);
		}
		DimensionManager.unloadWorlds(server, true);
		DimensionManager.unregisterDimension(cellDim.getId());
	}

	private static MinecraftServer getServer() {
		return ServerLifecycleHooks.getCurrentServer();
	}

	@Override
	public boolean isCellDimension( DimensionType cellDim )
	{
		// Check if the cell dimension type is even registered
		if (cellDim.getRegistryName() == null || !cellDim.getRegistryName().equals(DimensionType.getKey(cellDim))) {
			return false;
		}

		return cellDim.getModType() instanceof StorageCellModDimension;
	}

	@Override
	public int getCellDimensionOwner( DimensionType cellDim )
	{
		if (!(cellDim.getModType() instanceof StorageCellModDimension)) {
			return -1;
		}

		PacketBuffer data = cellDim.getData();
		if (data == null) {
			return -1;
		}
		data.readerIndex(0);
		return data.readInt();
	}

	@Override
	public BlockPos getCellDimensionOrigin( DimensionType cellDim )
	{
		// A region file is 512x512 blocks (32x32 chunks),
		// to avoid creating the 4 regions around 0,0,0,
		// we move the origin to the middle of region 0,0
		return new BlockPos(512 / 2, 61, 512 / 2);
	}

	@Override
	public BlockPos getCellContentSize( DimensionType cellDim )
	{
		if (!(cellDim.getModType() instanceof StorageCellModDimension)) {
			return BlockPos.ZERO;
		}

		PacketBuffer data = cellDim.getData();
		if (data == null) {
			return BlockPos.ZERO;
		}
		data.readerIndex(4);
		return data.readBlockPos();
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
