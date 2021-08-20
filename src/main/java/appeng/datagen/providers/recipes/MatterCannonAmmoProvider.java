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

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import appeng.core.AppEng;
import appeng.core.definitions.AEItems;

public class MatterCannonAmmoProvider extends AE2RecipeProvider {
    public MatterCannonAmmoProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {
        registerAmmoTag(consumer, "nuggets/meatraw", new ResourceLocation("c:meatraw_nuggets"), 32);
        registerAmmoTag(consumer, "nuggets/meatcooked", new ResourceLocation("c:meatcooked_nuggets"), 32);
        registerAmmoTag(consumer, "nuggets/meat", new ResourceLocation("c:meat_nuggets"), 32);
        registerAmmoTag(consumer, "nuggets/chicken", new ResourceLocation("c:chicken_nuggets"), 32);
        registerAmmoTag(consumer, "nuggets/beef", new ResourceLocation("c:beef_nuggets"), 32);
        registerAmmoTag(consumer, "nuggets/sheep", new ResourceLocation("c:sheep_nuggets"), 32);
        registerAmmoTag(consumer, "nuggets/fish", new ResourceLocation("c:fish_nuggets"), 32);

        // derived from real world atomic mass...
        registerAmmoTag(consumer, "nuggets/lithium", new ResourceLocation("c:lithium_nuggets"), 6.941f);
        registerAmmoTag(consumer, "nuggets/beryllium", new ResourceLocation("c:beryllium_nuggets"), 9.0122f);
        registerAmmoTag(consumer, "nuggets/boron", new ResourceLocation("c:boron_nuggets"), 10.811f);
        registerAmmoTag(consumer, "nuggets/carbon", new ResourceLocation("c:carbon_nuggets"), 12.0107f);
        registerAmmoTag(consumer, "nuggets/coal", new ResourceLocation("c:coal_nuggets"), 12.0107f);
        registerAmmoTag(consumer, "nuggets/charcoal", new ResourceLocation("c:charcoal_nuggets"), 12.0107f);
        registerAmmoTag(consumer, "nuggets/sodium", new ResourceLocation("c:sodium_nuggets"), 22.9897f);
        registerAmmoTag(consumer, "nuggets/magnesium", new ResourceLocation("c:magnesium_nuggets"), 24.305f);
        registerAmmoTag(consumer, "nuggets/aluminum", new ResourceLocation("c:aluminum_nuggets"), 26.9815f);
        registerAmmoTag(consumer, "nuggets/silicon", new ResourceLocation("c:silicon_nuggets"), 28.0855f);
        registerAmmoTag(consumer, "nuggets/phosphorus", new ResourceLocation("c:phosphorus_nuggets"), 30.9738f);
        registerAmmoTag(consumer, "nuggets/sulfur", new ResourceLocation("c:sulfur_nuggets"), 32.065f);
        registerAmmoTag(consumer, "nuggets/potassium", new ResourceLocation("c:potassium_nuggets"), 39.0983f);
        registerAmmoTag(consumer, "nuggets/calcium", new ResourceLocation("c:calcium_nuggets"), 40.078f);
        registerAmmoTag(consumer, "nuggets/scandium", new ResourceLocation("c:scandium_nuggets"), 44.9559f);
        registerAmmoTag(consumer, "nuggets/titanium", new ResourceLocation("c:titanium_nuggets"), 47.867f);
        registerAmmoTag(consumer, "nuggets/vanadium", new ResourceLocation("c:vanadium_nuggets"), 50.9415f);
        registerAmmoTag(consumer, "nuggets/manganese", new ResourceLocation("c:manganese_nuggets"), 54.938f);
        registerAmmoTag(consumer, "nuggets/iron", new ResourceLocation("c:iron_nuggets"), 55.845f);
        registerAmmoTag(consumer, "nuggets/gold", new ResourceLocation("c:gold_nuggets"), 196.96655f);
        registerAmmoTag(consumer, "nuggets/nickel", new ResourceLocation("c:nickel_nuggets"), 58.6934f);
        registerAmmoTag(consumer, "nuggets/cobalt", new ResourceLocation("c:cobalt_nuggets"), 58.9332f);
        registerAmmoTag(consumer, "nuggets/copper", new ResourceLocation("c:copper_nuggets"), 63.546f);
        registerAmmoTag(consumer, "nuggets/zinc", new ResourceLocation("c:zinc_nuggets"), 65.39f);
        registerAmmoTag(consumer, "nuggets/gallium", new ResourceLocation("c:gallium_nuggets"), 69.723f);
        registerAmmoTag(consumer, "nuggets/germanium", new ResourceLocation("c:germanium_nuggets"), 72.64f);
        registerAmmoTag(consumer, "nuggets/bromine", new ResourceLocation("c:bromine_nuggets"), 79.904f);
        registerAmmoTag(consumer, "nuggets/krypton", new ResourceLocation("c:krypton_nuggets"), 83.8f);
        registerAmmoTag(consumer, "nuggets/rubidium", new ResourceLocation("c:rubidium_nuggets"), 85.4678f);
        registerAmmoTag(consumer, "nuggets/strontium", new ResourceLocation("c:strontium_nuggets"), 87.62f);
        registerAmmoTag(consumer, "nuggets/yttrium", new ResourceLocation("c:yttrium_nuggets"), 88.9059f);
        registerAmmoTag(consumer, "nuggets/zirconium", new ResourceLocation("c:zirconium_nuggets"), 91.224f);
        registerAmmoTag(consumer, "nuggets/niobium", new ResourceLocation("c:niobium_nuggets"), 92.9064f);
        registerAmmoTag(consumer, "nuggets/technetium", new ResourceLocation("c:technetium_nuggets"), 98f);
        registerAmmoTag(consumer, "nuggets/ruthenium", new ResourceLocation("c:ruthenium_nuggets"), 101.07f);
        registerAmmoTag(consumer, "nuggets/rhodium", new ResourceLocation("c:rhodium_nuggets"), 102.9055f);
        registerAmmoTag(consumer, "nuggets/palladium", new ResourceLocation("c:palladium_nuggets"), 106.42f);
        registerAmmoTag(consumer, "nuggets/silver", new ResourceLocation("c:silver_nuggets"), 107.8682f);
        registerAmmoTag(consumer, "nuggets/cadmium", new ResourceLocation("c:cadmium_nuggets"), 112.411f);
        registerAmmoTag(consumer, "nuggets/indium", new ResourceLocation("c:indium_nuggets"), 114.818f);
        registerAmmoTag(consumer, "nuggets/tin", new ResourceLocation("c:tin_nuggets"), 118.71f);
        registerAmmoTag(consumer, "nuggets/antimony", new ResourceLocation("c:antimony_nuggets"), 121.76f);
        registerAmmoTag(consumer, "nuggets/iodine", new ResourceLocation("c:iodine_nuggets"), 126.9045f);
        registerAmmoTag(consumer, "nuggets/tellurium", new ResourceLocation("c:tellurium_nuggets"), 127.6f);
        registerAmmoTag(consumer, "nuggets/xenon", new ResourceLocation("c:xenon_nuggets"), 131.293f);
        registerAmmoTag(consumer, "nuggets/cesium", new ResourceLocation("c:cesium_nuggets"), 132.9055f);
        registerAmmoTag(consumer, "nuggets/barium", new ResourceLocation("c:barium_nuggets"), 137.327f);
        registerAmmoTag(consumer, "nuggets/lanthanum", new ResourceLocation("c:lanthanum_nuggets"), 138.9055f);
        registerAmmoTag(consumer, "nuggets/cerium", new ResourceLocation("c:cerium_nuggets"), 140.116f);
        registerAmmoTag(consumer, "nuggets/tantalum", new ResourceLocation("c:tantalum_nuggets"), 180.9479f);
        registerAmmoTag(consumer, "nuggets/tungsten", new ResourceLocation("c:tungsten_nuggets"), 183.84f);
        registerAmmoTag(consumer, "nuggets/osmium", new ResourceLocation("c:osmium_nuggets"), 190.23f);
        registerAmmoTag(consumer, "nuggets/iridium", new ResourceLocation("c:iridium_nuggets"), 192.217f);
        registerAmmoTag(consumer, "nuggets/platinum", new ResourceLocation("c:platinum_nuggets"), 195.078f);
        registerAmmoTag(consumer, "nuggets/lead", new ResourceLocation("c:lead_nuggets"), 207.2f);
        registerAmmoTag(consumer, "nuggets/bismuth", new ResourceLocation("c:bismuth_nuggets"), 208.9804f);
        registerAmmoTag(consumer, "nuggets/uranium", new ResourceLocation("c:uranium_nuggets"), 238.0289f);
        registerAmmoTag(consumer, "nuggets/plutonium", new ResourceLocation("c:plutonium_nuggets"), 244);

        // TE stuff...
        registerAmmoTag(consumer, "nuggets/invar", new ResourceLocation("c:invar_nuggets"),
                (58.6934f + 55.845f + 55.845f) / 3.0f);
        registerAmmoTag(consumer, "nuggets/electrum", new ResourceLocation("c:electrum_nuggets"),
                (107.8682f + 196.96655f) / 2.0f);

        registerAmmoItem(consumer, "matter_ball", AEItems.MATTER_BALL, 32.0f);
    }

    private void registerAmmoItem(Consumer<FinishedRecipe> consumer, String id, ItemLike item, float weight) {
        consumer.accept(new MatterCannonAmmo(
                AppEng.makeId("matter_cannon/" + id),
                null,
                item.asItem(),
                weight));
    }

    private void registerAmmoTag(Consumer<FinishedRecipe> consumer, String id, ResourceLocation tag, float weight) {
        consumer.accept(new MatterCannonAmmo(
                AppEng.makeId("matter_cannon/" + id),
                (Tag.Named<Item>) TagRegistry.item(tag),
                null,
                weight));
    }
}
