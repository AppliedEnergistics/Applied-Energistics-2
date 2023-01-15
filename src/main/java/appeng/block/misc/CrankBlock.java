/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.block.misc;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;

import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.CrankBlockEntity;

public class CrankBlock extends AEBaseEntityBlock<CrankBlockEntity> {

    private static final VoxelShape[] SHAPES = Arrays.stream(Direction.values())
            .map(CrankBlock::createShape)
            .toArray(VoxelShape[]::new);

    public CrankBlock(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player player, InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        if (player instanceof FakePlayer || player == null) {
            this.dropCrank(level, pos);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        var crank = this.getBlockEntity(level, pos);
        if (crank != null) {
            crank.power();
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    private void dropCrank(Level level, BlockPos pos) {
        level.destroyBlock(pos, true);
        level.sendBlockUpdated(pos, defaultBlockState(), level.getBlockState(pos), 3);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        // Does the change originate from the block we're attached to?
        if (getAttachedToPos(state, pos).equals(fromPos)) {
            if (getCrankable(state, level, pos) == null) {
                dropCrank(level, pos);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        if (levelReader instanceof Level level) {
            return getCrankable(state, level, pos) != null;
        } else {
            // We'll allow it for worldgen purposes...
            return true;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var top = getOrientationStrategy().getSide(state, RelativeSide.FRONT);
        return SHAPES[top.ordinal()];
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    public ICrankable getCrankable(BlockState state, Level level, BlockPos pos) {
        // This is facing away from the block the crank is attached to
        var facing = getOrientationStrategy().getFacing(state);
        var attachedToPos = getAttachedToPos(state, pos);
        return ICrankable.get(level, attachedToPos, facing);
    }

    private BlockPos getAttachedToPos(BlockState state, BlockPos pos) {
        var attachedToSide = getOrientationStrategy().getFacing(state).getOpposite();
        return pos.relative(attachedToSide);
    }

    private static VoxelShape createShape(Direction forward) {
        var xOff = -0.15 * forward.getStepX();
        var yOff = -0.15 * forward.getStepY();
        var zOff = -0.15 * forward.getStepZ();
        return Shapes.create(xOff + 0.15, yOff + 0.15, zOff + 0.15,
                xOff + 0.85, yOff + 0.85, zOff + 0.85);
    }
}
