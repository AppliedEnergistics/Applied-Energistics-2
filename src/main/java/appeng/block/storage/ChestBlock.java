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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ChestContainer;
import appeng.core.localization.PlayerMessages;
import appeng.tile.storage.ChestTileEntity;
import appeng.util.InteractionUtil;

public class ChestBlock extends AEBaseTileBlock<ChestTileEntity> {

    private final static BooleanProperty LIGHTS_ON = BooleanProperty.create("lights_on");

    public ChestBlock() {
        super(defaultProps(Material.METAL));
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHTS_ON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHTS_ON);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, ChestTileEntity te) {
        DriveSlotState slotState = DriveSlotState.EMPTY;

        if (te.getCellCount() >= 1) {
            slotState = DriveSlotState.fromCellStatus(te.getCellStatus(0));
        }
        // Power-state has to be checked separately
        if (!te.isPowered() && slotState != DriveSlotState.EMPTY) {
            slotState = DriveSlotState.OFFLINE;
        }

        return currentState.setValue(LIGHTS_ON, slotState != DriveSlotState.EMPTY && slotState != DriveSlotState.OFFLINE);
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        final ChestTileEntity tg = this.getTileEntity(w, pos);
        if (tg != null && !InteractionUtil.isInAlternateUseMode(p)) {
            if (!w.isClientSide()) {
                if (hit.getDirection() == tg.getUp()) {
                    if (!tg.openGui(p)) {
                        p.sendMessage(PlayerMessages.ChestCannotReadStorageCell.get(), Util.NIL_UUID);
                    }
                } else {
                    ContainerOpener.openContainer(ChestContainer.TYPE, p,
                            ContainerLocator.forTileEntitySide(tg, hit.getDirection()));
                }
            }

            return ActionResultType.sidedSuccess(w.isClientSide());
        }

        return ActionResultType.PASS;
    }
}
