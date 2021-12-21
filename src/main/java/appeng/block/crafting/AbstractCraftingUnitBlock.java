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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.util.InteractionUtil;

public abstract class AbstractCraftingUnitBlock<T extends CraftingBlockEntity> extends AEBaseEntityBlock<T> {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public final CraftingUnitType type;

    public AbstractCraftingUnitBlock(BlockBehaviour.Properties props, CraftingUnitType type) {
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn,
            BlockPos fromPos, boolean isMoving) {
        final CraftingBlockEntity cp = this.getBlockEntity(level, pos);
        if (cp != null) {
            cp.updateMultiBlock(fromPos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final CraftingBlockEntity cp = this.getBlockEntity(level, pos);
        if (cp != null) {
            cp.breakCluster();
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player p, InteractionHand hand,
            BlockHitResult hit) {
        final CraftingBlockEntity tg = this.getBlockEntity(level, pos);

        if (tg != null && !InteractionUtil.isInAlternateUseMode(p) && tg.isFormed() && tg.isActive()) {
            if (!level.isClientSide()) {
                hit.getDirection();
                MenuOpener.open(CraftingCPUMenu.TYPE, p,
                        MenuLocators.forBlockEntity(tg));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.use(state, level, pos, p, hand, hit);
    }

    public enum CraftingUnitType {
        UNIT, ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, MONITOR
    }
}
