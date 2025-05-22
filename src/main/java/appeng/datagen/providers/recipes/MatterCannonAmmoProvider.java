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

package appeng.datagen.providers.recipes;

import java.util.function.Consumer;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.mattercannon.MatterCannonAmmo;

public class MatterCannonAmmoProvider extends AE2RecipeProvider {
    public MatterCannonAmmoProvider(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        tag(consumer, "nuggets/meatraw", "forge:nuggets/meatraw", 32);
        tag(consumer, "nuggets/meatcooked", "forge:nuggets/meatcooked", 32);
        tag(consumer, "nuggets/meat", "forge:nuggets/meat", 32);
        tag(consumer, "nuggets/chicken", "forge:nuggets/chicken", 32);
        tag(consumer, "nuggets/beef", "forge:nuggets/beef", 32);
        tag(consumer, "nuggets/sheep", "forge:nuggets/sheep", 32);
        tag(consumer, "nuggets/fish", "forge:nuggets/fish", 32);

        // derived from real world atomic mass...
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/nuggets/iron"), ConventionTags.IRON_NUGGET,
                55.845f);
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/nuggets/gold"), ConventionTags.GOLD_NUGGET,
                196.96655f);
        tag(consumer, "nuggets/lithium", "forge:nuggets/lithium", 6.941f);
        tag(consumer, "nuggets/beryllium", "forge:nuggets/beryllium", 9.0122f);
        tag(consumer, "nuggets/boron", "forge:nuggets/boron", 10.811f);
        tag(consumer, "nuggets/carbon", "forge:nuggets/carbon", 12.0107f);
        tag(consumer, "nuggets/coal", "forge:nuggets/coal", 12.0107f);
        tag(consumer, "nuggets/charcoal", "forge:nuggets/charcoal", 12.0107f);
        tag(consumer, "nuggets/sodium", "forge:nuggets/sodium", 22.9897f);
        tag(consumer, "nuggets/magnesium", "forge:nuggets/magnesium", 24.305f);
        tag(consumer, "nuggets/aluminum", "forge:nuggets/aluminum", 26.9815f);
        tag(consumer, "nuggets/silicon", "forge:nuggets/silicon", 28.0855f);
        tag(consumer, "nuggets/phosphorus", "forge:nuggets/phosphorus", 30.9738f);
        tag(consumer, "nuggets/sulfur", "forge:nuggets/sulfur", 32.065f);
        tag(consumer, "nuggets/potassium", "forge:nuggets/potassium", 39.0983f);
        tag(consumer, "nuggets/calcium", "forge:nuggets/calcium", 40.078f);
        tag(consumer, "nuggets/scandium", "forge:nuggets/scandium", 44.9559f);
        tag(consumer, "nuggets/titanium", "forge:nuggets/titanium", 47.867f);
        tag(consumer, "nuggets/vanadium", "forge:nuggets/vanadium", 50.9415f);
        tag(consumer, "nuggets/manganese", "forge:nuggets/manganese", 54.938f);
        tag(consumer, "nuggets/nickel", "forge:nuggets/nickel", 58.6934f);
        tag(consumer, "nuggets/cobalt", "forge:nuggets/cobalt", 58.9332f);
        tag(consumer, "nuggets/copper", "forge:nuggets/copper", 63.546f);
        tag(consumer, "nuggets/zinc", "forge:nuggets/zinc", 65.39f);
        tag(consumer, "nuggets/gallium", "forge:nuggets/gallium", 69.723f);
        tag(consumer, "nuggets/germanium", "forge:nuggets/germanium", 72.64f);
        tag(consumer, "nuggets/bromine", "forge:nuggets/bromine", 79.904f);
        tag(consumer, "nuggets/krypton", "forge:nuggets/krypton", 83.8f);
        tag(consumer, "nuggets/rubidium", "forge:nuggets/rubidium", 85.4678f);
        tag(consumer, "nuggets/strontium", "forge:nuggets/strontium", 87.62f);
        tag(consumer, "nuggets/yttrium", "forge:nuggets/yttrium", 88.9059f);
        tag(consumer, "nuggets/zirconium", "forge:nuggets/zirconium", 91.224f);
        tag(consumer, "nuggets/niobium", "forge:nuggets/niobium", 92.9064f);
        tag(consumer, "nuggets/technetium", "forge:nuggets/technetium", 98f);
        tag(consumer, "nuggets/ruthenium", "forge:nuggets/ruthenium", 101.07f);
        tag(consumer, "nuggets/rhodium", "forge:nuggets/rhodium", 102.9055f);
        tag(consumer, "nuggets/palladium", "forge:nuggets/palladium", 106.42f);
        tag(consumer, "nuggets/silver", "forge:nuggets/silver", 107.8682f);
        tag(consumer, "nuggets/cadmium", "forge:nuggets/cadmium", 112.411f);
        tag(consumer, "nuggets/indium", "forge:nuggets/indium", 114.818f);
        tag(consumer, "nuggets/tin", "forge:nuggets/tin", 118.71f);
        tag(consumer, "nuggets/antimony", "forge:nuggets/antimony", 121.76f);
        tag(consumer, "nuggets/iodine", "forge:nuggets/iodine", 126.9045f);
        tag(consumer, "nuggets/tellurium", "forge:nuggets/tellurium", 127.6f);
        tag(consumer, "nuggets/xenon", "forge:nuggets/xenon", 131.293f);
        tag(consumer, "nuggets/cesium", "forge:nuggets/cesium", 132.9055f);
        tag(consumer, "nuggets/barium", "forge:nuggets/barium", 137.327f);
        tag(consumer, "nuggets/lanthanum", "forge:nuggets/lanthanum", 138.9055f);
        tag(consumer, "nuggets/cerium", "forge:nuggets/cerium", 140.116f);
        tag(consumer, "nuggets/tantalum", "forge:nuggets/tantalum", 180.9479f);
        tag(consumer, "nuggets/tungsten", "forge:nuggets/tungsten", 183.84f);
        tag(consumer, "nuggets/osmium", "forge:nuggets/osmium", 190.23f);
        tag(consumer, "nuggets/iridium", "forge:nuggets/iridium", 192.217f);
        tag(consumer, "nuggets/platinum", "forge:nuggets/platinum", 195.078f);
        tag(consumer, "nuggets/lead", "forge:nuggets/lead", 207.2f);
        tag(consumer, "nuggets/bismuth", "forge:nuggets/bismuth", 208.9804f);
        tag(consumer, "nuggets/uranium", "forge:nuggets/uranium", 238.0289f);
        tag(consumer, "nuggets/plutonium", "forge:nuggets/plutonium", 244);

        // TE stuff...
        tag(consumer, "nuggets/invar", "forge:nuggets/invar", (58.6934f + 55.845f + 55.845f) / 3.0f);
        tag(consumer, "nuggets/electrum", "forge:nuggets/electrum", (107.8682f + 196.96655f) / 2.0f);

        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/matter_ball"), AEItems.MATTER_BALL, 32.0f);
    }

    private static void tag(Consumer<FinishedRecipe> consumer, String recipeId, String tagId, float weight) {
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/" + recipeId),
                TagKey.create(Registries.ITEM, new ResourceLocation(tagId)), weight);
    }
}
