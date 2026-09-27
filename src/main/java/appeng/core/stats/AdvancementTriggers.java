/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.core.stats;

import java.util.Optional;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.PlayerTrigger;

public class AdvancementTriggers {
    /**
     * Has a network with 8 channels
     */
    public static final PlayerTrigger NETWORK_APPRENTICE = new PlayerTrigger();
    /**
     * Has a network with 128 channels
     */
    public static final PlayerTrigger NETWORK_ENGINEER = new PlayerTrigger();
    /**
     * Has a network with 2048 channels
     */
    public static final PlayerTrigger NETWORK_ADMIN = new PlayerTrigger();
    /**
     * Entered spatial dimension
     */
    public static final PlayerTrigger SPATIAL_EXPLORER = new PlayerTrigger();
    /**
     * Placed a storage bus on an interface.
     */
    public static final PlayerTrigger RECURSIVE = new PlayerTrigger();

    public static Criterion<?> networkApprenticeCriterion() {
        return NETWORK_APPRENTICE.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> networkEngineerCriterion() {
        return NETWORK_ENGINEER.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> networkAdminCriterion() {
        return NETWORK_ADMIN.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> spatialExplorerCriterion() {
        return SPATIAL_EXPLORER.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    public static Criterion<?> recursiveCriterion() {
        return RECURSIVE.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }
}
