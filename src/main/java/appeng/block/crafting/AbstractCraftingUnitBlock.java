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

package appeng.block.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.crafting.CraftingCPUContainer;
import appeng.tile.crafting.CraftingTileEntity;
import appeng.util.InteractionUtil;

public abstract class AbstractCraftingUnitBlock<T extends CraftingTileEntity> extends AEBaseTileBlock<T> {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public final CraftingUnitType type;

    public AbstractCraftingUnitBlock(BlockBehaviour.Properties props, final CraftingUnitType type) {
        super(props);
        this.type = type;
        this.registerDefaultState(defaultBlockState().setValue(FORMED, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
        builder.add(FORMED);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
            BlockPos currentPos, BlockPos facingPos) {
        BlockEntity te = worldIn.getBlockEntity(currentPos);
        if (te != null) {
            te.requestModelDataUpdate();
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, boolean isMoving) {
        final CraftingTileEntity cp = this.getTileEntity(worldIn, pos);
        if (cp != null) {
            cp.updateMultiBlock(fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final CraftingTileEntity cp = this.getTileEntity(w, pos);
        if (cp != null) {
            cp.breakCluster();
        }

        super.onRemove(state, w, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level w, BlockPos pos, Player p, InteractionHand hand,
            BlockHitResult hit) {
        final CraftingTileEntity tg = this.getTileEntity(w, pos);

        if (tg != null && !InteractionUtil.isInAlternateUseMode(p) && tg.isFormed() && tg.isActive()) {
            if (!w.isClientSide()) {
                ContainerOpener.openContainer(CraftingCPUContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getDirection()));
            }

            return InteractionResult.sidedSuccess(w.isClientSide());
        }

        return super.use(state, w, pos, p, hand, hit);
    }

    public enum CraftingUnitType {
        UNIT, ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, MONITOR
    }
}
