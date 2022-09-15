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

import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.CreativeModeTabEvent;

import appeng.api.ids.AECreativeTabIds;
import appeng.core.definitions.AEItems;

public final class FacadeItemGroup {

    private static CreativeModeTab group;

    public static void init(CreativeModeTabEvent.Register register) {
        Preconditions.checkState(group == null);

        group = register.registerCreativeModeTab(AECreativeTabIds.FACADES, builder -> {

            builder
                    .title(Component.translatable("itemGroup.ae2.facades"))
                    .icon(() -> {
                        var items = group.getDisplayItems();
                        return items.stream().findFirst().orElse(Items.CAKE.getDefaultInstance());
                    })
                    .displayItems(FacadeItemGroup::buildDisplayItems)
                    .build();
        });
    }

    public static CreativeModeTab getGroup() {
        if (group == null) {
            throw new IllegalStateException();
        }
        return group;
    }

    private static void buildDisplayItems(FeatureFlagSet featureFlagSet, CreativeModeTab.Output output,
            boolean opItems) {
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
