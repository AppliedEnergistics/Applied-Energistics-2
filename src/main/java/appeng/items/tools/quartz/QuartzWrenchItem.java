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
import appeng.util.InteractionUtil;
import appeng.util.PartHostWrenching;
import appeng.util.Platform;

public class QuartzWrenchItem extends AEBaseItem implements IAEWrench, AEToolItem {

    public QuartzWrenchItem(Item.Properties props) {
        super(props);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        PlayerEntity p = context.getPlayer();
        if (p == null) {
            return ActionResultType.PASS;
        }

        boolean isHoldingShift = InteractionUtil.isInAlternateUseMode(p);
        World w = context.getWorld();
        BlockPos pos = context.getPos();
        if (!Platform.hasPermissions(new DimensionalCoord(w, pos), p)) {
            return ActionResultType.FAIL;
        }

        BlockState blockState = w.getBlockState(pos);
        Block block = blockState.getBlock();

        if (isHoldingShift) {

            // Wrenching parts of cable buses or other part hosts
            TileEntity tile = w.getTileEntity(pos);
            IPartHost host = null;
            if (tile instanceof IPartHost) {
                host = (IPartHost) tile;
            }

            if (host != null) {
                if (!w.isRemote) {
                    // Build the relative position within the part
                    Vector3d relPos = context.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());

                    final SelectedPart sp = PartPlacement.selectPart(p, host, relPos);

                    PartHostWrenching.wrenchPart(w, pos, host, sp);
                }
                return ActionResultType.func_233537_a_(w.isRemote);
            }

            // Pass the use onto the block...
            return block.onBlockActivated(blockState, w, pos, p, context.getHand(),
                    new BlockRayTraceResult(context.getHitVec(), context.getFace(), pos, context.isInside()));
        }

        if (block instanceof AEBaseBlock) {
            if (!w.isRemote) {
                AEBaseBlock aeBlock = (AEBaseBlock) block;
                if (aeBlock.rotateAroundFaceAxis(w, pos, context.getFace())) {
                    p.swingArm(context.getHand());
                }
            }
            return ActionResultType.func_233537_a_(w.isRemote);
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canWrench(final ItemStack wrench, final PlayerEntity player, final BlockPos pos) {
        return true;
    }
}
