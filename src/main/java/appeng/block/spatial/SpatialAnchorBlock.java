/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.tile.spatial.SpatialAnchorTileEntity;
import appeng.util.Platform;

/**
 * The block for our chunk loader
 */
public class SpatialAnchorBlock extends AEBaseTileBlock<SpatialAnchorTileEntity> {

    private static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public SpatialAnchorBlock() {
        super(defaultProps(Material.IRON));
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, SpatialAnchorTileEntity te) {
        return currentState.with(POWERED, te.isActive());
    }

    @Override
    public ActionResultType onActivated(final World worldIn, final BlockPos pos, final PlayerEntity p, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        if (p.isSneaking()) {
            return ActionResultType.PASS;
        }

        final SpatialAnchorTileEntity tg = this.getTileEntity(worldIn, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(SpatialAnchorContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace()));
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

}
