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

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;

public final class InitP2PAttunements {

    private InitP2PAttunements() {
    }

    public static void init() {
        /*
         * Light tunnel
         */
        P2PTunnelAttunement.addItem(Blocks.TORCH, P2PTunnelAttunement.LIGHT_TUNNEL);
        P2PTunnelAttunement.addItem(Blocks.GLOWSTONE, P2PTunnelAttunement.LIGHT_TUNNEL);

        /*
         * Energy tunnel
         */
        P2PTunnelAttunement.addItem(AEBlocks.DENSE_ENERGY_CELL, P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItem(AEBlocks.ENERGY_ACCEPTOR, P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItem(AEBlocks.ENERGY_CELL, P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItem(AEBlocks.CREATIVE_ENERGY_CELL, P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItemByApi(EnergyStorage.ITEM, P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItemByMod("thermaldynamics", P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItemByMod("thermalexpansion", P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItemByMod("thermalfoundation", P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItemByMod("mekanism", P2PTunnelAttunement.ENERGY_TUNNEL);
        P2PTunnelAttunement.addItemByMod("rftools", P2PTunnelAttunement.ENERGY_TUNNEL);

        /*
         * Redstone tunnel
         */
        P2PTunnelAttunement.addItem(Items.REDSTONE, P2PTunnelAttunement.REDSTONE_TUNNEL);
        P2PTunnelAttunement.addItem(Items.REPEATER, P2PTunnelAttunement.REDSTONE_TUNNEL);
        P2PTunnelAttunement.addItem(Items.REDSTONE_LAMP, P2PTunnelAttunement.REDSTONE_TUNNEL);
        P2PTunnelAttunement.addItem(Items.COMPARATOR, P2PTunnelAttunement.REDSTONE_TUNNEL);
        P2PTunnelAttunement.addItem(Items.DAYLIGHT_DETECTOR, P2PTunnelAttunement.REDSTONE_TUNNEL);
        P2PTunnelAttunement.addItem(Items.REDSTONE_TORCH, P2PTunnelAttunement.REDSTONE_TUNNEL);
        P2PTunnelAttunement.addItem(Items.REDSTONE_BLOCK, P2PTunnelAttunement.REDSTONE_TUNNEL);
        P2PTunnelAttunement.addItem(Items.LEVER, P2PTunnelAttunement.REDSTONE_TUNNEL);

        /*
         * Item tunnel
         */
        P2PTunnelAttunement.addItem(AEBlocks.INTERFACE, P2PTunnelAttunement.ITEM_TUNNEL);
        P2PTunnelAttunement.addItem(AEParts.INTERFACE, P2PTunnelAttunement.ITEM_TUNNEL);
        P2PTunnelAttunement.addItem(AEParts.STORAGE_BUS, P2PTunnelAttunement.ITEM_TUNNEL);
        P2PTunnelAttunement.addItem(AEParts.IMPORT_BUS, P2PTunnelAttunement.ITEM_TUNNEL);
        P2PTunnelAttunement.addItem(AEParts.EXPORT_BUS, P2PTunnelAttunement.ITEM_TUNNEL);

        P2PTunnelAttunement.addItem(Blocks.HOPPER, P2PTunnelAttunement.ITEM_TUNNEL);
        P2PTunnelAttunement.addItem(Blocks.CHEST, P2PTunnelAttunement.ITEM_TUNNEL);
        P2PTunnelAttunement.addItem(Blocks.TRAPPED_CHEST, P2PTunnelAttunement.ITEM_TUNNEL);

        /*
         * Fluid tunnel
         */
        P2PTunnelAttunement.addItem(Items.BUCKET, P2PTunnelAttunement.FLUID_TUNNEL);
        P2PTunnelAttunement.addItem(Items.LAVA_BUCKET, P2PTunnelAttunement.FLUID_TUNNEL);
        P2PTunnelAttunement.addItem(Items.MILK_BUCKET, P2PTunnelAttunement.FLUID_TUNNEL);
        P2PTunnelAttunement.addItem(Items.WATER_BUCKET, P2PTunnelAttunement.FLUID_TUNNEL);
        P2PTunnelAttunement.addItemByApi(FluidStorage.ITEM, P2PTunnelAttunement.FLUID_TUNNEL);

        for (var c : AEColor.values()) {
            P2PTunnelAttunement.addItem(AEParts.GLASS_CABLE.item(c), P2PTunnelAttunement.ME_TUNNEL);
            P2PTunnelAttunement.addItem(AEParts.COVERED_CABLE.item(c), P2PTunnelAttunement.ME_TUNNEL);
            P2PTunnelAttunement.addItem(AEParts.SMART_CABLE.item(c), P2PTunnelAttunement.ME_TUNNEL);
            P2PTunnelAttunement.addItem(AEParts.SMART_DENSE_CABLE.item(c), P2PTunnelAttunement.ME_TUNNEL);
        }
    }
}
