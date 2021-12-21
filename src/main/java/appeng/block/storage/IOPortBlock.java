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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.storage.IOPortBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.IOPortMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;

public class IOPortBlock extends AEBaseEntityBlock<IOPortBlockEntity> {

    public IOPortBlock() {
        super(defaultProps(Material.METAL));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final IOPortBlockEntity te = this.getBlockEntity(level, pos);
        if (te != null) {
            te.updateRedstoneState();
        }
    }

    @Override
    public InteractionResult onActivated(Level level, BlockPos pos, Player p,
            InteractionHand hand,
            @Nullable ItemStack heldItem, BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        }

        final IOPortBlockEntity tg = this.getBlockEntity(level, pos);
        if (tg != null) {
            if (!level.isClientSide()) {
                hit.getDirection();
                MenuOpener.open(IOPortMenu.TYPE, p,
                        MenuLocators.forBlockEntity(tg));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }
}
