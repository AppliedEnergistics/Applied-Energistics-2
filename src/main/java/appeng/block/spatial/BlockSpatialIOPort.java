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

package appeng.block.spatial;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import appeng.container.implementations.ContainerSpatialIOPort;
import appeng.fluids.container.ContainerFluidLevelEmitter;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.util.Platform;

public class BlockSpatialIOPort extends AEBaseTileBlock<TileSpatialIOPort> {

    public BlockSpatialIOPort() {
        super(defaultProps(Material.IRON));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final TileSpatialIOPort te = this.getTileEntity(world, pos);
        if (te != null) {
            te.updateRedstoneState();
        }
    }

    @Override
    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (p.isCrouching()) {
            return ActionResultType.PASS;
        }

        final TileSpatialIOPort tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(ContainerSpatialIOPort.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace()));
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }
}
