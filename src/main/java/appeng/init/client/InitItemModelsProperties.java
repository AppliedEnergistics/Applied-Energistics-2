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

import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.util.AEColor;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.core.AppEng;
import appeng.core.api.definitions.AEItems;
import appeng.core.features.ItemDefinition;
import appeng.items.misc.CrystalSeedItem;
import appeng.items.tools.powered.ColorApplicatorItem;

/**
 * Registers custom properties that can be used in item model JSON files.
 */
@OnlyIn(Dist.CLIENT)
public final class InitItemModelsProperties {

    private InitItemModelsProperties() {
    }

    public static void init() {
        ColorApplicatorItem colorApplicatorItem = (ColorApplicatorItem) AEItems.COLOR_APPLICATOR.item();
        ItemModelsProperties.registerProperty(colorApplicatorItem, new ResourceLocation(AppEng.MOD_ID, "colored"),
                (itemStack, world, entity) -> {
                    // If the stack has no color, don't use the colored model since the impact of
                    // calling getColor for every quad is extremely high, if the stack tries to
                    // re-search its
                    // inventory for a new paintball everytime
                    AEColor col = colorApplicatorItem.getActiveColor(itemStack);
                    return col != null ? 1 : 0;
                });

        registerSeedGrowth(AEItems.CERTUS_CRYSTAL_SEED);
        registerSeedGrowth(AEItems.FLUIX_CRYSTAL_SEED);
        registerSeedGrowth(AEItems.NETHER_QUARTZ_SEED);

        // Register the client-only item model property for chargeable items
        ForgeRegistries.ITEMS.forEach(item -> {
            if (!(item instanceof AEBaseBlockItemChargeable)) {
                return;
            }

            AEBaseBlockItemChargeable chargeable = (AEBaseBlockItemChargeable) item;
            ItemModelsProperties.registerProperty(chargeable, new ResourceLocation("appliedenergistics2:fill_level"),
                    (is, world, entity) -> {
                        double curPower = chargeable.getAECurrentPower(is);
                        double maxPower = chargeable.getAEMaxPower(is);

                        return (int) Math.round(100 * curPower / maxPower);
                    });
        });
    }

    /**
     * Exposes a predicate "growth", which is used in the item model to differentiate the growth stages.
     */
    private static void registerSeedGrowth(ItemDefinition definition) {
        // Expose the growth of the seed to the model system
        ItemModelsProperties.registerProperty(definition.item(), new ResourceLocation("appliedenergistics2:growth"),
                (is, w, p) -> CrystalSeedItem.getGrowthTicks(is) / (float) CrystalSeedItem.GROWTH_TICKS_REQUIRED);
    }

}
