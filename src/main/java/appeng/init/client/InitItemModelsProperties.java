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

package appeng.init.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.util.AEColor;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.items.tools.powered.ColorApplicatorItem;

/**
 * Registers custom properties that can be used in item model JSON files.
 */
@OnlyIn(Dist.CLIENT)
public final class InitItemModelsProperties {

    public static final ResourceLocation COLORED_PREDICATE_ID = AppEng.makeId("colored");
    public static final ResourceLocation ENERGY_FILL_LEVEL_ID = AppEng.makeId("fill_level");

    private InitItemModelsProperties() {
    }

    public static void init() {
        ColorApplicatorItem colorApplicatorItem = AEItems.COLOR_APPLICATOR.asItem();
        ItemProperties.register(colorApplicatorItem, COLORED_PREDICATE_ID,
                (itemStack, level, entity, seed) -> {
                    // If the stack has no color, don't use the colored model since the impact of
                    // calling getColor for every quad is extremely high, if the stack tries to
                    // re-search its
                    // inventory for a new paintball everytime
                    AEColor col = colorApplicatorItem.getActiveColor(itemStack);
                    return col != null ? 1 : 0;
                });

        // Register the client-only item model property for chargeable items
        ForgeRegistries.ITEMS.forEach(item -> {
            if (!(item instanceof AEBaseBlockItemChargeable chargeable)) {
                return;
            }

            ItemProperties.register(chargeable, ENERGY_FILL_LEVEL_ID,
                    (is, level, entity, seed) -> {
                        double curPower = chargeable.getAECurrentPower(is);
                        double maxPower = chargeable.getAEMaxPower(is);

                        return (float) (curPower / maxPower);
                    });
        });
    }

}
