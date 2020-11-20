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

package appeng.bootstrap;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

import appeng.api.features.AEFeature;
import appeng.core.features.ItemDefinition;

/**
 * Allows an item to be defined and registered with the game. The item is only registered once build is called.
 */
public interface IItemBuilder {
    IItemBuilder bootstrap(Function<Item, IBootstrapComponent> component);

    IItemBuilder features(AEFeature... features);

    IItemBuilder addFeatures(AEFeature... features);

    IItemBuilder itemGroup(ItemGroup tab);

    IItemBuilder props(Consumer<Item.Settings> customizer);

    IItemBuilder rendering(ItemRenderingCustomizer callback);

    /**
     * Registers a custom dispenser behavior for this item.
     */
    IItemBuilder dispenserBehavior(Supplier<DispenserBehavior> behavior);

    ItemDefinition build();
}
