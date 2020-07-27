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
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ChestContainer;
import appeng.core.localization.PlayerMessages;
import appeng.tile.storage.ChestBlockEntity;

public class ChestBlock extends AEBaseTileBlock<ChestBlockEntity> {

    private final static BooleanProperty LIGHTS_ON = BooleanProperty.of("lights_on");

    public ChestBlock() {
        super(defaultProps(Material.METAL));
        this.setDefaultState(this.getDefaultState().with(LIGHTS_ON, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(LIGHTS_ON);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, ChestBlockEntity te) {
        DriveSlotState slotState = DriveSlotState.EMPTY;

        if (te.getCellCount() >= 1) {
            slotState = DriveSlotState.fromCellStatus(te.getCellStatus(0));
        }
        // Power-state has to be checked separately
        if (!te.isPowered() && slotState != DriveSlotState.EMPTY) {
            slotState = DriveSlotState.OFFLINE;
        }

        return currentState.with(LIGHTS_ON, slotState != DriveSlotState.EMPTY && slotState != DriveSlotState.OFFLINE);
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        final ChestBlockEntity tg = this.getBlockEntity(w, pos);
        if (tg != null && !p.isInSneakingPose()) {
            if (w.isClient()) {
                return ActionResult.SUCCESS;
            }

            if (hit.getSide() == tg.getUp()) {
                if (!tg.openGui(p)) {
                    p.sendSystemMessage(PlayerMessages.ChestCannotReadStorageCell.get(), Util.NIL_UUID);
                }
            } else {
                ContainerOpener.openContainer(ChestContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getSide()));
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
