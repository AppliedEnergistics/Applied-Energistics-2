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

package appeng.integration.modules.theoneprobe.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import alexiil.mc.lib.attributes.item.ItemTransferable;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.tile.AEBaseBlockEntity;
import appeng.tile.misc.ChargerBlockEntity;

public class ChargerInfoProvider implements ITileProbInfoProvider {

    @Override
    public void addProbeInfo(AEBaseBlockEntity tile, ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player,
                             World world, BlockState blockState, IProbeHitData data) {
        if (tile instanceof ChargerBlockEntity) {
            final ChargerBlockEntity charger = (ChargerBlockEntity) tile;
            final ItemTransferable chargerInventory = charger.getInternalInventory();
            final ItemStack chargingItem = chargerInventory.getStackInSlot(0);

            if (!chargingItem.isEmpty()) {
                final String currentInventory = chargingItem.getName().getString();
                final IProbeInfo centerAlignedHorizontalLayout = probeInfo
                        .horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));

                centerAlignedHorizontalLayout.item(chargingItem);
                centerAlignedHorizontalLayout.text(currentInventory);
            }
        }
    }

}
