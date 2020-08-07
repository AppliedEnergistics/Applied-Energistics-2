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
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.spatial.SpatialDimensionManager;
import appeng.spatial.StorageCellLot;
import appeng.spatial.StorageHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class SpatialStorageCellItem extends AEBaseItem implements ISpatialStorageCell {
    private static final String TAG_LOT_ID = "lot_id";

    private final int maxRegion;

    public SpatialStorageCellItem(Properties props, final int spatialScale) {
        super(props);
        this.maxRegion = spatialScale;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        int lotId = this.getAllocatedLotId(stack);
        if (lotId == -1) {
            lines.add(GuiText.Unformatted.text().deepCopy().mergeStyle(TextFormatting.ITALIC));
            lines.add(GuiText.SpatialCapacity.text(maxRegion, maxRegion, maxRegion));
            return;
        }

        SpatialDimensionManager.INSTANCE.addLotTooltip(lotId, lines);

        if (advancedTooltips.isAdvanced()) {
            StorageCellLot lot = SpatialDimensionManager.INSTANCE.getLot(lotId);
            if (lot != null) {
                lines.add(new StringTextComponent("Origin: " + lot.getOrigin()));
                lines.add(new StringTextComponent("Owner: " + lot.getOwner()));
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
    public int getAllocatedLotId(final ItemStack is) {
        final CompoundNBT c = is.getTag();
        if (c != null && c.contains(TAG_LOT_ID)) {
            try {
                int lotId = c.getInt(TAG_LOT_ID);
                if (SpatialDimensionManager.INSTANCE.getLot(lotId) == null) {
                    return -1;
                }
                return lotId;
            } catch (Exception e) {
                AELog.warn("Failed to retrieve storage cell dimension.", e);
            }
        }
        return -1;
    }

    @Override
    public TransitionResult doSpatialTransition(final ItemStack is, final ServerWorld w, final WorldCoord min,
            final WorldCoord max, int playerId) {
        final int targetX = max.x - min.x - 1;
        final int targetY = max.y - min.y - 1;
        final int targetZ = max.z - min.z - 1;
        final int maxSize = this.getMaxStoredDim(is);

        final BlockPos targetSize = new BlockPos(targetX, targetY, targetZ);

        SpatialDimensionManager manager = SpatialDimensionManager.INSTANCE;

        StorageCellLot lot = SpatialDimensionManager.INSTANCE.getLot(this.getAllocatedLotId(is));
        if (lot == null) {
            lot = manager.allocateLot(targetSize, playerId);
        }

        try {
            ServerWorld cellWorld = manager.getWorld();

            BlockPos scale = lot.getSize();

            if (scale.equals(targetSize)) {
                if (targetX <= maxSize && targetY <= maxSize && targetZ <= maxSize) {
                    BlockPos offset = lot.getOrigin();

                        this.setStoredDimension(is, lot.getId());
                        StorageHelper.getInstance().swapRegions(w, min.x + 1, min.y + 1, min.z + 1, cellWorld,
                                offset.getX(), offset.getY(), offset.getZ(), targetX - 1, targetY - 1, targetZ - 1);

                        return new TransitionResult(true, 0);
                    }
                }

            return new TransitionResult(false, 0);
        } finally {
            // clean up newly created dimensions that failed transfer
            if (this.getAllocatedLotId(is) == -1) {
                manager.freeLot(lot.getId());
            }
        }
    }

    private void setStoredDimension(final ItemStack is, int lotId) {
        final CompoundNBT c = is.getOrCreateTag();
        c.putInt(TAG_LOT_ID, lotId);
    }
}
