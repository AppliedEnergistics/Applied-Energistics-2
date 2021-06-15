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
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.config.TunnelType;
import appeng.api.definitions.IItemDefinition;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.capabilities.Capabilities;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiParts;

public final class P2PTunnelRegistry implements IP2PTunnelRegistry {
    private static final int INITIAL_CAPACITY = 40;

    private final Map<ItemStack, TunnelType> tunnels = new HashMap<>(INITIAL_CAPACITY);
    private final Map<String, TunnelType> modIdTunnels = new HashMap<>(INITIAL_CAPACITY);
    private final Map<Capability<?>, TunnelType> capTunnels = new HashMap<>(INITIAL_CAPACITY);

    public void configure() {

        /**
         * light!
         */
        this.addNewAttunement(new ItemStack(Blocks.TORCH), TunnelType.LIGHT);
        this.addNewAttunement(new ItemStack(Blocks.GLOWSTONE), TunnelType.LIGHT);

        /**
         * Forge energy tunnel items
         */

        this.addNewAttunement(ApiBlocks.energyCellDense(), TunnelType.FE_POWER);
        this.addNewAttunement(ApiBlocks.energyAcceptor(), TunnelType.FE_POWER);
        this.addNewAttunement(ApiBlocks.energyCell(), TunnelType.FE_POWER);
        this.addNewAttunement(ApiBlocks.energyCellCreative(), TunnelType.FE_POWER);

        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 0 ), TunnelType.FE_POWER );
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 1 ), TunnelType.FE_POWER );
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 2 ), TunnelType.FE_POWER );
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 3 ), TunnelType.FE_POWER );
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 4 ), TunnelType.FE_POWER );
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_0", 5 ), TunnelType.FE_POWER );

        /**
         * EU tunnel items
         */

        // FIXME this.addNewAttunement( this.getModItem( "ic2", "cable", 0 ), TunnelType.IC2_POWER ); // Copper cable
        // FIXME this.addNewAttunement( this.getModItem( "ic2", "cable", 1 ), TunnelType.IC2_POWER ); // Glass fibre
        // cable
        // FIXME this.addNewAttunement( this.getModItem( "ic2", "cable", 2 ), TunnelType.IC2_POWER ); // Gold cable
        // FIXME this.addNewAttunement( this.getModItem( "ic2", "cable", 3 ), TunnelType.IC2_POWER ); // HV cable
        // FIXME this.addNewAttunement( this.getModItem( "ic2", "cable", 4 ), TunnelType.IC2_POWER ); // Tin cable

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

        this.addNewAttunement(ApiBlocks.iface(), TunnelType.ITEM);
        this.addNewAttunement(ApiParts.iface(), TunnelType.ITEM);
        this.addNewAttunement(ApiParts.storageBus(), TunnelType.ITEM);
        this.addNewAttunement(ApiParts.importBus(), TunnelType.ITEM);
        this.addNewAttunement(ApiParts.exportBus(), TunnelType.ITEM);

        this.addNewAttunement(new ItemStack(Blocks.HOPPER), TunnelType.ITEM);
        this.addNewAttunement(new ItemStack(Blocks.CHEST), TunnelType.ITEM);
        this.addNewAttunement(new ItemStack(Blocks.TRAPPED_CHEST), TunnelType.ITEM);
        // FIXME this.addNewAttunement( this.getModItem( "extrautilities", "extractor_base", 0 ), TunnelType.ITEM );
        // FIXME this.addNewAttunement( this.getModItem( "mekanism", "parttransmitter", 9 ), TunnelType.ITEM );
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 0 ), TunnelType.ITEM ); //
        // itemduct
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 1 ), TunnelType.ITEM ); //
        // itemduct
        // FIXME // (opaque)
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 2 ), TunnelType.ITEM ); //
        // impulse
        // FIXME // itemduct
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_32", 3 ), TunnelType.ITEM ); //
        // impulse
        // FIXME // itemduct
        // (opaque)

        /**
         * attune based on lots of random item related stuff
         */
        this.addNewAttunement(new ItemStack(Items.BUCKET), TunnelType.FLUID);
        this.addNewAttunement(new ItemStack(Items.LAVA_BUCKET), TunnelType.FLUID);
        this.addNewAttunement(new ItemStack(Items.MILK_BUCKET), TunnelType.FLUID);
        this.addNewAttunement(new ItemStack(Items.WATER_BUCKET), TunnelType.FLUID);
        // FIXME this.addNewAttunement( this.getModItem( "mekanism", "machineblock2", 11 ), TunnelType.FLUID );
        // FIXME this.addNewAttunement( this.getModItem( "mekanism", "parttransmitter", 4 ), TunnelType.FLUID );
        // FIXME this.addNewAttunement( this.getModItem( "extrautilities", "extractor_base", 6 ), TunnelType.FLUID );
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 0 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 1 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // (opaque)
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 2 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // hardened
        // FIXME this.addNewAttunement( this.getModItem( "thermaldynamics", "duct_16", 3 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // hardened
        // FIXME // (opaque)
        // FIXME
        for (final AEColor c : AEColor.values()) {
            this.addNewAttunement(ApiParts.cableGlass().stack(c, 1), TunnelType.ME);
            this.addNewAttunement(ApiParts.cableCovered().stack(c, 1), TunnelType.ME);
            this.addNewAttunement(ApiParts.cableSmart().stack(c, 1), TunnelType.ME);
            this.addNewAttunement(ApiParts.cableDenseSmart().stack(c, 1), TunnelType.ME);
        }

        /**
         * attune based caps
         */
        this.addNewAttunement(Capabilities.FORGE_ENERGY, TunnelType.FE_POWER);
        this.addNewAttunement(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, TunnelType.FLUID);

        /**
         * attune based on the ItemStack's modId
         */

        this.addNewAttunement("thermaldynamics", TunnelType.FE_POWER);
        this.addNewAttunement("thermalexpansion", TunnelType.FE_POWER);
        this.addNewAttunement("thermalfoundation", TunnelType.FE_POWER);
        // TODO: Remove when confirmed that the official 1.12 version of EnderIO will
        // support FE.
        this.addNewAttunement("enderio", TunnelType.FE_POWER);
        // TODO: Remove when confirmed that the official 1.12 version of Mekanism will
        // support FE.
        this.addNewAttunement("mekanism", TunnelType.FE_POWER);
        // TODO: Remove when support for RFTools' Powercells support is added
        this.addNewAttunement("rftools", TunnelType.FE_POWER);
        this.addNewAttunement("ic2", TunnelType.IC2_POWER);

    }

    @Override
    public void addNewAttunement(@Nonnull final String modId, @Nullable final TunnelType type) {
        if (type == null || modId == null) {
            return;
        }
        this.modIdTunnels.put(modId, type);
    }

    @Override
    public void addNewAttunement(@Nonnull final Capability<?> cap, @Nullable final TunnelType type) {
        if (type == null || cap == null) {
            return;
        }
        this.capTunnels.put(cap, type);
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
            for (Direction face : Direction.values()) {
                for (Entry<Capability<?>, TunnelType> entry : this.capTunnels.entrySet()) {
                    if (trigger.getCapability(entry.getKey(), face).isPresent()) {
                        return entry.getValue();
                    }
                }
            }

            // Use the mod id as last option.
            for (final Entry<String, TunnelType> entry : this.modIdTunnels.entrySet()) {
                if (trigger.getItem().getRegistryName() != null
                        && trigger.getItem().getRegistryName().getNamespace().equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    @Nonnull
    private ItemStack getModItem(final String modID, final String name) {

        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modID + ":" + name));

        if (item == null) {
            return ItemStack.EMPTY;
        }

        final ItemStack myItemStack = new ItemStack(item, 1);
        return myItemStack;
    }

    private void addNewAttunement(final IItemDefinition definition, final TunnelType type) {
        definition.maybeStack(1).ifPresent(definitionStack -> this.addNewAttunement(definitionStack, type));
    }
}
