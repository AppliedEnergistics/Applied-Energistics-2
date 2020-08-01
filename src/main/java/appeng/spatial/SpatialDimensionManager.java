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

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import appeng.api.storage.ISpatialDimension;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.hooks.DynamicDimensions;

public final class SpatialDimensionManager implements ISpatialDimension {

    // A region file is 512x512 blocks (32x32 chunks),
    // to avoid creating the 4 regions around 0,0,0,
    // we move the origin to the middle of region 0,0
    public static final BlockPos REGION_CENTER = new BlockPos(512 / 2, 64, 512 / 2);

    public static final RegistryKey<DimensionType> STORAGE_DIMENSION_TYPE = RegistryKey
            .func_240903_a_(Registry.DIMENSION_TYPE_KEY, AppEng.makeId("storage_cell"));

    public static final ISpatialDimension INSTANCE = new SpatialDimensionManager();

    private static final String DIM_ID_PREFIX = "spatial_";

    private SpatialDimensionManager() {
    }

    @Override
    public ServerWorld getWorld(RegistryKey<World> cellDim) {
        MinecraftServer server = getServer();
        return server.getWorld(cellDim);
    }

    @Override
    public RegistryKey<World> createNewCellDimension(BlockPos size) {
        ResourceLocation dimKey = findFreeDimensionId();
        AELog.info("Allocating storage cell dimension '%s'", dimKey);

        DynamicDimensions dynamicDimensions = (DynamicDimensions) getServer();

        RegistryKey<World> worldId = RegistryKey.func_240903_a_(Registry.WORLD_KEY, dimKey);

        ServerWorld world = dynamicDimensions.addWorld(worldId, STORAGE_DIMENSION_TYPE, StorageChunkGenerator.INSTANCE);

        SpatialDimensionExtraData spatialExtraData = world.getSavedData().getOrCreate(SpatialDimensionExtraData::new,
                SpatialDimensionExtraData.ID);
        spatialExtraData.setSize(size);

        return worldId;
    }

    /**
     * Tries finding the next free storage cell dimension ID based on the currently
     * registered storage cell dimensions.
     */
    private ResourceLocation findFreeDimensionId() {
        int maxId = 0;
        for (RegistryKey<World> worldId : getServer().func_240770_D_()) {
            ResourceLocation regName = worldId.func_240901_a_();
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

        return AppEng.makeId(DIM_ID_PREFIX + maxId);
    }

    @Override
    public void deleteCellDimension(RegistryKey<World> worldId) {
        AELog.info("Unregistering storage cell dimension %s", worldId.func_240901_a_());
        MinecraftServer server = getServer();
// FIXME FABRIC        ServerWorld world = DimensionManager.getWorld(server, worldId, false, false);
// FIXME FABRIC        if (world != null) {
// FIXME FABRIC            DimensionManager.unloadWorld(world);
// FIXME FABRIC        }
// FIXME FABRIC        DimensionManager.unloadWorlds(server, true);
// FIXME FABRIC        DimensionManager.unregisterDimension(worldId.getId());
        throw new IllegalStateException();
    }

    @Override
    public boolean isCellDimension(RegistryKey<World> worldId) {
        ResourceLocation id = worldId.func_240901_a_();
        if (!id.getNamespace().equals(AppEng.MOD_ID)) {
            return false; // World belongs to a different mod
        }
        if (!id.getPath().startsWith(DIM_ID_PREFIX)) {
            return false;
        }

        // Check that the world has the right dimension type
        ServerWorld world = getServer().getWorld(worldId);
        if (world == null) {
            return false; // Non-existent world
        }

        return world.func_234922_V_().equals(STORAGE_DIMENSION_TYPE);
    }

    @Override
    public BlockPos getCellDimensionOrigin(RegistryKey<World> worldId) {
        return REGION_CENTER;
    }

    @Override
    public BlockPos getCellDimensionSize(RegistryKey<World> worldId) {
        SpatialDimensionExtraData extraData = getExtraData(worldId);
        return extraData != null ? extraData.getSize() : BlockPos.ZERO;
    }

    @Override
    public void addCellDimensionTooltip(RegistryKey<World> worldId, List<ITextComponent> lines) {
        // Check if the cell dimension type is even registered
        ResourceLocation registryName = worldId.func_240901_a_();
        if (registryName == null || !AppEng.MOD_ID.equals(registryName.getNamespace())) {
            return;
        }

        if (!registryName.getPath().startsWith(DIM_ID_PREFIX)) {
            return;
        }

        // Add the actual stored size
        BlockPos size = SpatialDimensionManager.INSTANCE.getCellDimensionSize(worldId);
        lines.add(GuiText.StoredSize.text(size.getX(), size.getY(), size.getZ()));

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
        lines.add(GuiText.SerialNumber.text(serialNumber));
    }

    @Nullable
    private SpatialDimensionExtraData getExtraData(RegistryKey<World> worldId) {
        ServerWorld world = getWorld(worldId);
        if (world == null) {
            return null;
        }

        return world.getSavedData().get(SpatialDimensionExtraData::new, SpatialDimensionExtraData.ID);
    }

    private static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

}
