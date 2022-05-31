/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.core.definitions.AEItems;
import appeng.items.parts.FacadeItem;

public final class FacadeCreativeTab {

    private static List<ItemStack> subTypes = null;

    private static CreativeModeTab group;

    public static void init() {
        Preconditions.checkState(group == null);
        group = FabricItemGroupBuilder.create(AppEng.makeId("facades")).icon(() -> {
            calculateSubTypes();
            if (subTypes.isEmpty()) {
                return new ItemStack(Items.CAKE);
            }
            return subTypes.get(0);
        }).appendItems(FacadeCreativeTab::fill).build();
    }

    public static CreativeModeTab getGroup() {
        if (group == null) {
            init();
        }
        return group;
    }

    private static void fill(List<ItemStack> items) {
        calculateSubTypes();
        items.addAll(subTypes);
    }

    private static void calculateSubTypes() {
        if (subTypes != null) {
            return;
        }
        subTypes = new ArrayList<>(1000);

        FacadeItem itemFacade = AEItems.FACADE.asItem();
        for (var b : Registry.BLOCK) {
            try {
                var item = Item.byBlock(b);
                if (item == Items.AIR) {
                    continue;
                }

                Item blockItem = b.asItem();
                if (blockItem != Items.AIR && blockItem.getItemCategory() != null) {
                    final NonNullList<ItemStack> tmpList = NonNullList.create();
                    b.fillItemCategory(blockItem.getItemCategory(), tmpList);
                    for (ItemStack l : tmpList) {
                        final ItemStack facade = itemFacade.createFacadeForItem(l, false);
                        if (!facade.isEmpty()) {
                            subTypes.add(facade);
                        }
                    }
                }
            } catch (Throwable t) {
                // just absorb..
            }
        }
    }

    public static List<ItemStack> getSubTypes() {
        calculateSubTypes();
        return subTypes;
    }
}
