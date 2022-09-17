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


import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;


public class BlockChest extends AEBaseTileBlock {

    private final static PropertyEnum<DriveSlotState> SLOT_STATE = PropertyEnum.create("slot_state", DriveSlotState.class);

    public BlockChest() {
        super(Material.IRON);
        this.setDefaultState(this.getDefaultState().withProperty(SLOT_STATE, DriveSlotState.EMPTY));
    }

    @Override
    protected IProperty[] getAEStates() {
        return new IProperty[]{SLOT_STATE};
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        DriveSlotState slotState = DriveSlotState.EMPTY;

        TileChest te = this.getTileEntity(worldIn, pos);

        if (te != null) {
            if (te.getCellCount() >= 1) {
                slotState = DriveSlotState.fromCellStatus(te.getCellStatus(0));
            }
            // Power-state has to be checked separately
            if (!te.isPowered() && slotState != DriveSlotState.EMPTY) {
                slotState = DriveSlotState.OFFLINE;
            }
        }

        return super.getActualState(state, worldIn, pos)
                .withProperty(SLOT_STATE, slotState);
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        final TileChest tg = this.getTileEntity(w, pos);
        if (tg != null && !p.isSneaking()) {
            if (Platform.isClient()) {
                return true;
            }

            if (side != tg.getUp()) {
                Platform.openGUI(p, tg, AEPartLocation.fromFacing(side), GuiBridge.GUI_CHEST);
            } else {
                if (!tg.openGui(p)) {
                    p.sendMessage(PlayerMessages.ChestCannotReadStorageCell.get());
                }
            }

            return true;
        }

        return false;
    }
}
