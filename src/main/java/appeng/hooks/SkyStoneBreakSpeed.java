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

package appeng.hooks;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

import appeng.core.definitions.AEBlocks;

/**
 * This hook is intended to essentially make sky stone blocks found in meteorites minable with iron tools while
 * multiplying their destroy time by 10. To accomplish this, the blocks are created with destroy time 50, and their
 * destroy time is divided by 10 if a tool _better_ than iron is used.
 */
public final class SkyStoneBreakSpeed {
    public static final int SPEEDUP_FACTOR = 10;

    private SkyStoneBreakSpeed() {
    }

    public static Float handleBreakFaster(Player player, BlockState blockState, float speed) {
        if (blockState.getBlock() == AEBlocks.SKY_STONE_BLOCK.block()) {
            var tool = player.getItemBySlot(EquipmentSlot.MAINHAND);
            if (tool.getDestroySpeed(blockState) > Tiers.IRON.getSpeed()) {
                return speed * SPEEDUP_FACTOR;
            }
        }
        return null;
    }
}
