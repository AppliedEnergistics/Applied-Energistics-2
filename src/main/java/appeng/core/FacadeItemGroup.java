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

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.items.parts.FacadeItem;

public final class FacadeItemGroup extends ItemGroup {

    private final FacadeItem itemFacade;

    private List<ItemStack> subTypes = null;

    public FacadeItemGroup() {
        super("appliedenergistics2.facades");

        itemFacade = (FacadeItem) Api.INSTANCE.definitions().items().facade().item();
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
                    for (final ItemStack l : tmpList) {
                        final ItemStack facade = itemFacade.createFacadeForItem(l, false);
                        if (!facade.isEmpty()) {
                            this.subTypes.add(facade);
                        }
                    }
                }
            } catch (final Throwable t) {
                // just absorb..
            }
        }
    }

}
