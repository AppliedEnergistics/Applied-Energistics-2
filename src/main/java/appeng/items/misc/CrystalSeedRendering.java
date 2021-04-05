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

package appeng.items.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.item.Item;

import appeng.bootstrap.components.IClientSetupComponent;
import appeng.core.AppEng;

/**
 * Exposes a predicate "growth", which is used in the item model to differentiate the growth stages.
 */
public class CrystalSeedRendering implements IClientSetupComponent {
    private final Item item;

    public CrystalSeedRendering(Item item) {
        this.item = item;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void setup() {
        // Expose the growth of the seed to the model system
        FabricModelPredicateProviderRegistry.register(item, AppEng.makeId("growth"),
                (is, w, p) -> CrystalSeedItem.getGrowthTicks(is) / (float) CrystalSeedItem.GROWTH_TICKS_REQUIRED);
    }
}
