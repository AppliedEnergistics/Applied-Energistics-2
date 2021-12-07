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

package appeng.items.tools.quartz;

import java.util.function.Supplier;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.datagen.providers.tags.ConventionTags;

public enum QuartzToolType {
    CERTUS("certus_quartz", () -> Ingredient.of(ConventionTags.CERTUS_QUARTZ)),
    NETHER("nether_quartz", () -> Ingredient.of(ConventionTags.NETHER_QUARTZ)),
    ;

    private final Tier toolTier;

    QuartzToolType(String name, Supplier<Ingredient> repairIngredient) {
        this.toolTier = new Tier() {
            @Override
            public int getUses() {
                return Tiers.IRON.getUses();
            }

            @Override
            public float getSpeed() {
                return Tiers.IRON.getSpeed();
            }

            @Override
            public float getAttackDamageBonus() {
                return Tiers.IRON.getAttackDamageBonus();
            }

            @Override
            public int getLevel() {
                return Tiers.IRON.getLevel();
            }

            @Override
            public int getEnchantmentValue() {
                return Tiers.IRON.getEnchantmentValue();
            }

            @Override
            public Ingredient getRepairIngredient() {
                return repairIngredient.get();
            }

            // This allows mods like LevelZ to identify our tools.
            @Override
            public String toString() {
                return AppEng.MOD_ID + ":" + name;
            }
        };
    }

    public final Tier getToolTier() {
        return toolTier;
    }
}
