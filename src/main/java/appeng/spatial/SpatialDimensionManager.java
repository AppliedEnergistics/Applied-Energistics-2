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

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SpatialDimensionManager {

    public static final RegistryKey<DimensionType> STORAGE_DIMENSION_TYPE = RegistryKey
            .func_240903_a_(Registry.DIMENSION_TYPE_KEY, AppEng.makeId("storage_cell"));

    public static final RegistryKey<Dimension> STORAGE_DIMENSION = RegistryKey
            .func_240903_a_(Registry.DIMENSION_KEY, AppEng.makeId("storage_cell"));

    public static final RegistryKey<World> WORLD_ID = RegistryKey
            .func_240903_a_(Registry.WORLD_KEY, AppEng.makeId("storage_cell"));

    public static final SpatialDimensionManager INSTANCE = new SpatialDimensionManager();

    private SpatialDimensionManager() {
    }

    public static List<Map.Entry<RegistryKey<Dimension>, Dimension>> removeDimension(List<Map.Entry<RegistryKey<Dimension>, Dimension>> dimensions) {
        dimensions.removeIf(e -> e.getKey().equals(STORAGE_DIMENSION));
        return dimensions;
    }

    /**
     * Gets the world used to store spatial storage cell's content.
     */
    public ServerWorld getWorld() {
        MinecraftServer server = getServer();
        ServerWorld world = server.getWorld(WORLD_ID);
        if (world == null) {
            throw new IllegalStateException("The storage cell world is missing.");
        }
        return world;
    }

    private StorageCellWorldData getWorldData() {
        return getWorld().getChunkProvider().getSavedData().getOrCreate(StorageCellWorldData::new, StorageCellWorldData.ID);
    }

    @Nullable
    public StorageCellLot getLot(int lotId) {
        if (lotId == -1) {
            return null;
        }
        return getWorldData().getLotById(lotId);
    }

    public StorageCellLot allocateLot(BlockPos size, int ownerId) {
        StorageCellLot lot = getWorldData().allocateLot(size, ownerId);
        AELog.info("Allocating storage cell lot %d with size %s for %d", lot.getId(), size, ownerId);
        return lot;
    }

    public void freeLot(int lotId) {
        // TODO: Should probably clear the items
        AELog.info("Freeing storage cell lot %d", lotId);
        getWorldData().removeLot(lotId);
    }

    public void addLotTooltip(int lotId, List<ITextComponent> lines) {
        StorageCellLot lot = getLot(lotId);
        if (lot == null) {
            return;
        }

        // Add the actual stored size
        BlockPos size = lot.getSize();
        lines.add(GuiText.StoredSize.text(size.getX(), size.getY(), size.getZ()));

        // Add a serial number to allows players to keep different cells apart
        // Try to make this a little more flavorful.
        String serialNumber = String.format(Locale.ROOT, "SP-%04d", lotId);
        lines.add(GuiText.SerialNumber.text(serialNumber));
    }

    private static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

}
