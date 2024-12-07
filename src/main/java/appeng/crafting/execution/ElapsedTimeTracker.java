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

import com.google.common.collect.Iterables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;

import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;

public class ElapsedTimeTracker {
    private static final String NBT_ELAPSED_TIME = "elapsedTime";
    private static final String NBT_STARTED_WORK = "startedWork";
    private static final String NBT_COMPLETED_WORK = "completedWork";

    private long lastTime = System.nanoTime();
    private long elapsedTime = 0;

    private final Reference2LongMap<AEKeyType> startedWorkByType = new Reference2LongOpenHashMap<>(
            Iterables.size(AEKeyTypes.getAll()));
    private final Reference2LongMap<AEKeyType> completedWorkByType = new Reference2LongOpenHashMap<>(
            Iterables.size(AEKeyTypes.getAll()));

    public ElapsedTimeTracker() {
    }

    public ElapsedTimeTracker(CompoundTag data) {
        this.elapsedTime = data.getLong(NBT_ELAPSED_TIME);
        readLongByTypeMap(data.getCompound(NBT_STARTED_WORK), startedWorkByType);
        readLongByTypeMap(data.getCompound(NBT_COMPLETED_WORK), completedWorkByType);
    }

    public CompoundTag writeToNBT() {
        CompoundTag data = new CompoundTag();
        data.putLong(NBT_ELAPSED_TIME, elapsedTime);
        data.put(NBT_STARTED_WORK, writeLongByTypeMap(startedWorkByType));
        data.put(NBT_COMPLETED_WORK, writeLongByTypeMap(completedWorkByType));
        return data;
    }

    private static void readLongByTypeMap(CompoundTag tag, Reference2LongMap<AEKeyType> output) {
        for (var keyType : AEKeyTypes.getAll()) {
            output.put(keyType, tag.getLong(keyType.getId().toString()));
        }
    }

    private static CompoundTag writeLongByTypeMap(Reference2LongMap<AEKeyType> input) {
        CompoundTag result = new CompoundTag();
        for (var entry : input.reference2LongEntrySet()) {
            result.putLong(entry.getKey().getId().toString(), entry.getLongValue());
        }
        return result;
    }

    private void updateTime() {
        long currentTime = System.nanoTime();
        this.elapsedTime = this.elapsedTime + (currentTime - this.lastTime);
        this.lastTime = currentTime;
    }

    void decrementItems(long itemDiff, AEKeyType keyType) {
        updateTime();
        completedWorkByType.merge(keyType, itemDiff, this::saturatedSum);
    }

    private long saturatedSum(long a, long b) {
        var result = a + b;
        return result < 0 ? Long.MAX_VALUE : result;
    }

    void addMaxItems(long itemDiff, AEKeyType keyType) {
        updateTime();
        startedWorkByType.merge(keyType, itemDiff, this::saturatedSum);
    }

    public long getElapsedTime() {
        boolean allDone = true;
        for (var keyType : AEKeyTypes.getAll()) {
            if (completedWorkByType.getLong(keyType) < startedWorkByType.getLong(keyType)) {
                allDone = false;
                break;
            }
        }

        if (!allDone) {
            return this.elapsedTime + (System.nanoTime() - this.lastTime);
        } else {
            return this.elapsedTime;
        }
    }

    // TODO: 1.21.4 Change the network packet and screen to use this rather than the counts below
    public float getProgress() {
        double startedUnits = 0;
        double completedUnits = 0;
        for (var keyType : AEKeyTypes.getAll()) {
            var startedForType = startedWorkByType.getLong(keyType);
            var completedForType = completedWorkByType.getLong(keyType);
            startedUnits += startedForType / (double) keyType.getAmountPerUnit();
            completedUnits += completedForType / (double) keyType.getAmountPerUnit();
        }

        return Mth.clamp((float) (completedUnits / startedUnits), 0, 1);
    }

    @Deprecated(forRemoval = true)
    public long getRemainingItemCount() {
        return (int) (Integer.MAX_VALUE - (double) getProgress() * Integer.MAX_VALUE);
    }

    @Deprecated(forRemoval = true)
    public long getStartItemCount() {
        return Integer.MAX_VALUE;
    }
}
