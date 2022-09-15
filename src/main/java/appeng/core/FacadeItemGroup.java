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

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.core.definitions.AEItems;
import appeng.items.parts.FacadeItem;

public final class FacadeItemGroup extends CreativeModeTab {

    public static FacadeItemGroup INSTANCE;

    public static void init() {
        INSTANCE = new FacadeItemGroup();
    }

    private final FacadeItem itemFacade;

    private List<ItemStack> subTypes = null;

    public FacadeItemGroup() {
        super("ae2.facades");

        itemFacade = AEItems.FACADE.asItem();
    }

    @Override
    public ItemStack makeIcon() {
        this.calculateSubTypes();
        if (this.subTypes.isEmpty()) {
            return new ItemStack(Items.CAKE);
        }
        return this.subTypes.get(0);
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items) {
        this.calculateSubTypes();
        items.addAll(subTypes);
    }

    private void calculateSubTypes() {
        if (this.subTypes != null) {
            return;
        }
        this.subTypes = new ArrayList<>(1000);

        for (final Block b : ForgeRegistries.BLOCKS) {
            try {
                final Item item = Item.byBlock(b);
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
                            this.subTypes.add(facade);
                        }
                    }
                }
            } catch (Throwable t) {
                // just absorb..
            }
        }
    }

    public List<ItemStack> getSubTypes() {
        calculateSubTypes();
        return subTypes;
    }
}
