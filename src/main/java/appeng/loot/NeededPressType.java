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

package appeng.loot;

import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;

/**
 * Specifies the types of presses that are still needed by the player.
 */
public enum NeededPressType {
    CALCULATION_PROCESSOR_PRESS("calc", () -> AEItems.CALCULATION_PROCESSOR_PRESS),
    ENGINEERING_PROCESSOR_PRESS("eng", () -> AEItems.ENGINEERING_PROCESSOR_PRESS),
    LOGIC_PROCESSOR_PRESS("logic", () -> AEItems.LOGIC_PROCESSOR_PRESS),
    SILICON_PRESS("silicon", () -> AEItems.SILICON_PRESS);

    private final String criterionName;

    private final Supplier<ItemDefinition<?>> item;

    NeededPressType(String criterionName, Supplier<ItemDefinition<?>> item) {
        this.criterionName = criterionName;
        this.item = item;
    }

    /**
     * Name of the advancement trigger/criterion in the "main/presses" advancement.
     */
    public String getCriterionName() {
        return criterionName;
    }

    /**
     * @return The item for the press.
     */
    public ItemDefinition<?> getItem() {
        var item = this.item.get();
        Preconditions.checkState(item != null, "AE2 has not initialized its items yet.");
        return item;
    }
}
