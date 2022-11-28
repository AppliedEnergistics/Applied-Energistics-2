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

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.mattercannon.MatterCannonAmmo;

public class MatterCannonAmmoProvider extends AE2RecipeProvider {
    public MatterCannonAmmoProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {
        tag(consumer, "nuggets/meatraw", "c:meatraw_nuggets", 32);
        tag(consumer, "nuggets/meatcooked", "c:meatcooked_nuggets", 32);
        tag(consumer, "nuggets/meat", "c:meat_nuggets", 32);
        tag(consumer, "nuggets/chicken", "c:chicken_nuggets", 32);
        tag(consumer, "nuggets/beef", "c:beef_nuggets", 32);
        tag(consumer, "nuggets/sheep", "c:sheep_nuggets", 32);
        tag(consumer, "nuggets/fish", "c:fish_nuggets", 32);

        // derived from real world atomic mass...
        MatterCannonAmmo.tag(consumer, AppEng.makeId("matter_cannon/nuggets/iron"), ConventionTags.IRON_NUGGET,
                55.845f);
        MatterCannonAmmo.tag(consumer, AppEng.makeId("matter_cannon/nuggets/gold"), ConventionTags.GOLD_NUGGET,
                196.96655f);

        tag(consumer, "nuggets/lithium", "c:lithium_nuggets", 6.941f);
        tag(consumer, "nuggets/beryllium", "c:beryllium_nuggets", 9.0122f);
        tag(consumer, "nuggets/boron", "c:boron_nuggets", 10.811f);
        tag(consumer, "nuggets/carbon", "c:carbon_nuggets", 12.0107f);
        tag(consumer, "nuggets/coal", "c:coal_nuggets", 12.0107f);
        tag(consumer, "nuggets/charcoal", "c:charcoal_nuggets", 12.0107f);
        tag(consumer, "nuggets/sodium", "c:sodium_nuggets", 22.9897f);
        tag(consumer, "nuggets/magnesium", "c:magnesium_nuggets", 24.305f);
        tag(consumer, "nuggets/aluminum", "c:aluminum_nuggets", 26.9815f);
        tag(consumer, "nuggets/silicon", "c:silicon_nuggets", 28.0855f);
        tag(consumer, "nuggets/phosphorus", "c:phosphorus_nuggets", 30.9738f);
        tag(consumer, "nuggets/sulfur", "c:sulfur_nuggets", 32.065f);
        tag(consumer, "nuggets/potassium", "c:potassium_nuggets", 39.0983f);
        tag(consumer, "nuggets/calcium", "c:calcium_nuggets", 40.078f);
        tag(consumer, "nuggets/scandium", "c:scandium_nuggets", 44.9559f);
        tag(consumer, "nuggets/titanium", "c:titanium_nuggets", 47.867f);
        tag(consumer, "nuggets/vanadium", "c:vanadium_nuggets", 50.9415f);
        tag(consumer, "nuggets/manganese", "c:manganese_nuggets", 54.938f);
        tag(consumer, "nuggets/nickel", "c:nickel_nuggets", 58.6934f);
        tag(consumer, "nuggets/cobalt", "c:cobalt_nuggets", 58.9332f);
        tag(consumer, "nuggets/copper", "c:copper_nuggets", 63.546f);
        tag(consumer, "nuggets/zinc", "c:zinc_nuggets", 65.39f);
        tag(consumer, "nuggets/gallium", "c:gallium_nuggets", 69.723f);
        tag(consumer, "nuggets/germanium", "c:germanium_nuggets", 72.64f);
        tag(consumer, "nuggets/bromine", "c:bromine_nuggets", 79.904f);
        tag(consumer, "nuggets/krypton", "c:krypton_nuggets", 83.8f);
        tag(consumer, "nuggets/rubidium", "c:rubidium_nuggets", 85.4678f);
        tag(consumer, "nuggets/strontium", "c:strontium_nuggets", 87.62f);
        tag(consumer, "nuggets/yttrium", "c:yttrium_nuggets", 88.9059f);
        tag(consumer, "nuggets/zirconium", "c:zirconium_nuggets", 91.224f);
        tag(consumer, "nuggets/niobium", "c:niobium_nuggets", 92.9064f);
        tag(consumer, "nuggets/technetium", "c:technetium_nuggets", 98f);
        tag(consumer, "nuggets/ruthenium", "c:ruthenium_nuggets", 101.07f);
        tag(consumer, "nuggets/rhodium", "c:rhodium_nuggets", 102.9055f);
        tag(consumer, "nuggets/palladium", "c:palladium_nuggets", 106.42f);
        tag(consumer, "nuggets/silver", "c:silver_nuggets", 107.8682f);
        tag(consumer, "nuggets/cadmium", "c:cadmium_nuggets", 112.411f);
        tag(consumer, "nuggets/indium", "c:indium_nuggets", 114.818f);
        tag(consumer, "nuggets/tin", "c:tin_nuggets", 118.71f);
        tag(consumer, "nuggets/antimony", "c:antimony_nuggets", 121.76f);
        tag(consumer, "nuggets/iodine", "c:iodine_nuggets", 126.9045f);
        tag(consumer, "nuggets/tellurium", "c:tellurium_nuggets", 127.6f);
        tag(consumer, "nuggets/xenon", "c:xenon_nuggets", 131.293f);
        tag(consumer, "nuggets/cesium", "c:cesium_nuggets", 132.9055f);
        tag(consumer, "nuggets/barium", "c:barium_nuggets", 137.327f);
        tag(consumer, "nuggets/lanthanum", "c:lanthanum_nuggets", 138.9055f);
        tag(consumer, "nuggets/cerium", "c:cerium_nuggets", 140.116f);
        tag(consumer, "nuggets/tantalum", "c:tantalum_nuggets", 180.9479f);
        tag(consumer, "nuggets/tungsten", "c:tungsten_nuggets", 183.84f);
        tag(consumer, "nuggets/osmium", "c:osmium_nuggets", 190.23f);
        tag(consumer, "nuggets/iridium", "c:iridium_nuggets", 192.217f);
        tag(consumer, "nuggets/platinum", "c:platinum_nuggets", 195.078f);
        tag(consumer, "nuggets/lead", "c:lead_nuggets", 207.2f);
        tag(consumer, "nuggets/bismuth", "c:bismuth_nuggets", 208.9804f);
        tag(consumer, "nuggets/uranium", "c:uranium_nuggets", 238.0289f);
        tag(consumer, "nuggets/plutonium", "c:plutonium_nuggets", 244);

        // TE stuff...
        tag(consumer, "nuggets/invar", "c:invar_nuggets", (58.6934f + 55.845f + 55.845f) / 3.0f);
        tag(consumer, "nuggets/electrum", "c:electrum_nuggets", (107.8682f + 196.96655f) / 2.0f);

        MatterCannonAmmo.item(consumer, AppEng.makeId("matter_cannon/matter_ball"), AEItems.MATTER_BALL, 32.0f);
    }

    private static void tag(Consumer<FinishedRecipe> consumer, String recipeId, String tagId, float weight) {
        MatterCannonAmmo.tag(consumer, AppEng.makeId("matter_cannon/" + recipeId),
                TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(tagId)), weight);
    }
}
