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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.storage.cells.CellState;
import appeng.block.AEBaseEntityBlock;
import appeng.block.orientation.IOrientationStrategy;
import appeng.block.orientation.OrientationStrategies;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.core.localization.PlayerMessages;
import appeng.util.InteractionUtil;

public class ChestBlock extends AEBaseEntityBlock<ChestBlockEntity> {

    public final static BooleanProperty LIGHTS_ON = BooleanProperty.create("lights_on");

    public ChestBlock() {
        super(defaultProps(Material.METAL));
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHTS_ON, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHTS_ON);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, ChestBlockEntity be) {
        var cellState = CellState.ABSENT;

        if (be.getCellCount() >= 1) {
            cellState = be.getCellStatus(0);
        }

        return currentState.setValue(LIGHTS_ON, be.isPowered() && cellState != CellState.ABSENT);
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player p,
            InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        var be = this.getBlockEntity(level, pos);
        if (be != null && !InteractionUtil.isInAlternateUseMode(p)) {
            if (!level.isClientSide()) {
                if (hit.getDirection() == be.getUp()) {
                    if (!be.openGui(p)) {
                        p.sendSystemMessage(PlayerMessages.ChestCannotReadStorageCell.text());
                    }
                } else {
                    be.openCellInventoryMenu(p);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
