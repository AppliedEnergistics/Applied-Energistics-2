/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.crafting.execution;

import net.minecraft.nbt.CompoundTag;

public class ElapsedTimeTracker {
    private static final String NBT_ELAPSED_TIME = "elapsedTime";
    private static final String NBT_START_ITEM_COUNT = "startItemCount";
    private static final String NBT_REMAINING_ITEM_COUNT = "remainingItemCount";

    private long lastTime = System.nanoTime();
    private long elapsedTime = 0;
    private final long startItemCount;
    private long remainingItemCount;

    public ElapsedTimeTracker(long startItemCount) {
        this.startItemCount = startItemCount;
        this.remainingItemCount = startItemCount;
    }

    public ElapsedTimeTracker(CompoundTag data) {
        this.elapsedTime = data.getLong(NBT_ELAPSED_TIME);
        this.startItemCount = data.getLong(NBT_START_ITEM_COUNT);
        this.remainingItemCount = data.getLong(NBT_REMAINING_ITEM_COUNT);
    }

    public CompoundTag writeToNBT() {
        CompoundTag data = new CompoundTag();
        data.putLong(NBT_ELAPSED_TIME, elapsedTime);
        data.putLong(NBT_START_ITEM_COUNT, startItemCount);
        data.putLong(NBT_REMAINING_ITEM_COUNT, remainingItemCount);
        return data;
    }

    void decrementItems(long itemDiff) {
        long currentTime = System.nanoTime();
        this.elapsedTime = this.elapsedTime + (currentTime - this.lastTime);
        this.lastTime = currentTime;
        this.remainingItemCount -= itemDiff;
    }

    public long getElapsedTime() {
        if (remainingItemCount > 0) {
            return this.elapsedTime + (System.nanoTime() - this.lastTime);
        } else {
            return this.elapsedTime;
        }
    }

    public long getRemainingItemCount() {
        return this.remainingItemCount;
    }

    public long getStartItemCount() {
        return this.startItemCount;
    }
}
