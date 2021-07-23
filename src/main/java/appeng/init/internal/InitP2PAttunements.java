/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.init.internal;

import javax.annotation.Nonnull;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.util.AEColor;
import appeng.capabilities.Capabilities;
import appeng.core.Api;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;

public final class InitP2PAttunements {

    private InitP2PAttunements() {
    }

    public static void init() {

        IP2PTunnelRegistry p2p = Api.instance().registries().p2pTunnel();

        /*
         * light!
         */
        p2p.addNewAttunement(new ItemStack(Blocks.TORCH), TunnelType.LIGHT);
        p2p.addNewAttunement(new ItemStack(Blocks.GLOWSTONE), TunnelType.LIGHT);

        /*
         * Forge energy tunnel items
         */

        addNewAttunement(p2p, AEBlocks.DENSE_ENERGY_CELL, TunnelType.FE_POWER);
        addNewAttunement(p2p, AEBlocks.ENERGY_ACCEPTOR, TunnelType.FE_POWER);
        addNewAttunement(p2p, AEBlocks.ENERGY_CELL, TunnelType.FE_POWER);
        addNewAttunement(p2p, AEBlocks.CREATIVE_ENERGY_CELL, TunnelType.FE_POWER);

        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_0", 0 ), TunnelType.FE_POWER );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_0", 1 ), TunnelType.FE_POWER );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_0", 2 ), TunnelType.FE_POWER );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_0", 3 ), TunnelType.FE_POWER );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_0", 4 ), TunnelType.FE_POWER );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_0", 5 ), TunnelType.FE_POWER );

        /*
         * EU tunnel items
         */

        // FIXME p2p.addNewAttunement( p2p.getModItem( "ic2", "cable", 0 ), TunnelType.IC2_POWER ); // Copper cable
        // FIXME p2p.addNewAttunement( p2p.getModItem( "ic2", "cable", 1 ), TunnelType.IC2_POWER ); // Glass fibre
        // cable
        // FIXME p2p.addNewAttunement( p2p.getModItem( "ic2", "cable", 2 ), TunnelType.IC2_POWER ); // Gold cable
        // FIXME p2p.addNewAttunement( p2p.getModItem( "ic2", "cable", 3 ), TunnelType.IC2_POWER ); // HV cable
        // FIXME p2p.addNewAttunement( p2p.getModItem( "ic2", "cable", 4 ), TunnelType.IC2_POWER ); // Tin cable

        /*
         * attune based on most redstone base items.
         */
        p2p.addNewAttunement(new ItemStack(Items.REDSTONE), TunnelType.REDSTONE);
        p2p.addNewAttunement(new ItemStack(Items.REPEATER), TunnelType.REDSTONE);
        p2p.addNewAttunement(new ItemStack(Blocks.REDSTONE_LAMP), TunnelType.REDSTONE);
        p2p.addNewAttunement(new ItemStack(Blocks.COMPARATOR), TunnelType.REDSTONE);
        p2p.addNewAttunement(new ItemStack(Blocks.DAYLIGHT_DETECTOR), TunnelType.REDSTONE);
        p2p.addNewAttunement(new ItemStack(Blocks.REDSTONE_WIRE), TunnelType.REDSTONE);
        p2p.addNewAttunement(new ItemStack(Blocks.REDSTONE_BLOCK), TunnelType.REDSTONE);
        p2p.addNewAttunement(new ItemStack(Blocks.LEVER), TunnelType.REDSTONE);

        /*
         * attune based on lots of random item related stuff
         */

        addNewAttunement(p2p, AEBlocks.INTERFACE, TunnelType.ITEM);
        addNewAttunement(p2p, AEParts.INTERFACE, TunnelType.ITEM);
        addNewAttunement(p2p, AEParts.STORAGE_BUS, TunnelType.ITEM);
        addNewAttunement(p2p, AEParts.IMPORT_BUS, TunnelType.ITEM);
        addNewAttunement(p2p, AEParts.EXPORT_BUS, TunnelType.ITEM);

        p2p.addNewAttunement(new ItemStack(Blocks.HOPPER), TunnelType.ITEM);
        p2p.addNewAttunement(new ItemStack(Blocks.CHEST), TunnelType.ITEM);
        p2p.addNewAttunement(new ItemStack(Blocks.TRAPPED_CHEST), TunnelType.ITEM);
        // FIXME p2p.addNewAttunement( p2p.getModItem( "extrautilities", "extractor_base", 0 ), TunnelType.ITEM );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "mekanism", "parttransmitter", 9 ), TunnelType.ITEM );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_32", 0 ), TunnelType.ITEM ); //
        // itemduct
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_32", 1 ), TunnelType.ITEM ); //
        // itemduct
        // FIXME // (opaque)
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_32", 2 ), TunnelType.ITEM ); //
        // impulse
        // FIXME // itemduct
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_32", 3 ), TunnelType.ITEM ); //
        // impulse
        // FIXME // itemduct
        // (opaque)

        /*
         * attune based on lots of random item related stuff
         */
        p2p.addNewAttunement(new ItemStack(Items.BUCKET), TunnelType.FLUID);
        p2p.addNewAttunement(new ItemStack(Items.LAVA_BUCKET), TunnelType.FLUID);
        p2p.addNewAttunement(new ItemStack(Items.MILK_BUCKET), TunnelType.FLUID);
        p2p.addNewAttunement(new ItemStack(Items.WATER_BUCKET), TunnelType.FLUID);
        // FIXME p2p.addNewAttunement( p2p.getModItem( "mekanism", "machineblock2", 11 ), TunnelType.FLUID );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "mekanism", "parttransmitter", 4 ), TunnelType.FLUID );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "extrautilities", "extractor_base", 6 ), TunnelType.FLUID );
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_16", 0 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_16", 1 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // (opaque)
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_16", 2 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // hardened
        // FIXME p2p.addNewAttunement( p2p.getModItem( "thermaldynamics", "duct_16", 3 ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // hardened
        // FIXME // (opaque)
        // FIXME
        for (final AEColor c : AEColor.values()) {
            p2p.addNewAttunement(AEParts.GLASS_CABLE.stack(c, 1), TunnelType.ME);
            p2p.addNewAttunement(AEParts.COVERED_CABLE.stack(c, 1), TunnelType.ME);
            p2p.addNewAttunement(AEParts.SMART_CABLE.stack(c, 1), TunnelType.ME);
            p2p.addNewAttunement(AEParts.SMART_DENSE_CABLE.stack(c, 1), TunnelType.ME);
        }

        /*
         * attune based caps
         */
        p2p.addNewAttunement(Capabilities.FORGE_ENERGY, TunnelType.FE_POWER);
        p2p.addNewAttunement(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, TunnelType.FLUID);

        /*
         * attune based on the ItemStack's modId
         */

        p2p.addNewAttunement("thermaldynamics", TunnelType.FE_POWER);
        p2p.addNewAttunement("thermalexpansion", TunnelType.FE_POWER);
        p2p.addNewAttunement("thermalfoundation", TunnelType.FE_POWER);
        // TODO: Remove when confirmed that the official 1.12 version of EnderIO will
        // support FE.
        p2p.addNewAttunement("enderio", TunnelType.FE_POWER);
        // TODO: Remove when confirmed that the official 1.12 version of Mekanism will
        // support FE.
        p2p.addNewAttunement("mekanism", TunnelType.FE_POWER);
        // TODO: Remove when support for RFTools' Powercells support is added
        p2p.addNewAttunement("rftools", TunnelType.FE_POWER);

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

    private static void addNewAttunement(IP2PTunnelRegistry p2p, final ItemDefinition<?> definition,
            final TunnelType type) {
        p2p.addNewAttunement(definition.stack(), type);
    }
}
