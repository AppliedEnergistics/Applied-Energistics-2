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
import java.util.Locale;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;
import appeng.spatial.SpatialStorageHelper;
import appeng.spatial.SpatialStoragePlot;
import appeng.spatial.SpatialStoragePlotManager;
import appeng.spatial.TransitionInfo;

public class SpatialStorageCellItem extends AEBaseItem implements ISpatialStorageCell {
    private static final Logger LOG = LoggerFactory.getLogger(SpatialStorageCellItem.class);

    private final int maxRegion;

    public SpatialStorageCellItem(Properties props, int spatialScale) {
        super(props);
        this.maxRegion = spatialScale;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
            Consumer<Component> lines,
            TooltipFlag advancedTooltips) {
        int plotId = this.getAllocatedPlotId(stack);
        if (plotId == -1) {
            lines.accept(Tooltips.of(GuiText.Unformatted).withStyle(ChatFormatting.ITALIC));
            lines.accept(Tooltips.of(GuiText.SpatialCapacity, maxRegion, maxRegion, maxRegion));
            return;
        }

        // Add a serial number to allows players to keep different cells apart
        // Try to make this a little more flavorful.
        String serialNumber = String.format(Locale.ROOT, "SP-%04d", plotId);
        lines.accept(Tooltips.of(GuiText.SerialNumber, serialNumber));

        var plotInfo = stack.get(AEComponents.SPATIAL_PLOT_INFO);
        if (plotInfo != null) {
            var size = plotInfo.size();
            lines.accept(Tooltips.of(GuiText.StoredSize, size.getX(), size.getY(), size.getZ()));
        }
    }

    @Override
    public boolean isSpatialStorage(ItemStack is) {
        return true;
    }

    @Override
    public int getMaxStoredDim(ItemStack is) {
        return this.maxRegion;
    }

    @Override
    public int getAllocatedPlotId(ItemStack stack) {
        var plotInfo = stack.get(AEComponents.SPATIAL_PLOT_INFO);
        if (plotInfo != null) {
            try {
                if (SpatialStoragePlotManager.INSTANCE.getPlot(plotInfo.id()) == null) {
                    return -1;
                }
                return plotInfo.id();
            } catch (Exception e) {
                LOG.warn("Failed to retrieve spatial storage dimension for plot {}: {}", plotInfo, e);
            }
        }
        return -1;
    }

    @Override
    public boolean doSpatialTransition(ItemStack is, ServerLevel level, BlockPos min,
            BlockPos max, int playerId) {
        final int targetX = max.getX() - min.getX() - 1;
        final int targetY = max.getY() - min.getY() - 1;
        final int targetZ = max.getZ() - min.getZ() - 1;
        final int maxSize = this.getMaxStoredDim(is);
        if (targetX > maxSize || targetY > maxSize || targetZ > maxSize) {
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
        TransitionInfo info = new TransitionInfo(level.dimension().location(), min, max, Instant.now());
        manager.setLastTransition(plot.getId(), info);

        try {
            ServerLevel cellLevel = manager.getLevel();

            BlockPos offset = plot.getOrigin();

            this.setStoredDimension(is, plot.getId(), plot.getSize());
            SpatialStorageHelper.getInstance().swapRegions(level, min.getX() + 1, min.getY() + 1, min.getZ() + 1,
                    cellLevel,
                    offset.getX(), offset.getY(), offset.getZ(), targetX - 1, targetY - 1, targetZ - 1);

            return true;
        } finally {
            // clean up newly created dimensions that failed transfer
            if (this.getAllocatedPlotId(is) == -1) {
                manager.freePlot(plot.getId(), true);
            }
        }
    }

    public void setStoredDimension(ItemStack is, int plotId, BlockPos size) {
        is.set(AEComponents.SPATIAL_PLOT_INFO, new SpatialPlotInfo(
                plotId, size.immutable()));
    }
}
