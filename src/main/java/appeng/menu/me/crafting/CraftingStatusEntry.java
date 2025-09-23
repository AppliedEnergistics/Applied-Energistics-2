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

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import appeng.api.stacks.AEKey;

/**
 * Describes an entry in a crafting job, which describes how many items of one type are yet to be crafted, or currently
 * scheduled to be crafted.
 */
public class CraftingStatusEntry implements Comparable<CraftingStatusEntry> {

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingStatusEntry> STREAM_CODEC = StreamCodec.of(
            CraftingStatusEntry::write,
            CraftingStatusEntry::read);

    public static final StreamCodec<RegistryFriendlyByteBuf, List<CraftingStatusEntry>> LIST_STREAM_CODEC = STREAM_CODEC
            .apply(ByteBufCodecs.list());

    private static final Comparator<CraftingStatusEntry> COMPARATOR = Comparator
            .comparing((CraftingStatusEntry e) -> e.getActiveAmount() + e.getPendingAmount())
            .thenComparing(CraftingStatusEntry::getStoredAmount)
            .reversed();

    private final long serial;
    @Nullable
    private final AEKey what;
    private final long storedAmount;
    private final long activeAmount;
    private final long pendingAmount;
    private final long blockedAmount;

    public CraftingStatusEntry(long serial, @Nullable AEKey what, long storedAmount, long activeAmount,
            long pendingAmount, long blockedAmount) {
        this.serial = serial;
        this.what = what;
        this.storedAmount = storedAmount;
        this.activeAmount = activeAmount;
        this.pendingAmount = pendingAmount;
        this.blockedAmount = blockedAmount;
    }

    public long getSerial() {
        return serial;
    }

    public long getActiveAmount() {
        return activeAmount;
    }

    public long getStoredAmount() {
        return storedAmount;
    }

    public long getPendingAmount() {
        return pendingAmount;
    }

    public long getBlockedAmount() {
        return blockedAmount;
    }

    public AEKey getWhat() {
        return what;
    }

    public static void write(RegistryFriendlyByteBuf buffer, CraftingStatusEntry entry) {
        buffer.writeVarLong(entry.serial);
        buffer.writeVarLong(entry.activeAmount);
        buffer.writeVarLong(entry.storedAmount);
        buffer.writeVarLong(entry.pendingAmount);
        buffer.writeVarLong(entry.blockedAmount);
        AEKey.writeOptionalKey(buffer, entry.what);
    }

    public static CraftingStatusEntry read(RegistryFriendlyByteBuf buffer) {
        long serial = buffer.readVarLong();
        long missingAmount = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long craftAmount = buffer.readVarLong();
        long blockedAmount = buffer.readVarLong();
        var what = AEKey.readOptionalKey(buffer);
        return new CraftingStatusEntry(serial, what, storedAmount, missingAmount, craftAmount, blockedAmount);
    }

    /**
     * Indicates whether this entry is actually a deletion record.
     */
    public boolean isDeleted() {
        return storedAmount == 0 && activeAmount == 0 && pendingAmount == 0;
    }

    @Override
    public int compareTo(final CraftingStatusEntry o) {
        return COMPARATOR.compare(this, o);
    }
}
