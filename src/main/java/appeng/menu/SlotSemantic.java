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

package appeng.menu;

public enum SlotSemantic {
    NONE(false),
    STORAGE(false),
    PLAYER_INVENTORY(true),
    PLAYER_HOTBAR(true),
    TOOLBOX(true),
    /**
     * Used for configuration slots that configure a filter, such as on planes, import/export busses, etc.
     */
    CONFIG(false),
    /**
     * An upgrade slot on a machine, cell workbench, etc.
     */
    UPGRADE(false),
    /**
     * One or more slots for storage cells, i.e. on drives, cell workbench or chest.
     */
    STORAGE_CELL(false),

    INSCRIBER_PLATE_TOP(false),

    INSCRIBER_PLATE_BOTTOM(false),

    MACHINE_INPUT(false),

    MACHINE_PROCESSING(false),

    MACHINE_OUTPUT(false),

    MACHINE_CRAFTING_GRID(false),

    BLANK_PATTERN(false),

    ENCODED_PATTERN(false),

    VIEW_CELL(false),

    CRAFTING_GRID(false),

    CRAFTING_RESULT(false),

    PROCESSING_PRIMARY_RESULT(false),

    PROCESSING_FIRST_OPTIONAL_RESULT(false),

    PROCESSING_SECOND_OPTIONAL_RESULT(false),

    BIOMETRIC_CARD(false),

    READONLY_STACKS(false);

    private final boolean playerSide;

    SlotSemantic(boolean playerSide) {
        this.playerSide = playerSide;
    }

    /**
     * @return Indicates whether a slot is considered to be part of the items that a player carries.
     */
    public boolean isPlayerSide() {
        return playerSide;
    }
}
