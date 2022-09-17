/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.crafttweaker;


import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipe;
import appeng.api.features.IGrinderRecipeBuilder;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Collection;
import java.util.Collections;


@ZenClass("mods.appliedenergistics2.Grinder")
public class GrinderRecipes {
    private GrinderRecipes() {
    }

    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input, int turns, @stanhebben.zenscript.annotations.Optional IItemStack secondary1Output, @stanhebben.zenscript.annotations.Optional Float secondary1Chance, @stanhebben.zenscript.annotations.Optional IItemStack secondary2Output, @stanhebben.zenscript.annotations.Optional Float secondary2Chance) {
        Collection<ItemStack> inStacks = CTModule.toStacks(input).orElse(Collections.emptySet());

        for (ItemStack inStack : inStacks) {
            IGrinderRecipeBuilder builder = AEApi.instance().registries().grinder().builder();
            builder.withInput(inStack)
                    .withOutput(CTModule.toStack(output))
                    .withTurns(turns);

            final ItemStack s1 = CTModule.toStack(secondary1Output);
            if (!s1.isEmpty()) {
                builder.withFirstOptional(s1, secondary1Chance == null ? 1.0f : secondary1Chance);
            }
            final ItemStack s2 = CTModule.toStack(secondary2Output);
            if (!s2.isEmpty()) {
                builder.withSecondOptional(s2, secondary2Chance == null ? 1.0f : secondary2Chance);
            }
            CTModule.MODIFICATIONS.add(new Add(builder.build()));
        }
    }

    @ZenMethod
    public static void removeRecipe(IIngredient input) {
        for (ItemStack inStack : CTModule.toStacks(input).orElse(Collections.emptySet())) {
            CTModule.MODIFICATIONS.add(new Remove(inStack));
        }
    }

    private static class Add implements IAction {
        private final IGrinderRecipe entry;

        private Add(IGrinderRecipe entry) {
            this.entry = entry;
        }

        @Override
        public void apply() {
            AEApi.instance().registries().grinder().addRecipe(this.entry);
        }

        @Override
        public String describe() {
            return "Adding Grinder Entry for " + this.entry.getInput().getDisplayName();
        }
    }

    private static class Remove implements IAction {
        private final ItemStack stack;

        private Remove(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void apply() {
            IGrinderRecipe recipe = AEApi.instance().registries().grinder().getRecipeForInput(this.stack);
            if (recipe != null) {
                AEApi.instance().registries().grinder().removeRecipe(recipe);
            }
        }

        @Override
        public String describe() {
            return "Removing Grinder Entry for " + this.stack.getDisplayName();
        }
    }
}
