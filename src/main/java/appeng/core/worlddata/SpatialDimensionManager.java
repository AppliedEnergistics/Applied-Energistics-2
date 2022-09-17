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
import appeng.capabilities.Capabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;


public class SpatialDimensionManager implements ISpatialDimension, ICapabilitySerializable<NBTTagCompound> {
    private static final String NBT_SPATIAL_DATA_KEY = "spatial_data";
    private static final String NBT_SPATIAL_ID_KEY = "id";

    private final World world;
    private final Map<Integer, StorageCellData> spatialData = new HashMap<>();

    private static final int MAX_CELL_DIMENSION = 512;

    public SpatialDimensionManager(World world) {
        this.world = world;
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public int createNewCellDimension(BlockPos contentSize, int owner) {
        int newId = this.getNextId();

        StorageCellData data = new StorageCellData();
        data.contentDimension = contentSize;
        data.owner = owner;

        this.spatialData.put(newId, data);

        return newId;
    }

    @Override
    public void deleteCellDimension(int cellStorageId) {
        StorageCellData removed = this.spatialData.remove(cellStorageId);
        if (removed != null) {
            this.clearCellArea(cellStorageId, removed);
        }
    }

    @Override
    public boolean isCellDimension(int cellStorageId) {
        return this.spatialData.containsKey(cellStorageId);
    }

    @Override
    public int getCellDimensionOwner(int cellStorageId) {
        StorageCellData cell = this.spatialData.get(cellStorageId);
        if (cell != null) {
            return cell.owner;
        }
        return -1;
    }

    @Override
    public BlockPos getCellDimensionOrigin(int cellStorageId) {
        if (this.isCellDimension(cellStorageId)) {
            return this.getBlockPosFromId(cellStorageId);
        }
        return null;
    }

    @Override
    public BlockPos getCellContentSize(int cellStorageId) {
        StorageCellData cell = this.spatialData.get(cellStorageId);
        if (cell != null) {
            return cell.contentDimension;
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == Capabilities.SPATIAL_DIMENSION;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.SPATIAL_DIMENSION) {
            return (T) this;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound ret = new NBTTagCompound();
        final NBTTagList list = new NBTTagList();

        for (Map.Entry<Integer, StorageCellData> entry : this.spatialData.entrySet()) {
            final NBTTagCompound nbt = entry.getValue().serializeNBT();
            nbt.setInteger(NBT_SPATIAL_ID_KEY, entry.getKey());
            list.appendTag(nbt);
        }
        ret.setTag(NBT_SPATIAL_DATA_KEY, list);
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(NBT_SPATIAL_DATA_KEY)) {
            final NBTTagList list = (NBTTagList) nbt.getTag(NBT_SPATIAL_DATA_KEY);

            this.spatialData.clear();
            for (int i = 0; i < list.tagCount(); ++i) {
                final NBTTagCompound entry = list.getCompoundTagAt(i);
                final StorageCellData data = new StorageCellData();
                final int id = entry.getInteger(NBT_SPATIAL_ID_KEY);
                data.deserializeNBT(entry);
                this.spatialData.put(id, data);
            }
        }
    }

    private int getNextId() {
        return this.spatialData.keySet().stream().max(Integer::compare).orElse(-1) + 1;
    }

    private BlockPos getBlockPosFromId(int id) {
        int signBits = id & 0b11;
        int offsetBits = id >> 2;
        int offsetScale = 1;
        int posx = MAX_CELL_DIMENSION / 2;
        int posz = MAX_CELL_DIMENSION / 2;

        // find quadrant
        while (offsetBits != 0) {
            posx += MAX_CELL_DIMENSION * offsetScale * (offsetBits & 0b01);
            posz += MAX_CELL_DIMENSION * offsetScale * (offsetBits >> 1 & 0b01);

            offsetBits >>= 2;
            offsetScale <<= 1;
        }

        // mirror in one of 4 directions
        if ((signBits & 0b01) == 0) {
            posx *= -1;
        }
        if ((signBits & 0b10) == 0) {
            posz *= -1;
        }

        // offset from cell center
        posx -= 64;
        posz -= 64;

        return new BlockPos(posx, 64, posz);
    }

    private void clearCellArea(int cellId, StorageCellData cell) {
        // TODO reset chunks?
    }

    private static class StorageCellData implements INBTSerializable<NBTTagCompound> {
        private static final String NBT_OWNER_KEY = "owner";
        private static final String NBT_DIM_X_KEY = "dim_x";
        private static final String NBT_DIM_Y_KEY = "dim_y";
        private static final String NBT_DIM_Z_KEY = "dim_z";

        public BlockPos contentDimension;
        public int owner;

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger(NBT_DIM_X_KEY, this.contentDimension.getX());
            nbt.setInteger(NBT_DIM_Y_KEY, this.contentDimension.getY());
            nbt.setInteger(NBT_DIM_Z_KEY, this.contentDimension.getZ());
            nbt.setInteger(NBT_OWNER_KEY, this.owner);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.contentDimension = new BlockPos(nbt.getInteger(NBT_DIM_X_KEY), nbt.getInteger(NBT_DIM_Y_KEY), nbt.getInteger(NBT_DIM_Z_KEY));
            this.owner = nbt.getInteger(NBT_OWNER_KEY);
        }
    }
}
