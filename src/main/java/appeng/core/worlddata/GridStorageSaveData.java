/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.me.GridStorage;

/**
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
public final class GridStorageSaveData extends SavedData implements IGridStorageSaveData {

    public static final String NAME = AppEng.MOD_ID + "_storage";

    private static final String TAG_NEXT_ID = "nextId";
    public static final String TAG_STORAGE = "storage";

    private final Map<Long, GridStorage> storage = new HashMap<>();

    // id that will be assigned to the next new grid, this needs to be persisted
    // because grids can be removed and we do not want to re-assign the same id
    // twice
    private long nextGridId;

    /**
     * lazy loading, can load any id, even ones that don't exist anymore.
     *
     * @param storageID ID of grid storage
     * @return corresponding grid storage
     */
    @Override
    public GridStorage getGridStorage(long storageID) {
        GridStorage result = storage.get(storageID);

        if (result == null) {
            result = new GridStorage(storageID);
            storage.put(storageID, result);
        }

        return result;
    }

    /**
     * create a new storage
     */
    @Nonnull
    @Override
    public GridStorage getNewGridStorage() {
        return getGridStorage(nextGridId++);
    }

    @Override
    public void destroyGridStorage(long id) {
        this.storage.remove(id);
    }

    public static GridStorageSaveData load(CompoundTag tag) {

        var result = new GridStorageSaveData();

        result.nextGridId = tag.getLong(TAG_NEXT_ID);

        // Load serialized grid storage
        CompoundTag storageTag = tag.getCompound(TAG_STORAGE);
        for (String storageIdStr : storageTag.getAllKeys()) {
            long storageId;
            try {
                storageId = Long.parseLong(storageIdStr);
            } catch (NumberFormatException e) {
                AELog.warn("Unable to load grid storage with malformed id: '{}'", storageIdStr);
                continue;
            }
            result.storage.put(storageId, new GridStorage(storageId, storageTag.getCompound(storageIdStr)));
        }

        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {

        tag.putLong(TAG_NEXT_ID, nextGridId);

        // Save serialized grid storage
        CompoundTag storageTag = new CompoundTag();
        for (Map.Entry<Long, GridStorage> entry : storage.entrySet()) {

            GridStorage gridStorage = entry.getValue();

            if (gridStorage.getGrid() == null || gridStorage.getGrid().isEmpty()) {
                continue;
            }

            try {
                entry.getValue().saveState();
            } catch (Exception e) {
                AELog.warn("Failed to save state of Grid {}, storing last known value instead.", entry.getKey(), e);
            }
            storageTag.put(String.valueOf(entry.getKey()), entry.getValue().dataObject());
        }
        tag.put(TAG_STORAGE, storageTag);

        return tag;
    }

}
