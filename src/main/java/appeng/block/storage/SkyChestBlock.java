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
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.definitions.AEBlockEntities;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.SkyChestMenu;

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

    public SkyChestBlock(final SkyChestType type, BlockBehaviour.Properties props) {
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player player,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (!level.isClientSide()) {
            SkyChestBlockEntity blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null) {
                blockEntity.unpackLootTable(player);
                MenuOpener.open(SkyChestMenu.TYPE, player,
                        MenuLocator.forBlockEntity(blockEntity));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        level.getBlockEntity(pos, AEBlockEntities.SKY_CHEST).ifPresent(SkyChestBlockEntity::recheckOpen);

    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        final SkyChestBlockEntity sk = this.getBlockEntity(level, pos);
        Direction up = sk != null ? sk.getUp() : Direction.UP;
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
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        BlockState blockState = this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return blockState;
    }

    @Override
    public FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED).booleanValue()
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        if (blockState.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(blockState, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos blockPos, BlockState state, Player player) {
        super.playerWillDestroy(level, blockPos, state, player);
        // Spawn loot for player
        level.getBlockEntity(blockPos, AEBlockEntities.SKY_CHEST).ifPresent(chest -> chest.unpackLootTable(player));
    }
}
