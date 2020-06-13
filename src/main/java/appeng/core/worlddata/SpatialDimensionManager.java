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
import appeng.spatial.StorageCellDimension;
import appeng.spatial.StorageCellModDimension;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;


public class SpatialDimensionManager implements ISpatialDimension
{

	private static final String DIM_ID_PREFIX = "storage_cell_";

	@Override
	public ServerWorld getWorld(DimensionType cellDim) {
		return DimensionManager.getWorld(getServer(), cellDim, true, true);
	}

	@Override
	public DimensionType createNewCellDimension(BlockPos contentSize)
	{
		ResourceLocation dimKey = findFreeDimensionId();
		AELog.info("Allocating storage cell dimension '%s' for %d", dimKey);

		PacketBuffer extraData = new PacketBuffer(Unpooled.buffer());
		extraData.writeBlockPos(contentSize);

		return DimensionManager.registerDimension(dimKey, StorageCellModDimension.INSTANCE, extraData, true);
	}

	/**
	 * Tries finding the next free storage cell dimension ID based on the currently registered storage
	 * cell dimensions.
	 */
	private ResourceLocation findFreeDimensionId() {
		int maxId = 0;
		for (DimensionType dimensionType : DimensionType.getAll()) {
			ResourceLocation regName = dimensionType.getRegistryName();
			if (regName == null || !AppEng.MOD_ID.equals(regName.getNamespace())) {
				continue;
			}

			String path = regName.getPath();
			if (!path.startsWith(DIM_ID_PREFIX)) {
				continue;
			}

			try {
				String numericIdPart = path.substring(DIM_ID_PREFIX.length());
				maxId = Math.max(Integer.parseUnsignedInt(numericIdPart), maxId);
			} catch (NumberFormatException e) {
				AELog.warn("Unparsable storage cell dimension id '%s'", path, e);
			}
		}

		++maxId;

		return new ResourceLocation(AppEng.MOD_ID, DIM_ID_PREFIX + maxId);
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
	public BlockPos getCellDimensionOrigin( DimensionType cellDim )
	{
		return StorageCellDimension.REGION_CENTER;
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

}
