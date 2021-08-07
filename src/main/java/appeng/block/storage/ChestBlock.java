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

import net.minecraft.Util;
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

import appeng.block.AEBaseEntityBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ChestContainer;
import appeng.core.localization.PlayerMessages;
import appeng.blockentity.storage.ChestBlockEntity;
import appeng.util.InteractionUtil;

public class ChestBlock extends AEBaseEntityBlock<ChestBlockEntity> {

    private final static BooleanProperty LIGHTS_ON = BooleanProperty.create("lights_on");

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
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, ChestBlockEntity be) {
        DriveSlotState slotState = DriveSlotState.EMPTY;

        if (be.getCellCount() >= 1) {
            slotState = DriveSlotState.fromCellStatus(be.getCellStatus(0));
        }
        // Power-state has to be checked separately
        if (!be.isPowered() && slotState != DriveSlotState.EMPTY) {
            slotState = DriveSlotState.OFFLINE;
        }

        return currentState.setValue(LIGHTS_ON,
                slotState != DriveSlotState.EMPTY && slotState != DriveSlotState.OFFLINE);
    }

    @Override
    public InteractionResult onActivated(final Level w, final BlockPos pos, final Player p, final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        final ChestBlockEntity tg = this.getBlockEntity(w, pos);
        if (tg != null && !InteractionUtil.isInAlternateUseMode(p)) {
            if (!w.isClientSide()) {
                if (hit.getDirection() == tg.getUp()) {
                    if (!tg.openGui(p)) {
                        p.sendMessage(PlayerMessages.ChestCannotReadStorageCell.get(), Util.NIL_UUID);
                    }
                } else {
                    ContainerOpener.openContainer(ChestContainer.TYPE, p,
                            ContainerLocator.forBlockEntitySide(tg, hit.getDirection()));
                }
            }

            return InteractionResult.sidedSuccess(w.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
