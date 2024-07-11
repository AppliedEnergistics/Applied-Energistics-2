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

package appeng.init;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;

import appeng.core.AppEng;
import appeng.core.stats.AdvancementTriggers;

public final class InitAdvancementTriggers {

    public static void init(Registry<CriterionTrigger<?>> registry) {
        Registry.register(registry, AppEng.makeId("network_apprentice"), AdvancementTriggers.NETWORK_APPRENTICE);
        Registry.register(registry, AppEng.makeId("network_engineer"), AdvancementTriggers.NETWORK_ENGINEER);
        Registry.register(registry, AppEng.makeId("network_admin"), AdvancementTriggers.NETWORK_ADMIN);
        Registry.register(registry, AppEng.makeId("spatial_explorer"), AdvancementTriggers.SPATIAL_EXPLORER);
        Registry.register(registry, AppEng.makeId("recursive_networking"), AdvancementTriggers.RECURSIVE);
    }

}
