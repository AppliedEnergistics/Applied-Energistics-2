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

package appeng.items.storage;


import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.storage.ISpatialDimension;
import appeng.api.util.WorldCoord;
import appeng.capabilities.Capabilities;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.spatial.StorageHelper;
import appeng.util.Platform;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;


public class ItemSpatialStorageCell extends AEBaseItem implements ISpatialStorageCell {
    private static final String NBT_CELL_ID_KEY = "StorageCellID";
    private static final String NBT_SIZE_X_KEY = "sizeX";
    private static final String NBT_SIZE_Y_KEY = "sizeY";
    private static final String NBT_SIZE_Z_KEY = "sizeZ";

    private final int maxRegion;

    public ItemSpatialStorageCell(final int spatialScale) {
        this.setMaxStackSize(1);
        this.maxRegion = spatialScale;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        final int id = this.getStoredDimensionID(stack);
        if (id >= 0) {
            lines.add(GuiText.CellId.getLocal() + ": " + id);
        }

        final WorldCoord wc = this.getStoredSize(stack);
        if (wc.x > 0) {
            lines.add(GuiText.StoredSize.getLocal() + ": " + wc.x + " x " + wc.y + " x " + wc.z);
        }
    }

    @Override
    public boolean isSpatialStorage(final ItemStack is) {
        return true;
    }

    @Override
    public int getMaxStoredDim(final ItemStack is) {
        return this.maxRegion;
    }

    @Override
    public ISpatialDimension getSpatialDimension() {
        final int id = AppEng.instance().getStorageDimensionID();
        World w = DimensionManager.getWorld(id);
        if (w == null) {
            DimensionManager.initDimension(id);
            w = DimensionManager.getWorld(id);
        }

        if (w != null && w.hasCapability(Capabilities.SPATIAL_DIMENSION, null)) {
            return w.getCapability(Capabilities.SPATIAL_DIMENSION, null);
        }
        return null;
    }

    @Override
    public WorldCoord getStoredSize(final ItemStack is) {
        if (is.hasTagCompound()) {
            final NBTTagCompound c = is.getTagCompound();
            return new WorldCoord(c.getInteger(NBT_SIZE_X_KEY), c.getInteger(NBT_SIZE_Y_KEY), c.getInteger(NBT_SIZE_Z_KEY));
        }
        return new WorldCoord(0, 0, 0);
    }

    @Override
    public int getStoredDimensionID(final ItemStack is) {
        if (is.hasTagCompound()) {
            final NBTTagCompound c = is.getTagCompound();
            return c.getInteger(NBT_CELL_ID_KEY);
        }
        return -1;
    }

    @Override
    public TransitionResult doSpatialTransition(final ItemStack is, final World w, final WorldCoord min, final WorldCoord max, int playerId) {
        final int targetX = max.x - min.x - 1;
        final int targetY = max.y - min.y - 1;
        final int targetZ = max.z - min.z - 1;
        final int maxSize = this.getMaxStoredDim(is);

        final BlockPos targetSize = new BlockPos(targetX, targetY, targetZ);

        ISpatialDimension manager = this.getSpatialDimension();

        int cellid = this.getStoredDimensionID(is);
        if (cellid < 0) {
            cellid = manager.createNewCellDimension(targetSize, playerId);
        }

        try {
            if (manager.isCellDimension(cellid)) {
                BlockPos scale = manager.getCellContentSize(cellid);

                if (scale.equals(targetSize)) {
                    if (targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize) {
                        BlockPos offset = manager.getCellDimensionOrigin(cellid);

                        this.setStorageCell(is, cellid, targetSize);
                        StorageHelper.getInstance()
                                .swapRegions(w, min.x + 1, min.y + 1, min.z + 1, manager.getWorld(), offset.getX(), offset.getY(),
                                        offset.getZ(), targetX - 1, targetY - 1,
                                        targetZ - 1);

                        return new TransitionResult(true, 0);
                    }
                }
            }
            return new TransitionResult(false, 0);
        } finally {
            // clean up newly created dimensions that failed transfer
            if (manager.isCellDimension(cellid) && this.getStoredDimensionID(is) < 0) {
                manager.deleteCellDimension(cellid);
            }
        }
    }

    private void setStorageCell(final ItemStack is, int id, BlockPos size) {
        final NBTTagCompound c = Platform.openNbtData(is);

        c.setInteger(NBT_CELL_ID_KEY, id);
        c.setInteger(NBT_SIZE_X_KEY, size.getX());
        c.setInteger(NBT_SIZE_Y_KEY, size.getY());
        c.setInteger(NBT_SIZE_Z_KEY, size.getZ());
    }
}
