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

package appeng.block.storage;

import net.minecraft.util.IStringSerializable;

import appeng.api.storage.cells.CellState;

/**
 * Describes the different states a single slot of a BlockDrive can be in in
 * terms of rendering.
 */
public enum DriveSlotState implements IStringSerializable {

    // No cell in slot
    EMPTY("empty"),

    // Cell in slot, but unpowered
    OFFLINE("offline"),

    // Online and free space
    ONLINE("online"),

    // Online and not space
    NOT_EMPTY("not_empty"),

    // Types full, space left
    TYPES_FULL("types_full"),

    // Completely full
    FULL("full");

    private final String name;

    DriveSlotState(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static DriveSlotState fromCellStatus(CellState cellStatus) {
        switch (cellStatus) {
            default:
            case ABSENT:
                return DriveSlotState.EMPTY;
            case EMPTY:
                return DriveSlotState.ONLINE;
            case NOT_EMPTY:
                return DriveSlotState.NOT_EMPTY;
            case TYPES_FULL:
                return DriveSlotState.TYPES_FULL;
            case FULL:
                return DriveSlotState.FULL;
        }
    }

}
