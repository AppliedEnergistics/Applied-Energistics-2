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

package appeng.crafting.inv;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;

/**
 * Extended version of {@link ICraftingInventory} to keep track of other simulation state that is not directly related
 * to inventory contents.
 */
public interface ICraftingSimulationState extends ICraftingInventory {
    void emitItems(AEKey what, long amount);

    void addBytes(double bytes);

    default void addStackBytes(AEKey key, long amount, long multiplier) {
        // Crafting storage is 8 times bigger than normal storage, this is intentional.
        addBytes((double) amount * multiplier / key.getType().getUnitsPerByte() * 8);
    }

    void addCrafting(IPatternDetails details, long crafts);
}
