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

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.ConventionTags;
import appeng.recipes.mattercannon.MatterCannonAmmo;

public class MatterCannonAmmoProvider extends AE2RecipeProvider {
    private final HolderGetter<Item> items;

    public MatterCannonAmmoProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        items = registries.lookupOrThrow(Registries.ITEM);
    }

    @Override
    public void buildRecipes() {
        tag(output, "nuggets/meatraw", "c:nuggets/meatraw", 32);
        tag(output, "nuggets/meatcooked", "c:nuggets/meatcooked", 32);
        tag(output, "nuggets/meat", "c:nuggets/meat", 32);
        tag(output, "nuggets/chicken", "c:nuggets/chicken", 32);
        tag(output, "nuggets/beef", "c:nuggets/beef", 32);
        tag(output, "nuggets/sheep", "c:nuggets/sheep", 32);
        tag(output, "nuggets/fish", "c:nuggets/fish", 32);

        // derived from real world atomic mass...
        MatterCannonAmmo.ammo(items, output, AppEng.makeId("matter_cannon/nuggets/iron"),
                ConventionTags.IRON_NUGGET, 55.845f);
        MatterCannonAmmo.ammo(items, output, AppEng.makeId("matter_cannon/nuggets/gold"),
                ConventionTags.GOLD_NUGGET, 196.96655f);
        tag(output, "nuggets/lithium", "c:nuggets/lithium", 6.941f);
        tag(output, "nuggets/beryllium", "c:nuggets/beryllium", 9.0122f);
        tag(output, "nuggets/boron", "c:nuggets/boron", 10.811f);
        tag(output, "nuggets/carbon", "c:nuggets/carbon", 12.0107f);
        tag(output, "nuggets/coal", "c:nuggets/coal", 12.0107f);
        tag(output, "nuggets/charcoal", "c:nuggets/charcoal", 12.0107f);
        tag(output, "nuggets/sodium", "c:nuggets/sodium", 22.9897f);
        tag(output, "nuggets/magnesium", "c:nuggets/magnesium", 24.305f);
        tag(output, "nuggets/aluminum", "c:nuggets/aluminum", 26.9815f);
        tag(output, "nuggets/silicon", "c:nuggets/silicon", 28.0855f);
        tag(output, "nuggets/phosphorus", "c:nuggets/phosphorus", 30.9738f);
        tag(output, "nuggets/sulfur", "c:nuggets/sulfur", 32.065f);
        tag(output, "nuggets/potassium", "c:nuggets/potassium", 39.0983f);
        tag(output, "nuggets/calcium", "c:nuggets/calcium", 40.078f);
        tag(output, "nuggets/scandium", "c:nuggets/scandium", 44.9559f);
        tag(output, "nuggets/titanium", "c:nuggets/titanium", 47.867f);
        tag(output, "nuggets/vanadium", "c:nuggets/vanadium", 50.9415f);
        tag(output, "nuggets/manganese", "c:nuggets/manganese", 54.938f);
        tag(output, "nuggets/nickel", "c:nuggets/nickel", 58.6934f);
        tag(output, "nuggets/cobalt", "c:nuggets/cobalt", 58.9332f);
        tag(output, "nuggets/copper", "c:nuggets/copper", 63.546f);
        tag(output, "nuggets/zinc", "c:nuggets/zinc", 65.39f);
        tag(output, "nuggets/gallium", "c:nuggets/gallium", 69.723f);
        tag(output, "nuggets/germanium", "c:nuggets/germanium", 72.64f);
        tag(output, "nuggets/bromine", "c:nuggets/bromine", 79.904f);
        tag(output, "nuggets/krypton", "c:nuggets/krypton", 83.8f);
        tag(output, "nuggets/rubidium", "c:nuggets/rubidium", 85.4678f);
        tag(output, "nuggets/strontium", "c:nuggets/strontium", 87.62f);
        tag(output, "nuggets/yttrium", "c:nuggets/yttrium", 88.9059f);
        tag(output, "nuggets/zirconium", "c:nuggets/zirconium", 91.224f);
        tag(output, "nuggets/niobium", "c:nuggets/niobium", 92.9064f);
        tag(output, "nuggets/technetium", "c:nuggets/technetium", 98f);
        tag(output, "nuggets/ruthenium", "c:nuggets/ruthenium", 101.07f);
        tag(output, "nuggets/rhodium", "c:nuggets/rhodium", 102.9055f);
        tag(output, "nuggets/palladium", "c:nuggets/palladium", 106.42f);
        tag(output, "nuggets/silver", "c:nuggets/silver", 107.8682f);
        tag(output, "nuggets/cadmium", "c:nuggets/cadmium", 112.411f);
        tag(output, "nuggets/indium", "c:nuggets/indium", 114.818f);
        tag(output, "nuggets/tin", "c:nuggets/tin", 118.71f);
        tag(output, "nuggets/antimony", "c:nuggets/antimony", 121.76f);
        tag(output, "nuggets/iodine", "c:nuggets/iodine", 126.9045f);
        tag(output, "nuggets/tellurium", "c:nuggets/tellurium", 127.6f);
        tag(output, "nuggets/xenon", "c:nuggets/xenon", 131.293f);
        tag(output, "nuggets/cesium", "c:nuggets/cesium", 132.9055f);
        tag(output, "nuggets/barium", "c:nuggets/barium", 137.327f);
        tag(output, "nuggets/lanthanum", "c:nuggets/lanthanum", 138.9055f);
        tag(output, "nuggets/cerium", "c:nuggets/cerium", 140.116f);
        tag(output, "nuggets/tantalum", "c:nuggets/tantalum", 180.9479f);
        tag(output, "nuggets/tungsten", "c:nuggets/tungsten", 183.84f);
        tag(output, "nuggets/osmium", "c:nuggets/osmium", 190.23f);
        tag(output, "nuggets/iridium", "c:nuggets/iridium", 192.217f);
        tag(output, "nuggets/platinum", "c:nuggets/platinum", 195.078f);
        tag(output, "nuggets/lead", "c:nuggets/lead", 207.2f);
        tag(output, "nuggets/bismuth", "c:nuggets/bismuth", 208.9804f);
        tag(output, "nuggets/uranium", "c:nuggets/uranium", 238.0289f);
        tag(output, "nuggets/plutonium", "c:nuggets/plutonium", 244);

        // TE stuff...
        tag(output, "nuggets/invar", "c:nuggets/invar", (58.6934f + 55.845f + 55.845f) / 3.0f);
        tag(output, "nuggets/electrum", "c:nuggets/electrum", (107.8682f + 196.96655f) / 2.0f);

        MatterCannonAmmo.ammo(output, AppEng.makeId("matter_cannon/matter_ball"), AEItems.MATTER_BALL, 32.0f);
    }

    private void tag(RecipeOutput output, String recipeId, String tagId, float weight) {
        MatterCannonAmmo.ammo(items, output,
                AppEng.makeId("matter_cannon/" + recipeId),
                TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId)), weight);
    }
}
