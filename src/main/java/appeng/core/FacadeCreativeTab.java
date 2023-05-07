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

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;

import appeng.api.ids.AECreativeTabIds;
import appeng.core.definitions.AEItems;

public final class FacadeCreativeTab {

    private static CreativeModeTab group;

    public static void init() {
        Preconditions.checkState(group == null);
        group = FabricItemGroup.builder(AECreativeTabIds.FACADES)
                .icon(() -> {
                    var items = group.getDisplayItems();
                    return items.stream().findFirst().orElse(Items.CAKE.getDefaultInstance());
                })
                .displayItems(FacadeCreativeTab::buildDisplayItems)
                .build();
    }

    public static CreativeModeTab getGroup() {
        if (group == null) {
            init();
        }
        return group;
    }

    private static void buildDisplayItems(CreativeModeTab.ItemDisplayParameters displayParameters,
            CreativeModeTab.Output output) {
        // We need to create our own set since vanilla doesn't allow duplicates, but we cannot guarantee
        // uniqueness
        var facades = ItemStackLinkedSet.createTypeAndTagSet();

        var itemFacade = AEItems.FACADE.asItem();// Collect all variants of this item from creative tabs
        try {
            for (var tab : CreativeModeTabs.allTabs()) {
                if (tab == group) {
                    continue; // Don't recurse
                }
                for (var displayItem : tab.getDisplayItems()) {
                    var facade = itemFacade.createFacadeForItem(displayItem, false);
                    if (!facade.isEmpty()) {
                        facades.add(facade);
                    }
                }
            }
        } catch (Throwable t) {
            // just absorb..
        }

        output.acceptAll(facades);
    }
}
