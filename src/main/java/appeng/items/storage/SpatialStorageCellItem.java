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

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.spatial.SpatialStorageHelper;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.spatial.TransitionInfo;

import net.minecraft.world.item.Item.Properties;

public class SpatialStorageCellItem extends AEBaseItem implements ISpatialStorageCell {
    private static final String TAG_PLOT_ID = "plot_id";

    /**
     * This is only stored in the itemstack to display in the tooltip on the client-side.
     */
    private static final String TAG_PLOT_SIZE = "plot_size";

    private final int maxRegion;

    public SpatialStorageCellItem(Item.Properties props, final int spatialScale) {
        super(props);
        this.maxRegion = spatialScale;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level world, final List<Component> lines,
                                final TooltipFlag advancedTooltips) {
        int plotId = this.getAllocatedPlotId(stack);
        if (plotId == -1) {
            lines.add(GuiText.Unformatted.text().copy().withStyle(ChatFormatting.ITALIC));
            lines.add(GuiText.SpatialCapacity.text(maxRegion, maxRegion, maxRegion));
            return;
        }

        // Add a serial number to allows players to keep different cells apart
        // Try to make this a little more flavorful.
        String serialNumber = String.format(Locale.ROOT, "SP-%04d", plotId);
        lines.add(GuiText.SerialNumber.text(serialNumber));

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_PLOT_SIZE, Constants.NBT.TAG_LONG)) {
            BlockPos size = BlockPos.of(tag.getLong(TAG_PLOT_SIZE));
            lines.add(GuiText.StoredSize.text(size.getX(), size.getY(), size.getZ()));
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
    public int getAllocatedPlotId(final ItemStack is) {
        final CompoundTag c = is.getTag();
        if (c != null && c.contains(TAG_PLOT_ID)) {
            try {
                int plotId = c.getInt(TAG_PLOT_ID);
                if (SpatialStoragePlotManager.INSTANCE.getPlot(plotId) == null) {
                    return -1;
                }
                return plotId;
            } catch (Exception e) {
                AELog.warn("Failed to retrieve spatial storage dimension: %s", e);
            }
        }
        return -1;
    }

    @Override
    public boolean doSpatialTransition(final ItemStack is, final ServerLevel w, final BlockPos min,
                                       final BlockPos max, int playerId) {
        final int targetX = max.getX() - min.getX() - 1;
        final int targetY = max.getY() - min.getY() - 1;
        final int targetZ = max.getZ() - min.getZ() - 1;
        final int maxSize = this.getMaxStoredDim(is);
        if (targetX > maxSize && targetY > maxSize && targetZ > maxSize) {
            AELog.info(
                    "Failing spatial transition because the transfer area (%dx%dx%d) exceeds the cell capacity (%s).",
                    targetX, targetY, targetZ, maxSize);
            return false;
        }

        final BlockPos targetSize = new BlockPos(targetX, targetY, targetZ);

        SpatialStoragePlotManager manager = SpatialStoragePlotManager.INSTANCE;

        SpatialStoragePlot plot = SpatialStoragePlotManager.INSTANCE.getPlot(this.getAllocatedPlotId(is));
        if (plot != null) {
            // Check that the existing plot has the right size
            if (!plot.getSize().equals(targetSize)) {
                AELog.info(
                        "Failing spatial transition because the transfer area (%dx%dx%d) does not match the spatial storage plot's size (%s).",
                        targetX, targetY, targetZ, plot.getSize());
                return false;
            }
        } else {
            // Otherwise allocate a new one
            plot = manager.allocatePlot(targetSize, playerId);
        }

        // Store some information about this transition in the plot
        TransitionInfo info = new TransitionInfo(w.dimension().location(), min, max, Instant.now());
        manager.setLastTransition(plot.getId(), info);

        try {
            ServerLevel cellWorld = manager.getWorld();

            BlockPos offset = plot.getOrigin();

            this.setStoredDimension(is, plot.getId(), plot.getSize());
            SpatialStorageHelper.getInstance().swapRegions(w, min.getX() + 1, min.getY() + 1, min.getZ() + 1,
                    cellWorld,
                    offset.getX(), offset.getY(), offset.getZ(), targetX - 1, targetY - 1, targetZ - 1);

            return true;
        } finally {
            // clean up newly created dimensions that failed transfer
            if (this.getAllocatedPlotId(is) == -1) {
                manager.freePlot(plot.getId(), true);
            }
        }
    }

    public void setStoredDimension(final ItemStack is, int plotId, BlockPos size) {
        final CompoundTag c = is.getOrCreateTag();
        c.putInt(TAG_PLOT_ID, plotId);
        c.putLong(TAG_PLOT_SIZE, size.asLong());
    }
}
