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

package appeng.integration.modules.theoneprobe.blockentity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.storage.data.IAEItemStack;
import appeng.integration.modules.theoneprobe.TheOneProbeText;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;

public class CraftingMonitorInfoProvider implements IBlockEntityInfoProvider {

    @Override
    public void addProbeInfo(AEBaseBlockEntity tile, ProbeMode mode, IProbeInfo probeInfo, Player player,
                             Level world, BlockState blockState, IProbeHitData data) {
        if (tile instanceof CraftingMonitorBlockEntity) {
            final CraftingMonitorBlockEntity monitor = (CraftingMonitorBlockEntity) tile;
            final IAEItemStack displayStack = monitor.getJobProgress();

            if (displayStack != null) {
                // TODO: check if OK
                final ItemStack itemStack = displayStack.asItemStackRepresentation();
                final String itemName = itemStack.getHoverName().getString();
                final Component formattedCrafting = TheOneProbeText.CRAFTING.getTranslationComponent(itemName);

                final IProbeInfo centerAlignedHorizontalLayout = probeInfo
                        .horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));

                centerAlignedHorizontalLayout.item(itemStack);
                centerAlignedHorizontalLayout.text(formattedCrafting);
            }
        }
    }

}
