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

import javax.annotation.Nullable;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.FakePlayer;

import appeng.api.implementations.blockentities.ICrankable;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.grindstone.CrankBlockEntity;
import appeng.core.stats.AeStats;

public class CrankBlock extends AEBaseEntityBlock<CrankBlockEntity> {

    public CrankBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public InteractionResult onActivated(final Level w, final BlockPos pos, final Player player,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (player instanceof FakePlayer || player == null) {
            this.dropCrank(w, pos);
            return InteractionResult.sidedSuccess(w.isClientSide());
        }

        final CrankBlockEntity blockEntity = this.getBlockEntity(w, pos);
        if (blockEntity != null) {
            if (blockEntity.power()) {
                AeStats.TurnedCranks.addToPlayer(player, 1);
            }
            return InteractionResult.sidedSuccess(w.isClientSide());
        }

        return InteractionResult.PASS;
    }

    private void dropCrank(final Level level, final BlockPos pos) {
        level.destroyBlock(pos, true);
        level.sendBlockUpdated(pos, this.defaultBlockState(), level.getBlockState(pos), 3);
    }

    @Override
    public void setPlacedBy(final Level level, final BlockPos pos, final BlockState state,
            final LivingEntity placer, final ItemStack stack) {
        final AEBaseBlockEntity blockEntity = this.getBlockEntity(level, pos);
        if (blockEntity != null) {
            final Direction mnt = this.findCrankable(level, pos);
            Direction forward = Direction.UP;
            if (mnt == Direction.UP || mnt == Direction.DOWN) {
                forward = Direction.SOUTH;
            }
            blockEntity.setOrientation(forward, mnt.getOpposite());
        } else {
            this.dropCrank(level, pos);
        }
    }

    @Override
    public boolean isValidOrientation(final LevelAccessor w, final BlockPos pos, final Direction forward,
            final Direction up) {
        final BlockEntity te = w.getBlockEntity(pos);
        return !(te instanceof CrankBlockEntity) || this.isCrankable(w, pos, up.getOpposite());
    }

    private Direction findCrankable(final BlockGetter level, final BlockPos pos) {
        for (final Direction dir : Direction.values()) {
            if (this.isCrankable(level, pos, dir)) {
                return dir;
            }
        }
        return null;
    }

    private boolean isCrankable(final BlockGetter level, final BlockPos pos, final Direction offset) {
        final BlockPos o = pos.relative(offset);
        final BlockEntity te = level.getBlockEntity(o);

        return te instanceof ICrankable && ((ICrankable) te).canCrankAttach(offset.getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final AEBaseBlockEntity blockEntity = this.getBlockEntity(level, pos);
        if (blockEntity != null) {
            if (!this.isCrankable(level, pos, blockEntity.getUp().getOpposite())) {
                this.dropCrank(level, pos);
            }
        } else {
            this.dropCrank(level, pos);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader w, BlockPos pos) {
        return this.findCrankable(w, pos) != null;
    }

    private Direction getUp(BlockGetter level, BlockPos pos) {
        CrankBlockEntity crank = getBlockEntity(level, pos);
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
