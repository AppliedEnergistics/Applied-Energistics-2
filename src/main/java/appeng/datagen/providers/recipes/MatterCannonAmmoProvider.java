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

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.mattercannon.MatterCannonAmmo;

public class MatterCannonAmmoProvider extends AE2RecipeProvider {
    public MatterCannonAmmoProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void buildRecipes(RecipeOutput consumer) {
        tag(consumer, "nuggets/meatraw", "c:nuggets/meatraw", 32);
        tag(consumer, "nuggets/meatcooked", "c:nuggets/meatcooked", 32);
        tag(consumer, "nuggets/meat", "c:nuggets/meat", 32);
        tag(consumer, "nuggets/chicken", "c:nuggets/chicken", 32);
        tag(consumer, "nuggets/beef", "c:nuggets/beef", 32);
        tag(consumer, "nuggets/sheep", "c:nuggets/sheep", 32);
        tag(consumer, "nuggets/fish", "c:nuggets/fish", 32);

        // derived from real world atomic mass...
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/nuggets/iron"), ConventionTags.IRON_NUGGET,
                55.845f);
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/nuggets/gold"), ConventionTags.GOLD_NUGGET,
                196.96655f);
        tag(consumer, "nuggets/lithium", "c:nuggets/lithium", 6.941f);
        tag(consumer, "nuggets/beryllium", "c:nuggets/beryllium", 9.0122f);
        tag(consumer, "nuggets/boron", "c:nuggets/boron", 10.811f);
        tag(consumer, "nuggets/carbon", "c:nuggets/carbon", 12.0107f);
        tag(consumer, "nuggets/coal", "c:nuggets/coal", 12.0107f);
        tag(consumer, "nuggets/charcoal", "c:nuggets/charcoal", 12.0107f);
        tag(consumer, "nuggets/sodium", "c:nuggets/sodium", 22.9897f);
        tag(consumer, "nuggets/magnesium", "c:nuggets/magnesium", 24.305f);
        tag(consumer, "nuggets/aluminum", "c:nuggets/aluminum", 26.9815f);
        tag(consumer, "nuggets/silicon", "c:nuggets/silicon", 28.0855f);
        tag(consumer, "nuggets/phosphorus", "c:nuggets/phosphorus", 30.9738f);
        tag(consumer, "nuggets/sulfur", "c:nuggets/sulfur", 32.065f);
        tag(consumer, "nuggets/potassium", "c:nuggets/potassium", 39.0983f);
        tag(consumer, "nuggets/calcium", "c:nuggets/calcium", 40.078f);
        tag(consumer, "nuggets/scandium", "c:nuggets/scandium", 44.9559f);
        tag(consumer, "nuggets/titanium", "c:nuggets/titanium", 47.867f);
        tag(consumer, "nuggets/vanadium", "c:nuggets/vanadium", 50.9415f);
        tag(consumer, "nuggets/manganese", "c:nuggets/manganese", 54.938f);
        tag(consumer, "nuggets/nickel", "c:nuggets/nickel", 58.6934f);
        tag(consumer, "nuggets/cobalt", "c:nuggets/cobalt", 58.9332f);
        tag(consumer, "nuggets/copper", "c:nuggets/copper", 63.546f);
        tag(consumer, "nuggets/zinc", "c:nuggets/zinc", 65.39f);
        tag(consumer, "nuggets/gallium", "c:nuggets/gallium", 69.723f);
        tag(consumer, "nuggets/germanium", "c:nuggets/germanium", 72.64f);
        tag(consumer, "nuggets/bromine", "c:nuggets/bromine", 79.904f);
        tag(consumer, "nuggets/krypton", "c:nuggets/krypton", 83.8f);
        tag(consumer, "nuggets/rubidium", "c:nuggets/rubidium", 85.4678f);
        tag(consumer, "nuggets/strontium", "c:nuggets/strontium", 87.62f);
        tag(consumer, "nuggets/yttrium", "c:nuggets/yttrium", 88.9059f);
        tag(consumer, "nuggets/zirconium", "c:nuggets/zirconium", 91.224f);
        tag(consumer, "nuggets/niobium", "c:nuggets/niobium", 92.9064f);
        tag(consumer, "nuggets/technetium", "c:nuggets/technetium", 98f);
        tag(consumer, "nuggets/ruthenium", "c:nuggets/ruthenium", 101.07f);
        tag(consumer, "nuggets/rhodium", "c:nuggets/rhodium", 102.9055f);
        tag(consumer, "nuggets/palladium", "c:nuggets/palladium", 106.42f);
        tag(consumer, "nuggets/silver", "c:nuggets/silver", 107.8682f);
        tag(consumer, "nuggets/cadmium", "c:nuggets/cadmium", 112.411f);
        tag(consumer, "nuggets/indium", "c:nuggets/indium", 114.818f);
        tag(consumer, "nuggets/tin", "c:nuggets/tin", 118.71f);
        tag(consumer, "nuggets/antimony", "c:nuggets/antimony", 121.76f);
        tag(consumer, "nuggets/iodine", "c:nuggets/iodine", 126.9045f);
        tag(consumer, "nuggets/tellurium", "c:nuggets/tellurium", 127.6f);
        tag(consumer, "nuggets/xenon", "c:nuggets/xenon", 131.293f);
        tag(consumer, "nuggets/cesium", "c:nuggets/cesium", 132.9055f);
        tag(consumer, "nuggets/barium", "c:nuggets/barium", 137.327f);
        tag(consumer, "nuggets/lanthanum", "c:nuggets/lanthanum", 138.9055f);
        tag(consumer, "nuggets/cerium", "c:nuggets/cerium", 140.116f);
        tag(consumer, "nuggets/tantalum", "c:nuggets/tantalum", 180.9479f);
        tag(consumer, "nuggets/tungsten", "c:nuggets/tungsten", 183.84f);
        tag(consumer, "nuggets/osmium", "c:nuggets/osmium", 190.23f);
        tag(consumer, "nuggets/iridium", "c:nuggets/iridium", 192.217f);
        tag(consumer, "nuggets/platinum", "c:nuggets/platinum", 195.078f);
        tag(consumer, "nuggets/lead", "c:nuggets/lead", 207.2f);
        tag(consumer, "nuggets/bismuth", "c:nuggets/bismuth", 208.9804f);
        tag(consumer, "nuggets/uranium", "c:nuggets/uranium", 238.0289f);
        tag(consumer, "nuggets/plutonium", "c:nuggets/plutonium", 244);

        // TE stuff...
        tag(consumer, "nuggets/invar", "c:nuggets/invar", (58.6934f + 55.845f + 55.845f) / 3.0f);
        tag(consumer, "nuggets/electrum", "c:nuggets/electrum", (107.8682f + 196.96655f) / 2.0f);

        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/matter_ball"), AEItems.MATTER_BALL, 32.0f);
    }

    private static void tag(RecipeOutput consumer, String recipeId, String tagId, float weight) {
        MatterCannonAmmo.ammo(consumer, AppEng.makeId("matter_cannon/" + recipeId),
                TagKey.create(Registries.ITEM, new ResourceLocation(tagId)), weight);
    }
}
