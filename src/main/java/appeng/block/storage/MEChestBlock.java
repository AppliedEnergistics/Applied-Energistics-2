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

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.storage.cells.CellState;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.MEChestBlockEntity;
import appeng.core.localization.PlayerMessages;

public class MEChestBlock extends AEBaseEntityBlock<MEChestBlockEntity> {

    public final static BooleanProperty LIGHTS_ON = BooleanProperty.create("lights_on");

    public MEChestBlock() {
        super(metalProps());
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
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, MEChestBlockEntity be) {
        var cellState = CellState.ABSENT;

        if (be.getCellCount() >= 1) {
            cellState = be.getCellStatus(0);
        }

        return currentState.setValue(LIGHTS_ON, be.isPowered() && cellState != CellState.ABSENT);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MEChestBlockEntity be) {
            if (!level.isClientSide()) {
                if (hitResult.getDirection() == be.getTop()) {
                    if (!be.openGui(player)) {
                        player.displayClientMessage(PlayerMessages.ChestCannotReadStorageCell.text(), true);
                    }
                } else {
                    be.openCellInventoryMenu(player);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
