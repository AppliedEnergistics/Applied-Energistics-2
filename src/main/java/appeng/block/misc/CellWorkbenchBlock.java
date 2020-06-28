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

import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.implementations.CellWorkbenchContainer;
import appeng.tile.misc.CellWorkbenchBlockEntity;
import appeng.util.Platform;

public class CellWorkbenchBlock extends AEBaseTileBlock<CellWorkbenchBlockEntity> {

    public CellWorkbenchBlock() {
        super(defaultProps(Material.METAL));
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (p.isInSneakingPose()) {
            return ActionResult.PASS;
        }

        final CellWorkbenchBlockEntity tg = this.getBlockEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                CellWorkbenchContainer.open(p, ContainerLocator.forTileEntity(tg));
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
}
