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

import com.google.common.primitives.Ints;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.integration.modules.theoneprobe.TheOneProbeText;

public class CraftingMonitorInfoProvider implements IBlockEntityInfoProvider {

    @Override
    public void addProbeInfo(AEBaseBlockEntity blockEntity, ProbeMode mode, IProbeInfo probeInfo, Player player,
            Level level, BlockState blockState, IProbeHitData data) {
        if (blockEntity instanceof CraftingMonitorBlockEntity monitor) {
            var stack = monitor.getJobProgress();

            if (stack != null) {

                final IProbeInfo centerAlignedHorizontalLayout = probeInfo
                        .horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));

                if (stack.what() instanceof AEItemKey itemKey) {
                    centerAlignedHorizontalLayout.item(itemKey.toStack(Ints.saturatedCast(stack.amount())));
                }
                centerAlignedHorizontalLayout.text(
                        TheOneProbeText.CRAFTING.getTranslationComponent(stack.what().getDisplayName()));
            }
        }
    }

}
