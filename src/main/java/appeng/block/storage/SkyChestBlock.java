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

package appeng.block.storage;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
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
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.SkyChestContainer;
import appeng.tile.storage.SkyChestTileEntity;

public class SkyChestBlock extends AEBaseTileBlock<SkyChestTileEntity> implements IWaterLoggable {

    private static final double AABB_OFFSET_BOTTOM = 0.00;
    private static final double AABB_OFFSET_SIDES = 0.06;
    private static final double AABB_OFFSET_TOP = 0.0625;

    // Precomputed bounding boxes of the chest, sorted into the map by the UP
    // direction
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    static {
        for (Direction up : Direction.values()) {
            AxisAlignedBB aabb = computeAABB(up);
            SHAPES.put(up, VoxelShapes.create(aabb));
        }
    }

    public enum SkyChestType {
        STONE, BLOCK
    }

    public final SkyChestType type;

    public SkyChestBlock(final SkyChestType type, Properties props) {
        super(props);
        this.type = type;
        setDefaultState(getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
        return true;
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (!w.isRemote()) {
            SkyChestTileEntity tile = getTileEntity(w, pos);
            if (tile != null) {
                ContainerOpener.openContainer(SkyChestContainer.TYPE, player, ContainerLocator.forTileEntity(tile));
            }
        }

        return ActionResultType.func_233537_a_(w.isRemote());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        final SkyChestTileEntity sk = this.getTileEntity(worldIn, pos);
        Direction up = sk != null ? sk.getUp() : Direction.UP;
        return SHAPES.get(up);
    }

    private static AxisAlignedBB computeAABB(Direction up) {
        final double offsetX = up.getXOffset() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetY = up.getYOffset() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetZ = up.getZOffset() == 0 ? AABB_OFFSET_SIDES : 0.0;

        // for x/z top and bottom is swapped
        final double minX = Math.max(0.0,
                offsetX + (up.getXOffset() < 0 ? AABB_OFFSET_BOTTOM : (up.getXOffset() * AABB_OFFSET_TOP)));
        final double minY = Math.max(0.0,
                offsetY + (up.getYOffset() < 0 ? AABB_OFFSET_TOP : (up.getYOffset() * AABB_OFFSET_BOTTOM)));
        final double minZ = Math.max(0.0,
                offsetZ + (up.getZOffset() < 0 ? AABB_OFFSET_BOTTOM : (up.getZOffset() * AABB_OFFSET_TOP)));

        final double maxX = Math.min(1.0,
                1.0 - offsetX - (up.getXOffset() < 0 ? AABB_OFFSET_TOP : (up.getXOffset() * AABB_OFFSET_BOTTOM)));
        final double maxY = Math.min(1.0,
                1.0 - offsetY - (up.getYOffset() < 0 ? AABB_OFFSET_BOTTOM : (up.getYOffset() * AABB_OFFSET_TOP)));
        final double maxZ = Math.min(1.0,
                1.0 - offsetZ - (up.getZOffset() < 0 ? AABB_OFFSET_TOP : (up.getZOffset() * AABB_OFFSET_BOTTOM)));

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        FluidState fluidState = context.getWorld().getFluidState(pos);
        BlockState blockState = this.getDefaultState()
                .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);

        return blockState;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.get(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getStillFluidState(false)
                : super.getFluidState(blockState);
    }

    @Override
    public BlockState updatePostPlacement(BlockState blockState, Direction facing, BlockState facingState, IWorld world,
            BlockPos currentPos, BlockPos facingPos) {
        if (blockState.get(WATERLOGGED)) {
            world.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER,
                    Fluids.WATER.getTickRate(world));
        }

        return super.updatePostPlacement(blockState, facing, facingState, world, currentPos, facingPos);
    }
}
