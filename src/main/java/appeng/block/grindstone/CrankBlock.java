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

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import appeng.api.implementations.tiles.ICrankable;
import appeng.block.AEBaseTileBlock;
import appeng.core.stats.AeStats;
import appeng.tile.AEBaseBlockEntity;
import appeng.tile.grindstone.CrankBlockEntity;
import appeng.util.FakePlayer;

public class CrankBlock extends AEBaseTileBlock<CrankBlockEntity> {

    public CrankBlock(Settings props) {
        super(props);
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (FakePlayer.isFakePlayer(player) || player == null) {
            this.dropCrank(w, pos);
            return ActionResult.SUCCESS;
        }

        final CrankBlockEntity tile = this.getBlockEntity(w, pos);
        if (tile != null) {
            if (tile.power()) {
                AeStats.TurnedCranks.addToPlayer(player, 1);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private void dropCrank(final World world, final BlockPos pos) {
        world.breakBlock(pos, true);
        world.updateListeners(pos, this.getDefaultState(), world.getBlockState(pos), 3);
    }

    @Override
    public void onPlaced(final World world, final BlockPos pos, final BlockState state, final LivingEntity placer,
            final ItemStack stack) {
        final AEBaseBlockEntity tile = this.getBlockEntity(world, pos);
        if (tile != null) {
            final Direction mnt = this.findCrankable(world, pos);
            Direction forward = Direction.UP;
            if (mnt == Direction.UP || mnt == Direction.DOWN) {
                forward = Direction.SOUTH;
            }
            tile.setOrientation(forward, mnt.getOpposite());
        } else {
            this.dropCrank(world, pos);
        }
    }

    @Override
    public boolean isValidOrientation(final WorldAccess w, final BlockPos pos, final Direction forward,
            final Direction up) {
        final BlockEntity te = w.getBlockEntity(pos);
        return !(te instanceof CrankBlockEntity) || this.isCrankable(w, pos, up.getOpposite());
    }

    private Direction findCrankable(final BlockView world, final BlockPos pos) {
        for (final Direction dir : Direction.values()) {
            if (this.isCrankable(world, pos, dir)) {
                return dir;
            }
        }
        return null;
    }

    private boolean isCrankable(final BlockView world, final BlockPos pos, final Direction offset) {
        final BlockPos o = pos.offset(offset);
        final BlockEntity te = world.getBlockEntity(o);

        return te instanceof ICrankable && ((ICrankable) te).canCrankAttach(offset.getOpposite());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final AEBaseBlockEntity tile = this.getBlockEntity(world, pos);
        if (tile != null) {
            if (!this.isCrankable(world, pos, tile.getUp().getOpposite())) {
                this.dropCrank(world, pos);
            }
        } else {
            this.dropCrank(world, pos);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView w, BlockPos pos) {
        return this.findCrankable(w, pos) != null;
    }

    private Direction getUp(BlockView world, BlockPos pos) {
        CrankBlockEntity crank = getBlockEntity(world, pos);
        return crank != null ? crank.getUp() : null;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Direction up = getUp(world, pos);

        if (up == null) {
            return VoxelShapes.empty();
        } else {
            // FIXME: Cache per direction, and build it 'precise', not just from AABB
            final double xOff = -0.15 * up.getOffsetX();
            final double yOff = -0.15 * up.getOffsetY();
            final double zOff = -0.15 * up.getOffsetZ();
            return VoxelShapes
                    .cuboid(new Box(xOff + 0.15, yOff + 0.15, zOff + 0.15, xOff + 0.85, yOff + 0.85, zOff + 0.85));
        }

    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }
}
