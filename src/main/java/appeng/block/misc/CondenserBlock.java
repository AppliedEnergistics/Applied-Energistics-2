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

package appeng.block.misc;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.CondenserContainer;
import appeng.tile.misc.CondenserTileEntity;
import appeng.util.InteractionUtil;
import net.minecraft.world.level.material.Material;

public class CondenserBlock extends AEBaseTileBlock<CondenserTileEntity> {

    public CondenserBlock() {
        super(defaultProps(Material.METAL));
    }

    @Override
    public InteractionResult onActivated(final Level w, final net.minecraft.core.BlockPos pos, final Player player, final InteractionHand hand,
                                         final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        if (!w.isClientSide()) {
            final CondenserTileEntity tc = this.getTileEntity(w, pos);
            if (tc != null && !InteractionUtil.isInAlternateUseMode(player)) {
                ContainerOpener.openContainer(CondenserContainer.TYPE, player,
                        ContainerLocator.forTileEntitySide(tc, hit.getDirection()));
            }
        }

        return InteractionResult.sidedSuccess(w.isClientSide());
    }
}
