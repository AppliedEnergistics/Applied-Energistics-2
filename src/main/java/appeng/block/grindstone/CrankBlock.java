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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import appeng.api.implementations.tiles.ICrankable;
import appeng.block.AEBaseTileBlock;
import appeng.core.stats.AeStats;
import appeng.tile.AEBaseTileEntity;
import appeng.tile.grindstone.CrankTileEntity;
import appeng.util.FakePlayer;

public class CrankBlock extends AEBaseTileBlock<CrankTileEntity> {

    public CrankBlock(Properties props) {
        super(props);
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (FakePlayer.isFakePlayer(player) || player == null) {
            this.dropCrank(w, pos);
            return ActionResultType.func_233537_a_(w.isRemote());
        }

        final CrankTileEntity tile = this.getTileEntity(w, pos);
        if (tile != null) {
            if (tile.power()) {
                AeStats.TurnedCranks.addToPlayer(player, 1);
            }
            return ActionResultType.func_233537_a_(w.isRemote());
        }

        return ActionResultType.PASS;
    }

    private void dropCrank(final World world, final BlockPos pos) {
        world.destroyBlock(pos, true);
        world.notifyBlockUpdate(pos, this.getDefaultState(), world.getBlockState(pos), 3);
    }

    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final BlockState state,
            final LivingEntity placer, final ItemStack stack) {
        final AEBaseTileEntity tile = this.getTileEntity(world, pos);
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
    public boolean isValidOrientation(final IWorld w, final BlockPos pos, final Direction forward, final Direction up) {
        final TileEntity te = w.getTileEntity(pos);
        return !(te instanceof CrankTileEntity) || this.isCrankable(w, pos, up.getOpposite());
    }

    private Direction findCrankable(final IBlockReader world, final BlockPos pos) {
        for (final Direction dir : Direction.values()) {
            if (this.isCrankable(world, pos, dir)) {
                return dir;
            }
        }
        return null;
    }

    private boolean isCrankable(final IBlockReader world, final BlockPos pos, final Direction offset) {
        final BlockPos o = pos.offset(offset);
        final TileEntity te = world.getTileEntity(o);

        return te instanceof ICrankable && ((ICrankable) te).canCrankAttach(offset.getOpposite());
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final AEBaseTileEntity tile = this.getTileEntity(world, pos);
        if (tile != null) {
            if (!this.isCrankable(world, pos, tile.getUp().getOpposite())) {
                this.dropCrank(world, pos);
            }
        } else {
            this.dropCrank(world, pos);
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader w, BlockPos pos) {
        return this.findCrankable(w, pos) != null;
    }

    private Direction getUp(IBlockReader world, BlockPos pos) {
        CrankTileEntity crank = getTileEntity(world, pos);
        return crank != null ? crank.getUp() : null;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        Direction up = getUp(world, pos);

        if (up == null) {
            return VoxelShapes.empty();
        } else {
            // FIXME: Cache per direction, and build it 'precise', not just from AABB
            final double xOff = -0.15 * up.getXOffset();
            final double yOff = -0.15 * up.getYOffset();
            final double zOff = -0.15 * up.getZOffset();
            return VoxelShapes.create(
                    new AxisAlignedBB(xOff + 0.15, yOff + 0.15, zOff + 0.15, xOff + 0.85, yOff + 0.85, zOff + 0.85));
        }

    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }
}
