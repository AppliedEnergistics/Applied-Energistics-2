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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.SkyChestContainer;
import appeng.tile.storage.SkyChestTileEntity;

import net.minecraft.world.phys.shapes.CollisionContext;

public class SkyChestBlock extends AEBaseTileBlock<SkyChestTileEntity> implements SimpleWaterloggedBlock {

    private static final double AABB_OFFSET_BOTTOM = 0.00;
    private static final double AABB_OFFSET_SIDES = 0.06;
    private static final double AABB_OFFSET_TOP = 0.0625;

    // Precomputed bounding boxes of the chest, sorted into the map by the UP
    // direction
    private static final Map<net.minecraft.core.Direction, VoxelShape> SHAPES = new EnumMap<>(net.minecraft.core.Direction.class);

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    static {
        for (net.minecraft.core.Direction up : net.minecraft.core.Direction.values()) {
            AABB aabb = computeAABB(up);
            SHAPES.put(up, Shapes.create(aabb));
        }
    }

    public enum SkyChestType {
        STONE, BLOCK
    }

    public final SkyChestType type;

    public SkyChestBlock(final SkyChestType type, net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
        super(props);
        this.type = type;
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(net.minecraft.world.level.block.state.BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean propagatesSkylightDown(net.minecraft.world.level.block.state.BlockState state, BlockGetter reader, net.minecraft.core.BlockPos pos) {
        return true;
    }

    @Override
    public InteractionResult onActivated(final Level w, final net.minecraft.core.BlockPos pos, final Player player, final InteractionHand hand,
                                         final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (!w.isClientSide()) {
            SkyChestTileEntity tile = getTileEntity(w, pos);
            if (tile != null) {
                ContainerOpener.openContainer(SkyChestContainer.TYPE, player, ContainerLocator.forTileEntity(tile));
            }
        }

        return InteractionResult.sidedSuccess(w.isClientSide());
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(net.minecraft.world.level.block.state.BlockState state, BlockGetter worldIn, net.minecraft.core.BlockPos pos, CollisionContext context) {
        final SkyChestTileEntity sk = this.getTileEntity(worldIn, pos);
        Direction up = sk != null ? sk.getUp() : net.minecraft.core.Direction.UP;
        return SHAPES.get(up);
    }

    private static AABB computeAABB(net.minecraft.core.Direction up) {
        final double offsetX = up.getStepX() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetY = up.getStepY() == 0 ? AABB_OFFSET_SIDES : 0.0;
        final double offsetZ = up.getStepZ() == 0 ? AABB_OFFSET_SIDES : 0.0;

        // for x/z top and bottom is swapped
        final double minX = Math.max(0.0,
                offsetX + (up.getStepX() < 0 ? AABB_OFFSET_BOTTOM : up.getStepX() * AABB_OFFSET_TOP));
        final double minY = Math.max(0.0,
                offsetY + (up.getStepY() < 0 ? AABB_OFFSET_TOP : up.getStepY() * AABB_OFFSET_BOTTOM));
        final double minZ = Math.max(0.0,
                offsetZ + (up.getStepZ() < 0 ? AABB_OFFSET_BOTTOM : up.getStepZ() * AABB_OFFSET_TOP));

        final double maxX = Math.min(1.0,
                1.0 - offsetX - (up.getStepX() < 0 ? AABB_OFFSET_TOP : up.getStepX() * AABB_OFFSET_BOTTOM));
        final double maxY = Math.min(1.0,
                1.0 - offsetY - (up.getStepY() < 0 ? AABB_OFFSET_BOTTOM : up.getStepY() * AABB_OFFSET_TOP));
        final double maxZ = Math.min(1.0,
                1.0 - offsetZ - (up.getStepZ() < 0 ? AABB_OFFSET_TOP : up.getStepZ() * AABB_OFFSET_BOTTOM));

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        net.minecraft.core.BlockPos pos = context.getClickedPos();
        net.minecraft.world.level.material.FluidState fluidState = context.getLevel().getFluidState(pos);
        net.minecraft.world.level.block.state.BlockState blockState = this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return blockState;
    }

    @Override
    public FluidState getFluidState(net.minecraft.world.level.block.state.BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(net.minecraft.world.level.block.state.BlockState blockState, net.minecraft.core.Direction facing, net.minecraft.world.level.block.state.BlockState facingState, LevelAccessor world,
                                  BlockPos currentPos, BlockPos facingPos) {
        if (blockState.getValue(WATERLOGGED)) {
            world.getLiquidTicks().scheduleTick(currentPos, net.minecraft.world.level.material.Fluids.WATER,
                    net.minecraft.world.level.material.Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(blockState, facing, facingState, world, currentPos, facingPos);
    }
}
