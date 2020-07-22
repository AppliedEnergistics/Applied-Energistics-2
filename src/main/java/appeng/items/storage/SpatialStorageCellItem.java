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

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.implementations.TransitionResult;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.storage.ISpatialDimension;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.spatial.SpatialDimensionManager;
import appeng.spatial.StorageHelper;

public class SpatialStorageCellItem extends AEBaseItem implements ISpatialStorageCell {
    private static final String TAG_DIMENSION_ID = "dimension_id";

    private final int maxRegion;

    public SpatialStorageCellItem(Properties props, final int spatialScale) {
        super(props);
        this.maxRegion = spatialScale;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        final DimensionType dimType = this.getStoredDimension(stack);
        if (dimType == null) {
            lines.add(GuiText.Unformatted.textComponent().applyTextStyle(TextFormatting.ITALIC));
            lines.add(GuiText.SpatialCapacity.textComponent(maxRegion, maxRegion, maxRegion));
        } else {
            SpatialDimensionManager.INSTANCE.addCellDimensionTooltip(dimType, lines);
        }

        if (advancedTooltips.isAdvanced()) {
            if (dimType != null && dimType.getRegistryName() != null) {
                lines.add(new StringTextComponent("Dimension: " + dimType.getRegistryName()));
            }
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
    public DimensionType getStoredDimension(final ItemStack is) {
        final CompoundNBT c = is.getTag();
        if (c != null && c.contains(TAG_DIMENSION_ID)) {
            try {
                ResourceLocation dimTypeId = new ResourceLocation(c.getString(TAG_DIMENSION_ID));
                return DimensionType.byName(dimTypeId);
            } catch (Exception e) {
                AELog.warn("Failed to retrieve storage cell dimension.", e);
            }
        }
        return null;
    }

    @Override
    public TransitionResult doSpatialTransition(final ItemStack is, final World w, final WorldCoord min,
            final WorldCoord max, int playerId) {
        final int targetX = max.x - min.x - 1;
        final int targetY = max.y - min.y - 1;
        final int targetZ = max.z - min.z - 1;
        final int maxSize = this.getMaxStoredDim(is);

        final BlockPos targetSize = new BlockPos(targetX, targetY, targetZ);

        ISpatialDimension manager = SpatialDimensionManager.INSTANCE;

        DimensionType storedDim = this.getStoredDimension(is);
        if (storedDim == null) {
            storedDim = manager.createNewCellDimension(targetSize);
        }

        if (storedDim == null) {
            // Failed to create the dimension
            return new TransitionResult(false, 0);
        }

        try {
            if (manager.isCellDimension(storedDim)) {
                World cellWorld = manager.getWorld(storedDim);

                BlockPos scale = manager.getCellDimensionSize(storedDim);

                if (scale.equals(targetSize)) {
                    if (targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize) {
                        BlockPos offset = manager.getCellDimensionOrigin(storedDim);

                        this.setStoredDimension(is, storedDim);
                        StorageHelper.getInstance().swapRegions(w, min.x + 1, min.y + 1, min.z + 1, cellWorld,
                                offset.getX(), offset.getY(), offset.getZ(), targetX - 1, targetY - 1, targetZ - 1);

                        return new TransitionResult(true, 0);
                    }
                }
            }
            return new TransitionResult(false, 0);
        } finally {
            // clean up newly created dimensions that failed transfer
            if (manager.isCellDimension(storedDim) && this.getStoredDimension(is) == null) {
                manager.deleteCellDimension(storedDim);
            }
        }
    }

    private void setStoredDimension(final ItemStack is, DimensionType dim) {
        final CompoundNBT c = is.getOrCreateTag();
        c.putString(TAG_DIMENSION_ID, dim.getRegistryName().toString());
    }
}
