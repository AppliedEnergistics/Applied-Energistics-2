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

package appeng.block.grindstone;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;

import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseEntityBlock;
import appeng.tile.grindstone.CrankBlockEntity;

public class CrankBlock extends AEBaseEntityBlock<CrankBlockEntity> implements IOrientableBlock {

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

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack is) {
        super.setPlacedBy(level, pos, state, placer, is);

        var be = getBlockEntity(level, pos);
        if (be != null) {
            var mnt = this.findCrankableDirection(level, pos);
            if (mnt == null) {
                dropCrank(level, pos);
                return;
            }

            Direction forward = Direction.UP;
            if (mnt == Direction.UP || mnt == Direction.DOWN) {
                forward = Direction.SOUTH;
            }
            be.setOrientation(forward, mnt.getOpposite());
        } else {
            dropCrank(level, pos);
        }
    }

    @Override
    protected boolean isValidOrientation(LevelAccessor levelAccessor, BlockPos pos, Direction forward, Direction up) {
        if (levelAccessor instanceof Level level) {
            var be = level.getBlockEntity(pos);
            return !(be instanceof CrankBlockEntity) || isCrankable(level, pos, up.getOpposite());
        } else {
            return true;
        }
    }

    private Direction findCrankableDirection(Level level, BlockPos pos) {
        for (var dir : Direction.values()) {
            if (isCrankable(level, pos, dir)) {
                return dir;
            }
        }
        return null;
    }

    private boolean isCrankable(Level level, BlockPos pos, Direction offset) {
        var o = pos.relative(offset);
        return ICrankable.get(level, o, offset.getOpposite()) != null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        var be = this.getBlockEntity(level, pos);
        if (be != null) {
            if (!isCrankable(level, pos, be.getUp().getOpposite())) {
                dropCrank(level, pos);
            }
        } else {
            dropCrank(level, pos);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos pos) {
        if (levelReader instanceof Level level) {
            return findCrankableDirection(level, pos) != null;
        } else {
            return true;
        }
    }

    private Direction getUp(BlockGetter level, BlockPos pos) {
        var crank = getBlockEntity(level, pos);
        return crank != null ? crank.getUp() : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction up = getUp(level, pos);

        if (up == null) {
            return Shapes.empty();
        } else {
            // FIXME: Cache per direction, and build it 'precise', not just from AABB
            final double xOff = -0.15 * up.getStepX();
            final double yOff = -0.15 * up.getStepY();
            final double zOff = -0.15 * up.getStepZ();
            return Shapes.create(
                    new AABB(xOff + 0.15, yOff + 0.15, zOff + 0.15, xOff + 0.85, yOff + 0.85, zOff + 0.85));
        }

    }
}
