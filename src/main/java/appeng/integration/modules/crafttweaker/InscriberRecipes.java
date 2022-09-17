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
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.IInscriberRegistry;
import appeng.api.features.InscriberProcessType;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;


@ZenClass("mods.appliedenergistics2.Inscriber")
public class InscriberRecipes {
    private InscriberRecipes() {
    }

    @ZenMethod
    public static void addRecipe(IItemStack output, IIngredient input, boolean inscribe, @stanhebben.zenscript.annotations.Optional IIngredient top, @stanhebben.zenscript.annotations.Optional IIngredient bottom) {
        Optional<Collection<ItemStack>> inStacks = CTModule.toStacks(input);
        if (!inStacks.isPresent()) {
            return;
        }

        Collection<ItemStack> topList = CTModule.toStacks(top).orElse(Collections.singleton(ItemStack.EMPTY));
        Collection<ItemStack> bottomList = CTModule.toStacks(bottom).orElse(Collections.singleton(ItemStack.EMPTY));

        for (ItemStack topStack : topList) {
            for (ItemStack bottomStack : bottomList) {
                final IInscriberRecipeBuilder builder = AEApi.instance().registries().inscriber().builder();
                builder.withProcessType(inscribe ? InscriberProcessType.INSCRIBE : InscriberProcessType.PRESS)
                        .withOutput(CTModule.toStack(output))
                        .withInputs(inStacks.get());

                if (!topStack.isEmpty()) {
                    builder.withTopOptional(topStack);
                }
                if (!bottomStack.isEmpty()) {
                    builder.withBottomOptional(bottomStack);
                }
                CTModule.MODIFICATIONS.add(new Add(builder.build()));
            }
        }
    }

    @ZenMethod
    public static void removeRecipe(IItemStack output) {
        CTModule.MODIFICATIONS.add(new Remove((ItemStack) output.getInternal()));
    }

    private static class Add implements IAction {
        private final IInscriberRecipe entry;

        private Add(IInscriberRecipe entry) {
            this.entry = entry;
        }

        @Override
        public void apply() {
            AEApi.instance().registries().inscriber().addRecipe(this.entry);
        }

        @Override
        public String describe() {
            return "Adding Inscriber Entry for " + this.entry.getOutput().getDisplayName();
        }
    }

    private static class Remove implements IAction {
        private final ItemStack stack;

        private Remove(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public void apply() {
            final IInscriberRegistry inscriber = AEApi.instance().registries().inscriber();
            inscriber.getRecipes()
                    .stream()
                    .filter(r -> r.getOutput().isItemEqual(this.stack))
                    .collect(Collectors.toList())
                    .forEach(inscriber::removeRecipe);
        }

        @Override
        public String describe() {
            return "Removing Inscriber Entry for " + this.stack.getDisplayName();
        }
    }

}
