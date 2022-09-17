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


import appeng.integration.abstraction.ICraftTweaker;
import appeng.util.Platform;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.stream.Collectors;


public class CTModule implements ICraftTweaker {
    static final List<IAction> MODIFICATIONS = new ArrayList<>();

    @Override
    public void preInit() {
        CraftTweakerAPI.registerClass(GrinderRecipes.class);
        CraftTweakerAPI.registerClass(InscriberRecipes.class);
        CraftTweakerAPI.registerClass(SpatialRegistry.class);
        CraftTweakerAPI.registerClass(AttunementRegistry.class);
        CraftTweakerAPI.registerClass(CannonRegistry.class);
    }

    @Override
    public void postInit() {
        MODIFICATIONS.forEach(CraftTweakerAPI::apply);
    }

    public static ItemStack toStack(IItemStack iStack) {
        if (iStack == null) {
            return ItemStack.EMPTY;
        } else {
            return (ItemStack) iStack.getInternal();
        }
    }

    public static List<ItemStack> toStackExpand(IItemStack iStack) {
        if (iStack == null) {
            return Collections.emptyList();
        } else {
            ItemStack is = (ItemStack) iStack.getInternal();
            if (!is.isItemStackDamageable() && is.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                NonNullList<ItemStack> ret = NonNullList.create();
                is.getItem().getSubItems(CreativeTabs.SEARCH, ret);
                return ret.stream().map(i -> new ItemStack(i.getItem(), iStack.getAmount(), i.getItemDamage())).collect(Collectors.toList());
            } else {
                return Collections.singletonList(is);
            }
        }
    }

    public static Optional<Collection<ItemStack>> toStacks(IIngredient ingredient) {
        if (ingredient == null) {
            return Optional.empty();
        }
        Set<ItemStack> ret = new TreeSet<>(CTModule::compareItemStacks);
        ingredient.getItems().stream().map(CTModule::toStackExpand).forEach(ret::addAll);
        if (ret.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ret);
    }

    private static int compareItemStacks(ItemStack a, ItemStack b) {
        if (Platform.itemComparisons().isSameItem(a, b)) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return System.identityHashCode(a) - System.identityHashCode(b);
    }
}
