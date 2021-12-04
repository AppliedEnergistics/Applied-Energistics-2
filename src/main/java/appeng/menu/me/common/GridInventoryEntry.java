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

package appeng.menu.me.common;

import javax.annotation.Nullable;

import appeng.api.stacks.AEKey;

/**
 * Contains information about something that is stored inside of the grid inventory. This is used to synchronize the
 * grid inventory to a client, and incrementally update the information. To this end, each stack sent to the client is
 * identified by a {@link #serial}, which is subsequently used to update the inventory entry for that specific
 * item/fluid/etc.
 */
public class GridInventoryEntry {
    private final long serial;

    @Nullable
    private final AEKey what;

    private final long storedAmount;

    private final long requestableAmount;

    private final boolean craftable;

    public GridInventoryEntry(long serial, @Nullable AEKey what, long storedAmount, long requestableAmount,
            boolean craftable) {
        this.serial = serial;
        this.what = what;
        this.storedAmount = storedAmount;
        this.requestableAmount = requestableAmount;
        this.craftable = craftable;
    }

    /**
     * Gets the serial number assigned to this inventory entry. Subsequent changes to properties other than
     * {@link #what} will use this serial to identify which entry needs to be updated.
     */
    public long getSerial() {
        return serial;
    }

    /**
     * Gets the client-side representation of what is being stored. Do not use the statistical information on this
     * object (count, requestable, etc.) and only use it for informing the player of *what* the stored object is. When
     * this entry is an incremental update, this field is null, and {@link #serial} refers to a previous inventory entry
     * that should be updated.
     */
    @Nullable
    public AEKey getWhat() {
        return what;
    }

    /**
     * How much of {@link #what} is stored in the network.
     */
    public long getStoredAmount() {
        return storedAmount;
    }

    /**
     * How much of {@link #what} can be requested from attached external networks (i.e. logistic pipes).
     */
    public long getRequestableAmount() {
        return requestableAmount;
    }

    /**
     * Indicates that {@link #what} can be automatically crafted.
     */
    public boolean isCraftable() {
        return craftable;
    }

    /**
     * @return True if this entry should still be present, otherwise it's a removal.
     */
    public boolean isMeaningful() {
        return storedAmount > 0 || requestableAmount > 0 || craftable;
    }
}
