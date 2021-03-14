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

package appeng.items.tools.quartz;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

import appeng.api.implementations.items.IAEWrench;
import appeng.api.util.DimensionalCoord;
import appeng.block.AEBaseBlock;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class QuartzWrenchItem extends AEBaseItem implements IAEWrench {

    public QuartzWrenchItem(Item.Properties props) {
        super(props);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (!Platform.isEntityHoldingShift(context.getPlayer()) && Platform
                .hasPermissions(new DimensionalCoord(context.getWorld(), context.getPos()), context.getPlayer())) {

            Block block = context.getWorld().getBlockState(context.getPos()).getBlock();
            if (block instanceof AEBaseBlock) {
                if (Platform.isClient()) {
                    // TODO 1.10-R - if we return FAIL on client, action will not be sent to server.
                    // Fix that in all Block#onItemUseFirst overrides.
                    return !context.getWorld().isRemote ? ActionResultType.SUCCESS : ActionResultType.PASS;
                }

                AEBaseBlock aeBlock = (AEBaseBlock) block;
                if (aeBlock.rotateAroundFaceAxis(context.getWorld(), context.getPos(), context.getFace())) {
                    context.getPlayer().swingArm(context.getHand());
                    return !context.getWorld().isRemote ? ActionResultType.SUCCESS : ActionResultType.FAIL;
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final PlayerEntity player, final BlockPos pos) {
        return true;
    }
}
