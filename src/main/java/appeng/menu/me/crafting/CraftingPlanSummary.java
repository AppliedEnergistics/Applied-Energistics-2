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
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;

/**
 * A crafting plan intended to be sent to the client.
 */
public class CraftingPlanSummary {

    /**
     * @see ICraftingJob#getByteTotal()
     */
    private final long usedBytes;

    /**
     * @see ICraftingJob#isSimulation()
     */
    private final boolean simulation;

    /**
     * @see ICraftingJob#populatePlan(IItemList)
     */
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

    /**
     * Creates a plan summary from the given planning result.
     *
     * @param grid         The grid used to determine the amount of items already stored.
     * @param actionSource The action source used to determine the amount of items already stored.
     */
    public static CraftingPlanSummary fromJob(IGrid grid, IActionSource actionSource, ICraftingJob job) {
        final IItemList<IAEItemStack> plan = Api.instance().storage()
                .getStorageChannel(IItemStorageChannel.class).createList();
        job.populatePlan(plan);

        ImmutableList.Builder<CraftingPlanSummaryEntry> entries = ImmutableList.builder();

        final IStorageService sg = grid.getService(IStorageService.class);
        final IMEInventory<IAEItemStack> items = sg
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

        for (final IAEItemStack out : plan) {
            long missingAmount;
            long storedAmount;
            if (job.isSimulation()) {
                IAEItemStack available = items.extractItems(out.copy(), Actionable.SIMULATE, actionSource);

                storedAmount = available == null ? 0 : available.getStackSize();
                missingAmount = out.getStackSize() - storedAmount;
            } else {
                storedAmount = out.getStackSize();
                missingAmount = 0;
            }
            long craftAmount = out.getCountRequestable();

            entries.add(new CraftingPlanSummaryEntry(
                    out.asItemStackRepresentation(),
                    missingAmount,
                    storedAmount,
                    craftAmount));

        }

        return new CraftingPlanSummary(job.getByteTotal(), job.isSimulation(), entries.build());

    }

}
