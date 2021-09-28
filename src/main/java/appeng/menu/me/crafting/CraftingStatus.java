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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.storage.data.IAEStack;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.menu.me.common.IncrementalUpdateHelper;

/**
 * Describes a currently running crafting job. A crafting status can either be a full update which replaces any
 * previously kept state on the client ({@link #isFullStatus()}, or an incremental update, which uses previously sent
 * {@link CraftingStatusEntry#getSerial() serials} to update entries on the client that were previously sent. To reduce
 * the packet size for updates, the {@link CraftingStatusEntry#getStack() stack} for entries that were previously sent
 * to the client are set to {@code null}.
 */
public class CraftingStatus {

    public static final CraftingStatus EMPTY = new CraftingStatus(true, 0, 0, 0, Collections.emptyList());

    /**
     * True if this status update replaces any previous status information. Otherwise it should be considered an
     * incremental update.
     */
    private final boolean fullStatus;

    /**
     * @see ElapsedTimeTracker
     */
    private final long elapsedTime;

    /**
     * @see ElapsedTimeTracker
     */
    private final long remainingItemCount;

    /**
     * @see ElapsedTimeTracker
     */
    private final long startItemCount;

    private final List<CraftingStatusEntry> entries;

    public CraftingStatus(boolean fullStatus, long elapsedTime, long remainingItemCount, long startItemCount,
            List<CraftingStatusEntry> entries) {
        this.fullStatus = fullStatus;
        this.elapsedTime = elapsedTime;
        this.remainingItemCount = remainingItemCount;
        this.startItemCount = startItemCount;
        this.entries = ImmutableList.copyOf(entries);
    }

    public boolean isFullStatus() {
        return fullStatus;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getRemainingItemCount() {
        return remainingItemCount;
    }

    public long getStartItemCount() {
        return startItemCount;
    }

    public List<CraftingStatusEntry> getEntries() {
        return entries;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(fullStatus);
        buffer.writeVarLong(elapsedTime);
        buffer.writeVarLong(remainingItemCount);
        buffer.writeVarLong(startItemCount);
        buffer.writeVarInt(entries.size());
        for (CraftingStatusEntry entry : entries) {
            entry.write(buffer);
        }
    }

    public static CraftingStatus read(FriendlyByteBuf buffer) {
        boolean fullStatus = buffer.readBoolean();
        long elapsedTime = buffer.readVarLong();
        long remainingItemCount = buffer.readVarLong();
        long startItemCount = buffer.readVarLong();
        int entryCount = buffer.readVarInt();

        ImmutableList.Builder<CraftingStatusEntry> entries = ImmutableList.builder();
        for (int i = 0; i < entryCount; i++) {
            entries.add(CraftingStatusEntry.read(buffer));
        }

        return new CraftingStatus(fullStatus, elapsedTime, remainingItemCount, startItemCount, entries.build());
    }

    public static CraftingStatus create(IncrementalUpdateHelper<IAEStack> changes,
            CraftingCpuLogic logic) {

        boolean full = changes.isFullUpdate();

        ImmutableList.Builder<CraftingStatusEntry> newEntries = ImmutableList.builder();
        for (var stack : changes) {
            long storedCount = logic.getStored(stack);
            long activeCount = logic.getWaitingFor(stack);
            long pendingCount = logic.getPendingOutputs(stack);

            IAEStack sentStack = IAEStack.copy(stack);
            if (!full && changes.getSerial(stack) != null) {
                // The item was already sent to the client, so we can skip the item stack
                sentStack = null;
            }

            var entry = new CraftingStatusEntry(
                    changes.getOrAssignSerial(stack),
                    sentStack,
                    storedCount,
                    activeCount,
                    pendingCount);
            newEntries.add(entry);

            if (entry.isDeleted()) {
                stack.reset(); // Ensure it is deleted on commit, since the client will also clear it.
            }
        }

        long elapsedTime = logic.getElapsedTimeTracker().getElapsedTime();
        long remainingItems = logic.getElapsedTimeTracker().getRemainingItemCount();
        long startItems = logic.getElapsedTimeTracker().getStartItemCount();

        return new CraftingStatus(
                full,
                elapsedTime,
                remainingItems,
                startItems,
                newEntries.build());
    }

}
