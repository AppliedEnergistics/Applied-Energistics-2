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

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEKey;

/**
 * Describes an entry in a crafting job, which describes how many items of one type are yet to be crafted, or currently
 * scheduled to be crafted.
 */
public class CraftingStatusEntry {
    private final long serial;
    @Nullable
    private final AEKey what;
    private final long storedAmount;
    private final long activeAmount;
    private final long pendingAmount;

    public CraftingStatusEntry(long serial, @Nullable AEKey what, long storedAmount, long activeAmount,
            long pendingAmount) {
        this.serial = serial;
        this.what = what;
        this.storedAmount = storedAmount;
        this.activeAmount = activeAmount;
        this.pendingAmount = pendingAmount;
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

    public AEKey getWhat() {
        return what;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarLong(serial);
        buffer.writeVarLong(activeAmount);
        buffer.writeVarLong(storedAmount);
        buffer.writeVarLong(pendingAmount);
        AEKey.writeOptionalKey(buffer, what);
    }

    public static CraftingStatusEntry read(FriendlyByteBuf buffer) {
        long serial = buffer.readVarLong();
        long missingAmount = buffer.readVarLong();
        long storedAmount = buffer.readVarLong();
        long craftAmount = buffer.readVarLong();
        var what = AEKey.readOptionalKey(buffer);
        return new CraftingStatusEntry(serial, what, storedAmount, missingAmount, craftAmount);
    }

    /**
     * Indicates whether this entry is actually a deletion record.
     */
    public boolean isDeleted() {
        return storedAmount == 0 && activeAmount == 0 && pendingAmount == 0;
    }

}
