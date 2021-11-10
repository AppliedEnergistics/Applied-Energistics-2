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

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.AEKey;

/**
 * Simplified inventory with unbounded capacity and support for multiple IAEStacks. Used for crafting, both for the
 * simulation and for the CPU's own inventory.
 */
public interface ICraftingInventory {
    /**
     * Inject items. Can never fail.
     */
    void insert(AEKey what, long amount, Actionable mode);

    /**
     * Extract items.
     */
    long extract(AEKey what, long amount, Actionable mode);

    /**
     * Return a list of templates that match the input with {@link FuzzyMode#IGNORE_ALL} to obtain compatible crafting
     * ingredients in case patterns allow substitutions.
     * <p/>
     * Use {@link #extract} to query the exact amount that is available.
     */
    Iterable<AEKey> findFuzzyTemplates(AEKey input);
}
