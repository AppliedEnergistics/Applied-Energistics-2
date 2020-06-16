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

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerCondenser;
import appeng.container.implementations.ContainerDrive;
import appeng.tile.misc.TileCondenser;
import appeng.util.Platform;

public class BlockCondenser extends AEBaseTileBlock<TileCondenser> {

    public BlockCondenser() {
        super(defaultProps(Material.IRON));
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (player.isCrouching()) {
            return ActionResultType.PASS;
        }

        if (Platform.isServer()) {
            final TileCondenser tc = this.getTileEntity(w, pos);
            if (tc != null && !player.isCrouching()) {
                ContainerOpener.openContainer(ContainerCondenser.TYPE, player,
                        ContainerLocator.forTileEntitySide(tc, hit.getFace()));
                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.SUCCESS;
    }
}
