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

package appeng.decorative.solid;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import appeng.block.AEBaseBlock;
import appeng.core.worlddata.WorldData;

public class SkyStoneBlock extends AEBaseBlock {
    private static final float BREAK_SPEAK_SCALAR = 0.1f;
    private static final double BREAK_SPEAK_THRESHOLD = 7.0;
    private final SkystoneType type;

    public SkyStoneBlock(SkystoneType type, Settings props) {
        super(props);
        this.type = type;
    }

// FIXME FABRIC
//    @SubscribeEvent
//    public void breakFaster(final PlayerEvent.BreakSpeed event) {
//        if (event.getState().getBlock() == this && event.getPlayer() != null) {
//            final ItemStack is = event.getPlayer().getItemStackFromSlot(EquipmentSlot.MAINHAND);
//            int level = -1;
//
//            if (!is.isEmpty()) {
//                level = is.getItem().getHarvestLevel(is, FabricToolTags.PICKAXES, event.getPlayer(), event.getState());
//            }
//
//            if (this.type != SkystoneType.STONE || level >= 3 || event.getOriginalSpeed() > BREAK_SPEAK_THRESHOLD) {
//                event.setNewSpeed(event.getNewSpeed() / BREAK_SPEAK_SCALAR);
//            }
//        }
//    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState,
            WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (worldIn instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) worldIn;
            WorldData.instance().compassData().service().notifyBlockChange(serverWorld, currentPos);
        }

        return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;
            WorldData.instance().compassData().service().notifyBlockChange(serverWorld, pos);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        super.onStateReplaced(state, w, pos, newState, isMoving);

        if (w instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) w;
            WorldData.instance().compassData().service().notifyBlockChange(serverWorld, pos);
        }
    }

    public enum SkystoneType {
        STONE, BLOCK, BRICK, SMALL_BRICK
    }
}
