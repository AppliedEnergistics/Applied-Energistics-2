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

package appeng.block.grindstone;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.GrinderContainer;
import appeng.tile.grindstone.GrinderBlockEntity;

public class GrinderBlock extends AEBaseTileBlock<GrinderBlockEntity> {

    public GrinderBlock(Settings props) {
        super(props);
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        final GrinderBlockEntity tg = this.getBlockEntity(w, pos);
        if (tg != null && !p.isInSneakingPose()) {
            if (p instanceof ServerPlayerEntity) {
                ContainerOpener.openContainer(GrinderContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getSide()));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
