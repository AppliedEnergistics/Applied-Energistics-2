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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.CreativeModeTabEvent;

import appeng.api.ids.AECreativeTabIds;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseBlockItem;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.ItemDefinition;
import appeng.items.AEBaseItem;

public final class MainCreativeTab {

    private static final Multimap<CreativeModeTab, ItemDefinition<?>> externalItemDefs = HashMultimap.create();
    private static final List<ItemDefinition<?>> itemDefs = new ArrayList<>();

    public static CreativeModeTab INSTANCE;

    public static void init(CreativeModeTabEvent.Register register) {
        INSTANCE = register.registerCreativeModeTab(AECreativeTabIds.MAIN, builder -> {
            builder
                    .title(Component.translatable("itemGroup.ae2.main"))
                    .icon(() -> AEBlocks.CONTROLLER.stack(1))
                    .displayItems(MainCreativeTab::buildDisplayItems)
                    .build();
        });
    }

    public static void initExternal(CreativeModeTabEvent.BuildContents contents) {
        for (var itemDefinition : externalItemDefs.get(contents.getTab())) {
            contents.accept(itemDefinition);
        }
    }

    public static void add(ItemDefinition<?> itemDef) {
        itemDefs.add(itemDef);
    }

    public static void addExternal(CreativeModeTab tab, ItemDefinition<?> itemDef) {
        externalItemDefs.put(tab, itemDef);
    }

    private static void buildDisplayItems(FeatureFlagSet featureFlagSet, CreativeModeTab.Output output,
            boolean opItems) {
        for (var itemDef : itemDefs) {
            var item = itemDef.asItem();

            // For block items, the block controls the creative tab
            if (item instanceof AEBaseBlockItem baseItem
                    && baseItem.getBlock() instanceof AEBaseBlock baseBlock) {
                baseBlock.addToMainCreativeTab(output);
            } else if (item instanceof AEBaseItem baseItem) {
                baseItem.addToMainCreativeTab(output);
            } else {
                output.accept(itemDef);
            }
        }
    }
}
