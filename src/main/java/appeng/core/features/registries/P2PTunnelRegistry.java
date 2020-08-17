/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.features.registries;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import alexiil.mc.lib.attributes.Attribute;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;

import appeng.api.config.TunnelType;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.core.Api;

public final class P2PTunnelRegistry implements IP2PTunnelRegistry {
    private static final int INITIAL_CAPACITY = 40;

    private final Map<ItemStack, TunnelType> tunnels = new HashMap<>(INITIAL_CAPACITY);
    private final Map<String, TunnelType> modIdTunnels = new HashMap<>(INITIAL_CAPACITY);
    private final Map<Attribute<?>, TunnelType> attrTunnels = new HashMap<>(INITIAL_CAPACITY);

    public void configure() {

        final IDefinitions definitions = Api.instance().definitions();
        final IBlocks blocks = definitions.blocks();
        final IParts parts = definitions.parts();

        /*
         * light!
         */
        this.addNewAttunement(new ItemStack(Blocks.TORCH), TunnelType.LIGHT);
        this.addNewAttunement(new ItemStack(Blocks.GLOWSTONE), TunnelType.LIGHT);

        /**
         * attune based on most redstone base items.
         */
        this.addNewAttunement(new ItemStack(Items.REDSTONE), TunnelType.REDSTONE);
        this.addNewAttunement(new ItemStack(Items.REPEATER), TunnelType.REDSTONE);
        this.addNewAttunement(new ItemStack(Blocks.REDSTONE_LAMP), TunnelType.REDSTONE);
        this.addNewAttunement(new ItemStack(Blocks.COMPARATOR), TunnelType.REDSTONE);
        this.addNewAttunement(new ItemStack(Blocks.DAYLIGHT_DETECTOR), TunnelType.REDSTONE);
        this.addNewAttunement(new ItemStack(Blocks.REDSTONE_WIRE), TunnelType.REDSTONE);
        this.addNewAttunement(new ItemStack(Blocks.REDSTONE_BLOCK), TunnelType.REDSTONE);
        this.addNewAttunement(new ItemStack(Blocks.LEVER), TunnelType.REDSTONE);

        /**
         * attune based on lots of random item related stuff
         */

        this.addNewAttunement(blocks.iface(), TunnelType.ITEM);
        this.addNewAttunement(parts.iface(), TunnelType.ITEM);
        this.addNewAttunement(parts.storageBus(), TunnelType.ITEM);
        this.addNewAttunement(parts.importBus(), TunnelType.ITEM);
        this.addNewAttunement(parts.exportBus(), TunnelType.ITEM);

        this.addNewAttunement(new ItemStack(Blocks.HOPPER), TunnelType.ITEM);
        this.addNewAttunement(new ItemStack(Blocks.CHEST), TunnelType.ITEM);
        this.addNewAttunement(new ItemStack(Blocks.TRAPPED_CHEST), TunnelType.ITEM);

        /**
         * attune based on lots of random item related stuff
         */
        this.addNewAttunement(new ItemStack(Items.BUCKET), TunnelType.FLUID);
        this.addNewAttunement(new ItemStack(Items.LAVA_BUCKET), TunnelType.FLUID);
        this.addNewAttunement(new ItemStack(Items.MILK_BUCKET), TunnelType.FLUID);
        this.addNewAttunement(new ItemStack(Items.WATER_BUCKET), TunnelType.FLUID);

        for (final AEColor c : AEColor.values()) {
            this.addNewAttunement(parts.cableGlass().stack(c, 1), TunnelType.ME);
            this.addNewAttunement(parts.cableCovered().stack(c, 1), TunnelType.ME);
            this.addNewAttunement(parts.cableSmart().stack(c, 1), TunnelType.ME);
            this.addNewAttunement(parts.cableDenseSmart().stack(c, 1), TunnelType.ME);
        }

        /**
         * attune based caps
         */
        this.addNewAttunement(FluidAttributes.EXTRACTABLE, TunnelType.FLUID);
        this.addNewAttunement(FluidAttributes.INSERTABLE, TunnelType.FLUID);
        this.addNewAttunement(FluidAttributes.FIXED_INV, TunnelType.FLUID);
        this.addNewAttunement(FluidAttributes.GROUPED_INV, TunnelType.FLUID);

    }

    @Override
    public void addNewAttunement(@Nonnull final String modId, @Nullable final TunnelType type) {
        if (type == null || modId == null) {
            return;
        }
        this.modIdTunnels.put(modId, type);
    }

    @Override
    public void addNewAttunement(@Nonnull final Attribute<?> attr, @Nullable final TunnelType type) {
        if (type == null || attr == null) {
            return;
        }
        this.attrTunnels.put(attr, type);
    }

    @Override
    public void addNewAttunement(@Nonnull final ItemStack trigger, @Nullable final TunnelType type) {
        if (type == null || trigger.isEmpty()) {
            return;
        }

        this.tunnels.put(trigger, type);
    }

    @Nullable
    @Override
    public TunnelType getTunnelTypeByItem(final ItemStack trigger) {
        if (!trigger.isEmpty()) {
            // First match exact items
            for (final Entry<ItemStack, TunnelType> entry : this.tunnels.entrySet()) {
                final ItemStack is = entry.getKey();

                if (is.getItem() == trigger.getItem()) {
                    return entry.getValue();
                }

                if (ItemStack.areItemsEqual(is, trigger)) {
                    return entry.getValue();
                }
            }

            // Next, check if the Item you're holding supports any registered capability
            for (Entry<Attribute<?>, TunnelType> entry : this.attrTunnels.entrySet()) {
                if (entry.getKey().getFirstOrNull(trigger) != null) {
                    return entry.getValue();
                }
            }

            // Use the mod id as last option.
            Identifier itemId = Registry.ITEM.getId(trigger.getItem());
            if (itemId == Registry.ITEM.getDefaultId()) {
                return null; // Unregistered item
            }

            for (final Entry<String, TunnelType> entry : this.modIdTunnels.entrySet()) {
                if (itemId.getNamespace().equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    @Nonnull
    private ItemStack getModItem(final String modID, final String name) {

        final Item item = Registry.ITEM.getOrEmpty(new Identifier(modID, name)).orElse(null);

        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item);
    }

    private void addNewAttunement(final IItemDefinition definition, final TunnelType type) {
        definition.maybeStack(1).ifPresent(definitionStack -> this.addNewAttunement(definitionStack, type));
    }
}
