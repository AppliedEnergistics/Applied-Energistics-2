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

package appeng.spatial;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import appeng.api.storage.ISpatialDimension;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;

public final class SpatialDimensionManager implements ISpatialDimension {

    public static final ISpatialDimension INSTANCE = new SpatialDimensionManager();

    private static final String DIM_ID_PREFIX = "spatial_";

    private SpatialDimensionManager() {
    }

    @Override
    public ServerWorld getWorld(DimensionType cellDim) {
        return DimensionManager.getWorld(getServer(), cellDim, true, true);
    }

    @Override
    public DimensionType createNewCellDimension(BlockPos size) {
        ResourceLocation dimKey = findFreeDimensionId();
        AELog.info("Allocating storage cell dimension '%s'", dimKey);

        PacketBuffer extraData = new SpatialDimensionExtraData(size).write();

        return DimensionManager.registerDimension(dimKey, StorageCellModDimension.INSTANCE, extraData, true);
    }

    /**
     * Tries finding the next free storage cell dimension ID based on the currently
     * registered storage cell dimensions.
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
    public void deleteCellDimension(DimensionType cellDim) {
        AELog.info("Unregistering storage cell dimension %s", cellDim.getRegistryName());
        MinecraftServer server = getServer();
        ServerWorld world = DimensionManager.getWorld(server, cellDim, false, false);
        if (world != null) {
            DimensionManager.unloadWorld(world);
        }
        DimensionManager.unloadWorlds(server, true);
        DimensionManager.unregisterDimension(cellDim.getId());
    }

    @Override
    public boolean isCellDimension(DimensionType cellDim) {
        // Check if the cell dimension type is even registered
        if (cellDim.getRegistryName() == null || !cellDim.getRegistryName().equals(DimensionType.getKey(cellDim))) {
            return false;
        }

        return cellDim.getModType() instanceof StorageCellModDimension;
    }

    @Override
    public BlockPos getCellDimensionOrigin(DimensionType cellDim) {
        return StorageCellDimension.REGION_CENTER;
    }

    @Override
    public BlockPos getCellDimensionSize(DimensionType cellDim) {
        SpatialDimensionExtraData extraData = getExtraData(cellDim);
        return extraData != null ? extraData.getSize() : BlockPos.ZERO;
    }

    @Override
    public void addCellDimensionTooltip(DimensionType cellDim, List<ITextComponent> lines) {
        // Check if the cell dimension type is even registered
        ResourceLocation registryName = cellDim.getRegistryName();
        if (registryName == null || !AppEng.MOD_ID.equals(registryName.getNamespace())) {
            return;
        }

        if (!registryName.getPath().startsWith(DIM_ID_PREFIX)) {
            return;
        }

        // Add the actual stored size
        BlockPos size = SpatialDimensionManager.INSTANCE.getCellDimensionSize(cellDim);
        lines.add(GuiText.StoredSize.textComponent(size.getX(), size.getY(), size.getZ()));

        // Add a serial number to allows players to keep different cells apart
        int dimId;
        try {
            String numericIdPart = registryName.getPath().substring(DIM_ID_PREFIX.length());
            dimId = Integer.parseUnsignedInt(numericIdPart);
        } catch (NumberFormatException ignored) {
            return;
        }

        // Try to make this a little more flavorful.
        String serialNumber = String.format(Locale.ROOT, "SP-%04d", dimId);
        lines.add(GuiText.SerialNumber.textComponent(serialNumber));
    }

    @Nullable
    private SpatialDimensionExtraData getExtraData(DimensionType cellDim) {
        if (!(cellDim.getModType() instanceof StorageCellModDimension)) {
            return null;
        }
        return SpatialDimensionExtraData.read(cellDim.getData());
    }

    private static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

}
