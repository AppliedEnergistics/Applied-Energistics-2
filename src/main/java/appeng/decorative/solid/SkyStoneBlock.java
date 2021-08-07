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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.player.PlayerEvent;

import appeng.block.AEBaseBlock;
import appeng.core.worlddata.WorldData;

public class SkyStoneBlock extends AEBaseBlock {
    private static final float BREAK_SPEAK_SCALAR = 0.1f;
    private static final double BREAK_SPEAK_THRESHOLD = 7.0;
    private final SkystoneType type;

    public SkyStoneBlock(SkystoneType type, BlockBehaviour.Properties props) {
        super(props);
        this.type = type;

        MinecraftForge.EVENT_BUS.addListener(this::breakFaster);
    }

    private void breakFaster(final PlayerEvent.BreakSpeed event) {
        if (event.getState().getBlock() == this && event.getPlayer() != null) {
            final ItemStack is = event.getPlayer().getItemBySlot(EquipmentSlot.MAINHAND);
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            WorldData.instance().compassData().service().notifyBlockChange(serverLevel, currentPos);
        }

        return super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        super.onRemove(state, level, pos, newState, isMoving);

        if (level instanceof ServerLevel serverLevel) {
            WorldData.instance().compassData().service().notifyBlockChange(serverLevel, pos);
        }
    }

    public enum SkystoneType {
        STONE, BLOCK, BRICK, SMALL_BRICK
    }
}
