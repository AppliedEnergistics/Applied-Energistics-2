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

import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.SkyChestMenu;
import appeng.menu.locator.MenuLocators;

public class SkyChestBlock extends AEBaseEntityBlock<SkyChestBlockEntity> implements SimpleWaterloggedBlock {

    private static final double AABB_OFFSET_BOTTOM = 0.00;
    private static final double AABB_OFFSET_SIDES = 0.06;
    private static final double AABB_OFFSET_TOP = 0.0625;

    // Precomputed bounding boxes of the chest, sorted into the map by the UP
    // direction
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    static {
        for (Direction up : Direction.values()) {
            AABB aabb = computeAABB(up);
            SHAPES.put(up, Shapes.create(aabb));
        }
    }

    public enum SkyChestType {
        STONE, BLOCK
    }

    public final SkyChestType type;

    public SkyChestBlock(SkyChestType type, Properties props) {
        super(props);
        this.type = type;
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SkyChestBlockEntity be) {
            if (!level.isClientSide()) {
                MenuOpener.open(SkyChestMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        var chest = AEBlockEntities.SKY_CHEST.getBlockEntity(level, pos);
        if (chest != null) {
            chest.recheckOpen();
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        final SkyChestBlockEntity sk = this.getBlockEntity(level, pos);
        Direction up = sk != null ? sk.getTop() : Direction.UP;
        return SHAPES.get(up);
    }

    private static AABB computeAABB(Direction up) {
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
        var fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }
}
