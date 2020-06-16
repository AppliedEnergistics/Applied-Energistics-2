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

import net.minecraft.block.BlockState;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import appeng.block.AEBaseBlock;
import appeng.core.worlddata.WorldData;
import appeng.util.Platform;

public class BlockSkyStone extends AEBaseBlock {
    private static final float BREAK_SPEAK_SCALAR = 0.1f;
    private static final double BREAK_SPEAK_THRESHOLD = 7.0;
    private final SkystoneType type;

    public BlockSkyStone(SkystoneType type, Properties props) {
        super(props);
        this.type = type;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void breakFaster(final PlayerEvent.BreakSpeed event) {
        if (event.getState().getBlock() == this && event.getPlayer() != null) {
            final ItemStack is = event.getPlayer().getItemStackFromSlot(EquipmentSlotType.MAINHAND);
            int level = -1;

            if (!is.isEmpty()) {
                level = is.getItem().getHarvestLevel(is, ToolType.PICKAXE, event.getPlayer(), event.getState());
            }

            if (this.type != SkystoneType.STONE || level >= 3 || event.getOriginalSpeed() > BREAK_SPEAK_THRESHOLD) {
                event.setNewSpeed(event.getNewSpeed() / BREAK_SPEAK_SCALAR);
            }
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
            BlockPos currentPos, BlockPos facingPos) {
        if (worldIn instanceof ServerWorld) {
            WorldData.instance().compassData().service().updateArea((ServerWorld) worldIn, currentPos.getX(),
                    currentPos.getY(), currentPos.getZ());
        }

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void onReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        super.onReplaced(state, w, pos, newState, isMoving);

        if (w instanceof ServerWorld) {
            WorldData.instance().compassData().service().updateArea((ServerWorld) w, pos.getX(), pos.getY(),
                    pos.getZ());
        }
    }

    public enum SkystoneType {
        STONE, BLOCK, BRICK, SMALL_BRICK
    }
}
