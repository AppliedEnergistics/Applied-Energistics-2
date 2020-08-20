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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.api.implementations.items.IAEWrench;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.api.util.DimensionalCoord;
import appeng.block.AEBaseBlock;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.parts.PartPlacement;
import appeng.util.PartHostWrenching;
import appeng.util.Platform;

public class QuartzWrenchItem extends AEBaseItem implements IAEWrench, AEToolItem {

    public QuartzWrenchItem(Item.Settings props) {
        super(props);
    }

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResult.PASS;
        }

        boolean isHoldingShift = player.isInSneakingPose();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        if (!Platform.hasPermissions(new DimensionalCoord(world, pos), player)) {
            return ActionResult.FAIL;
        }

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (isHoldingShift) {

            // Wrenching parts of cable buses or other part hosts
            BlockEntity tile = world.getBlockEntity(pos);
            IPartHost host = null;
            if (tile instanceof IPartHost) {
                host = (IPartHost) tile;
            }

            if (host != null) {
                if (!world.isClient) {
                    // Build the relative position within the part
                    Vec3d relPos = context.getHitPos().subtract(pos.getX(), pos.getY(), pos.getZ());

                    final SelectedPart sp = PartPlacement.selectPart(player, host, relPos);

                    PartHostWrenching.wrenchPart(world, pos, host, sp);
                }
                return ActionResult.SUCCESS;
            }

            // Pass the use onto the block...
            return block.onUse(blockState, world, pos, player, context.getHand(),
                    new BlockHitResult(context.getHitPos(), context.getSide(), pos, context.hitsInsideBlock()));
        }

        if (block instanceof AEBaseBlock) {
            if (Platform.isClient()) {
                // TODO 1.10-R - if we return FAIL on client, action will not be sent to server.
                // Fix that in all Block#onItemUseFirst overrides.
                return !world.isClient ? ActionResult.SUCCESS : ActionResult.PASS;
            }

            AEBaseBlock aeBlock = (AEBaseBlock) block;
            if (aeBlock.rotateAroundFaceAxis(world, pos, context.getSide())) {
                player.swingHand(context.getHand());
                return !world.isClient ? ActionResult.SUCCESS : ActionResult.FAIL;
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final PlayerEntity player, final BlockPos pos) {
        return true;
    }
}
