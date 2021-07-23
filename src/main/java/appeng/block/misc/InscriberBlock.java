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

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.InscriberContainer;
import appeng.tile.misc.InscriberTileEntity;
import appeng.util.InteractionUtil;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class InscriberBlock extends AEBaseTileBlock<InscriberTileEntity> {

    public InscriberBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 2; // FIXME validate this. a) possibly not required because of getShape b) value
        // range. was 2 in 1.10
    }

    @Override
    public InteractionResult onActivated(final Level w, final BlockPos pos, final Player p, final InteractionHand hand,
                                         final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (!InteractionUtil.isInAlternateUseMode(p)) {
            final InscriberTileEntity tg = this.getTileEntity(w, pos);
            if (tg != null) {
                if (!w.isClientSide()) {
                    ContainerOpener.openContainer(InscriberContainer.TYPE, p,
                            ContainerLocator.forTileEntitySide(tg, hit.getDirection()));
                }
                return InteractionResult.sidedSuccess(w.isClientSide());
            }
        }
        return InteractionResult.PASS;

    }

}
