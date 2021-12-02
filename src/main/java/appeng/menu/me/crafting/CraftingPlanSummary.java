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

package appeng.menu.me.crafting;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.KeyMap;

/**
 * A crafting plan intended to be sent to the client.
 */
public class CraftingPlanSummary {

    /**
     * @see ICraftingPlan#bytes()
     */
    private final long usedBytes;

    /**
     * @see ICraftingPlan#simulation()
     */
    private final boolean simulation;

    private final List<CraftingPlanSummaryEntry> entries;

    public CraftingPlanSummary(long usedBytes, boolean simulation, List<CraftingPlanSummaryEntry> entries) {
        this.usedBytes = usedBytes;
        this.simulation = simulation;
        this.entries = entries;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public boolean isSimulation() {
        return simulation;
    }

    public List<CraftingPlanSummaryEntry> getEntries() {
        return entries;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarLong(usedBytes);
        buffer.writeBoolean(simulation);
        buffer.writeVarInt(entries.size());
        for (CraftingPlanSummaryEntry entry : entries) {
            entry.write(buffer);
        }
    }

    public static CraftingPlanSummary read(FriendlyByteBuf buffer) {

        long bytesUsed = buffer.readVarLong();
        boolean simulation = buffer.readBoolean();
        int entryCount = buffer.readVarInt();
        ImmutableList.Builder<CraftingPlanSummaryEntry> entries = ImmutableList.builder();
        for (int i = 0; i < entryCount; i++) {
            entries.add(CraftingPlanSummaryEntry.read(buffer));
        }

        return new CraftingPlanSummary(bytesUsed, simulation, entries.build());
    }

    private static class KeyStats {
        public long stored;
        public long crafting;
    }

    /**
     * Creates a plan summary from the given planning result.
     *
     * @param grid         The grid used to determine the amount of items already stored.
     * @param actionSource The action source used to determine the amount of items already stored.
     */
    public static CraftingPlanSummary fromJob(IGrid grid, IActionSource actionSource, ICraftingPlan job) {
        var plan = new KeyMap<>(null, KeyStats::new);

        for (var used : job.usedItems()) {
            plan.mapping(used.getKey()).stored += used.getLongValue();
        }
        for (var missing : job.missingItems()) {
            plan.mapping(missing.getKey()).stored += missing.getLongValue();
        }
        for (var emitted : job.emittedItems()) {
            var entry = plan.mapping(emitted.getKey());
            entry.stored += emitted.getLongValue();
            entry.crafting += emitted.getLongValue();
        }
        for (var entry : job.patternTimes().entrySet()) {
            for (var out : entry.getKey().getOutputs()) {
                plan.mapping(out.what()).crafting += out.amount() * entry.getValue();
            }
        }

        var entries = ImmutableList.<CraftingPlanSummaryEntry>builder();

        var storage = grid.getStorageService().getInventory();

        for (var out : plan) {
            long missingAmount;
            long storedAmount;
            if (job.simulation()) {
                storedAmount = storage.extract(out.getKey(), out.getValue().stored, Actionable.SIMULATE, actionSource);
                missingAmount = out.getValue().stored - storedAmount;
            } else {
                storedAmount = out.getValue().stored;
                missingAmount = 0;
            }
            long craftAmount = out.getValue().crafting;

            entries.add(new CraftingPlanSummaryEntry(
                    out.getKey(),
                    missingAmount,
                    storedAmount,
                    craftAmount));

        }

        return new CraftingPlanSummary(job.bytes(), job.simulation(), entries.build());

    }

}
