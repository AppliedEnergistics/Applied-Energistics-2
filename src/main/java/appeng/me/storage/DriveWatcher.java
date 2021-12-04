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

package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.stacks.AEKey;

public class DriveWatcher extends MEInventoryHandler {

    private CellState oldStatus = CellState.EMPTY;
    private final Runnable activityCallback;

    public DriveWatcher(StorageCell i, Runnable activityCallback) {
        super(i);
        this.activityCallback = activityCallback;
    }

    public CellState getStatus() {
        return ((StorageCell) getDelegate()).getStatus();
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        var inserted = super.insert(what, amount, mode, source);

        if (mode == Actionable.MODULATE && inserted > 0) {
            var newStatus = this.getStatus();

            if (newStatus != this.oldStatus) {
                this.activityCallback.run();
                this.oldStatus = newStatus;
            }
        }

        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var extracted = super.extract(what, amount, mode, source);

        if (mode == Actionable.MODULATE && extracted > 0) {
            var newStatus = this.getStatus();

            if (newStatus != this.oldStatus) {
                this.activityCallback.run();
                this.oldStatus = newStatus;
            }
        }

        return extracted;
    }
}
