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

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.config.TunnelType;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;

public final class InitP2PAttunements {

    private InitP2PAttunements() {
    }

    public static void init() {

        /*
         * light!
         */
        P2PTunnelAttunement.addNewAttunement(Blocks.TORCH, TunnelType.LIGHT);
        P2PTunnelAttunement.addNewAttunement(Blocks.GLOWSTONE, TunnelType.LIGHT);

        /*
         * Forge energy tunnel items
         */
        P2PTunnelAttunement.addNewAttunement(AEBlocks.DENSE_ENERGY_CELL, TunnelType.FE_POWER);
        P2PTunnelAttunement.addNewAttunement(AEBlocks.ENERGY_ACCEPTOR, TunnelType.FE_POWER);
        P2PTunnelAttunement.addNewAttunement(AEBlocks.ENERGY_CELL, TunnelType.FE_POWER);
        P2PTunnelAttunement.addNewAttunement(AEBlocks.CREATIVE_ENERGY_CELL, TunnelType.FE_POWER);

        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_0", 0 ),
        // TunnelType.FE_POWER );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_0", 1 ),
        // TunnelType.FE_POWER );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_0", 2 ),
        // TunnelType.FE_POWER );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_0", 3 ),
        // TunnelType.FE_POWER );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_0", 4 ),
        // TunnelType.FE_POWER );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_0", 5 ),
        // TunnelType.FE_POWER );

        /*
         * EU tunnel items
         */

        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "ic2", "cable", 0 ),
        // TunnelType.IC2_POWER ); // Copper cable
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "ic2", "cable", 1 ),
        // TunnelType.IC2_POWER ); // Glass fibre
        // cable
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "ic2", "cable", 2 ),
        // TunnelType.IC2_POWER ); // Gold cable
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "ic2", "cable", 3 ),
        // TunnelType.IC2_POWER ); // HV cable
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "ic2", "cable", 4 ),
        // TunnelType.IC2_POWER ); // Tin cable

        /*
         * attune based on most redstone base items.
         */
        P2PTunnelAttunement.addNewAttunement(Items.REDSTONE, TunnelType.REDSTONE);
        P2PTunnelAttunement.addNewAttunement(Items.REPEATER, TunnelType.REDSTONE);
        P2PTunnelAttunement.addNewAttunement(Blocks.REDSTONE_LAMP, TunnelType.REDSTONE);
        P2PTunnelAttunement.addNewAttunement(Blocks.COMPARATOR, TunnelType.REDSTONE);
        P2PTunnelAttunement.addNewAttunement(Blocks.DAYLIGHT_DETECTOR, TunnelType.REDSTONE);
        P2PTunnelAttunement.addNewAttunement(Blocks.REDSTONE_WIRE, TunnelType.REDSTONE);
        P2PTunnelAttunement.addNewAttunement(Blocks.REDSTONE_BLOCK, TunnelType.REDSTONE);
        P2PTunnelAttunement.addNewAttunement(Blocks.LEVER, TunnelType.REDSTONE);

        /*
         * attune based on lots of random item related stuff
         */

        P2PTunnelAttunement.addNewAttunement(AEBlocks.ITEM_INTERFACE, TunnelType.ITEM);
        P2PTunnelAttunement.addNewAttunement(AEParts.ITEM_INTERFACE, TunnelType.ITEM);
        P2PTunnelAttunement.addNewAttunement(AEParts.ITEM_STORAGE_BUS, TunnelType.ITEM);
        P2PTunnelAttunement.addNewAttunement(AEParts.IMPORT_BUS, TunnelType.ITEM);
        P2PTunnelAttunement.addNewAttunement(AEParts.EXPORT_BUS, TunnelType.ITEM);

        P2PTunnelAttunement.addNewAttunement(Blocks.HOPPER, TunnelType.ITEM);
        P2PTunnelAttunement.addNewAttunement(Blocks.CHEST, TunnelType.ITEM);
        P2PTunnelAttunement.addNewAttunement(Blocks.TRAPPED_CHEST, TunnelType.ITEM);
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "extrautilities",
        // "extractor_base", 0 ), TunnelType.ITEM );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "mekanism", "parttransmitter", 9
        // ), TunnelType.ITEM );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_32", 0
        // ), TunnelType.ITEM ); //
        // itemduct
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_32", 1
        // ), TunnelType.ITEM ); //
        // itemduct
        // FIXME // (opaque)
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_32", 2
        // ), TunnelType.ITEM ); //
        // impulse
        // FIXME // itemduct
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_32", 3
        // ), TunnelType.ITEM ); //
        // impulse
        // FIXME // itemduct
        // (opaque)

        /*
         * attune based on lots of random item related stuff
         */
        P2PTunnelAttunement.addNewAttunement(Items.BUCKET, TunnelType.FLUID);
        P2PTunnelAttunement.addNewAttunement(Items.LAVA_BUCKET, TunnelType.FLUID);
        P2PTunnelAttunement.addNewAttunement(Items.MILK_BUCKET, TunnelType.FLUID);
        P2PTunnelAttunement.addNewAttunement(Items.WATER_BUCKET, TunnelType.FLUID);
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "mekanism", "machineblock2", 11
        // ), TunnelType.FLUID );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "mekanism", "parttransmitter", 4
        // ), TunnelType.FLUID );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "extrautilities",
        // "extractor_base", 6 ), TunnelType.FLUID );
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_16", 0
        // ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_16", 1
        // ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // (opaque)
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_16", 2
        // ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // hardened
        // FIXME P2PTunnelAttunement.addNewAttunement( P2PTunnelAttunement.getModItem( "thermaldynamics", "duct_16", 3
        // ), TunnelType.FLUID ); //
        // fluiduct
        // FIXME // hardened
        // FIXME // (opaque)
        // FIXME
        for (final AEColor c : AEColor.values()) {
            P2PTunnelAttunement.addNewAttunement(AEParts.GLASS_CABLE.stack(c, 1), TunnelType.ME);
            P2PTunnelAttunement.addNewAttunement(AEParts.COVERED_CABLE.stack(c, 1), TunnelType.ME);
            P2PTunnelAttunement.addNewAttunement(AEParts.SMART_CABLE.stack(c, 1), TunnelType.ME);
            P2PTunnelAttunement.addNewAttunement(AEParts.SMART_DENSE_CABLE.stack(c, 1), TunnelType.ME);
        }

        /*
         * attune based caps
         */
        P2PTunnelAttunement.addNewAttunement(EnergyStorage.ITEM, TunnelType.FE_POWER);
        P2PTunnelAttunement.addNewAttunement(FluidStorage.ITEM, TunnelType.FLUID);

        /*
         * attune based on the ItemStack's modId
         */

        P2PTunnelAttunement.addNewAttunement("thermaldynamics", TunnelType.FE_POWER);
        P2PTunnelAttunement.addNewAttunement("thermalexpansion", TunnelType.FE_POWER);
        P2PTunnelAttunement.addNewAttunement("thermalfoundation", TunnelType.FE_POWER);
        // TODO: Remove when confirmed that the official 1.12 version of EnderIO will
        // support FE.
        P2PTunnelAttunement.addNewAttunement("enderio", TunnelType.FE_POWER);
        // TODO: Remove when confirmed that the official 1.12 version of Mekanism will
        // support FE.
        P2PTunnelAttunement.addNewAttunement("mekanism", TunnelType.FE_POWER);
        // TODO: Remove when support for RFTools' Powercells support is added
        P2PTunnelAttunement.addNewAttunement("rftools", TunnelType.FE_POWER);

    }

    @Nonnull
    private ItemStack getModItem(final String modID, final String name) {
        var item = Registry.ITEM.get(new ResourceLocation(modID + ":" + name));
        return new ItemStack(item);
    }

}
