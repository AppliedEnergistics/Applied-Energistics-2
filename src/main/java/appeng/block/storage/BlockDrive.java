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
import appeng.client.UnlistedProperty;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;


public class BlockDrive extends AEBaseTileBlock {

    public static final UnlistedProperty<DriveSlotsState> SLOTS_STATE = new UnlistedProperty<>("drive_slots_state", DriveSlotsState.class);

    public BlockDrive() {
        super(Material.IRON);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, this.getAEStates(), new IUnlistedProperty[]{
                SLOTS_STATE,
                FORWARD,
                UP
        });
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileDrive te = this.getTileEntity(world, pos);
        IExtendedBlockState extState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        return extState.withProperty(SLOTS_STATE, te == null ? DriveSlotsState.createEmpty(10) : DriveSlotsState.fromChestOrDrive(te));
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (p.isSneaking()) {
            return false;
        }

        final TileDrive tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                Platform.openGUI(p, tg, AEPartLocation.fromFacing(side), GuiBridge.GUI_DRIVE);
            }
            return true;
        }
        return false;
    }
}
