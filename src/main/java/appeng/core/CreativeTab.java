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

import appeng.core.Api;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public final class CreativeTab {

    private static final List<IItemDefinition> itemDefs = new ArrayList<>();

    public static ItemGroup INSTANCE;

    public static void init() {
        INSTANCE = FabricItemGroupBuilder.create(AppEng.makeId("main"))
                .icon(() -> {
                    final IDefinitions definitions = Api.instance().definitions();
                    final IBlocks blocks = definitions.blocks();
                    return blocks.controller().stack(1);
                })
                .appendItems(CreativeTab::fill)
                .build();
    }

    public static void add(IItemDefinition itemDef) {
        itemDefs.add(itemDef);
    }

    private static void fill(List<ItemStack> items) {
        for (IItemDefinition itemDef : itemDefs) {
            itemDef.item().appendStacks(INSTANCE, new ListWrapper(items));
        }
    }

    private static class ListWrapper extends DefaultedList<ItemStack> {

        public ListWrapper(List<ItemStack> items) {
            super(items, ItemStack.EMPTY);
        }
    }

}