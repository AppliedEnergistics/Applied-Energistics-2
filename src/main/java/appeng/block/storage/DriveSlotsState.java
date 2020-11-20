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

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.cells.CellState;

/**
 * Contains the full information about what the state of the slots in a BlockDrive is.
 */
public class DriveSlotsState {

    private final Item[] cells;

    private final DriveSlotState[] states;

    public DriveSlotsState(Item[] cells, DriveSlotState[] states) {
        Preconditions.checkArgument(cells.length == states.length);
        this.cells = cells;
        this.states = states;
    }

    public DriveSlotState getState(int index) {
        if (index >= this.states.length) {
            return DriveSlotState.EMPTY;
        }
        return this.states[index];
    }

    public Item getCell(int index) {
        if (index >= this.cells.length) {
            return null;
        }
        return this.cells[index];
    }

    public int getSlotCount() {
        return this.cells.length;
    }

    /**
     * Retrieve an array that describes the state of each slot in this drive or chest.
     */
    public static DriveSlotsState fromChestOrDrive(IChestOrDrive chestOrDrive) {
        DriveSlotState[] states = new DriveSlotState[chestOrDrive.getCellCount()];
        Item[] cells = new Item[chestOrDrive.getCellCount()];
        for (int i = 0; i < chestOrDrive.getCellCount(); i++) {
            cells[i] = chestOrDrive.getCellItem(i);

            if (!chestOrDrive.isPowered()) {
                if (chestOrDrive.getCellStatus(i) != CellState.EMPTY) {
                    states[i] = DriveSlotState.OFFLINE;
                } else {
                    states[i] = DriveSlotState.EMPTY;
                }
            } else {
                states[i] = DriveSlotState.fromCellStatus(chestOrDrive.getCellStatus(i));
            }
        }
        return new DriveSlotsState(cells, states);
    }

    public static DriveSlotsState createEmpty(int slotCount) {
        DriveSlotState[] states = new DriveSlotState[slotCount];
        Item[] cells = new Item[slotCount];
        for (int i = 0; i < slotCount; i++) {
            states[i] = DriveSlotState.EMPTY;
        }
        return new DriveSlotsState(cells, states);
    }
}
