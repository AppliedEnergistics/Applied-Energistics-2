/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.me.service.helpers;

import java.util.LongSummaryStatistics;

import javax.annotation.Nonnull;

import net.minecraft.CrashReportCategory;
import net.minecraft.util.Mth;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickingRequest;

public class TickTracker implements Comparable<TickTracker> {

    private final TickingRequest request;
    private final IGridTickable gt;
    private final IGridNode node;
    private final LongSummaryStatistics statistics;

    private long lastTick;
    private int currentRate;

    public TickTracker(TickingRequest req, IGridNode node, IGridTickable gt, long currentTick) {
        this.request = req;
        this.gt = gt;
        this.node = node;
        this.setCurrentRate(req.initialTickRate());
        this.setLastTick(currentTick);
        this.statistics = new LongSummaryStatistics();
    }

    @Override
    public int compareTo(@Nonnull final TickTracker t) {
        int next = Long.compare(this.getNextTick(), t.getNextTick());

        if (next != 0) {
            return next;
        }

        int last = Long.compare(this.getLastTick(), t.getLastTick());

        if (last != 0) {
            return last;
        }

        return Integer.compare(this.getCurrentRate(), t.getCurrentRate());

    }

    public void addEntityCrashInfo(CrashReportCategory category) {
        node.fillCrashReportCategory(category);

        category.setDetail("CurrentTickRate", this.getCurrentRate());
        category.setDetail("MinTickRate", this.getRequest().minTickRate());
        category.setDetail("MaxTickRate", this.getRequest().maxTickRate());
        category.setDetail("ConnectedSides", this.getNode().getConnectedSides());
    }

    public int getCurrentRate() {
        return this.currentRate;
    }

    public void setCurrentRate(final int currentRate) {
        this.currentRate = Mth.clamp(currentRate, request.minTickRate(), request.maxTickRate());
    }

    public void setTickOnNextTick() {
        this.currentRate = 0;
    }

    public long getNextTick() {
        return this.lastTick + this.currentRate;
    }

    public long getLastTick() {
        return this.lastTick;
    }

    public void setLastTick(final long lastTick) {
        this.lastTick = lastTick;
    }

    public IGridNode getNode() {
        return this.node;
    }

    public IGridTickable getGridTickable() {
        return this.gt;
    }

    public TickingRequest getRequest() {
        return this.request;
    }

    public LongSummaryStatistics getStatistics() {
        return statistics;
    }
}
