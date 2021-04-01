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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
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

    public QuartzWrenchItem(Item.Properties props) {
        super(props);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.field_5811;
        }

        boolean isHoldingShift = player.isCrouching();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (!Platform.hasPermissions(new DimensionalCoord(world, pos), player)) {
            return ActionResultType.field_5814;
        }

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (isHoldingShift) {

            // Wrenching parts of cable buses or other part hosts
            TileEntity tile = world.getTileEntity(pos);
            IPartHost host = null;
            if (tile instanceof IPartHost) {
                host = (IPartHost) tile;
            }

            if (host != null) {
                if (!world.isRemote) {
                    // Build the relative position within the part
                    Vector3d relPos = context.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());

                    final SelectedPart sp = PartPlacement.selectPart(player, host, relPos);

                    PartHostWrenching.wrenchPart(world, pos, host, sp);
                }
                return ActionResultType.field_5812;
            }

            // Pass the use onto the block...
            return block.onBlockActivated(blockState, world, pos, player, context.getHand(),
                    new BlockRayTraceResult(context.getHitVec(), context.getFace(), pos, context.isInside()));
        }

        if (block instanceof AEBaseBlock) {
            if (!world.isRemote) {
                AEBaseBlock aeBlock = (AEBaseBlock) block;
                if (aeBlock.rotateAroundFaceAxis(world, pos, context.getFace())) {
                    player.swingArm(context.getHand());
                }
            }
            return ActionResultType.field_5812;
        }
        return ActionResultType.field_5811;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final PlayerEntity player, final BlockPos pos) {
        return true;
    }
}
