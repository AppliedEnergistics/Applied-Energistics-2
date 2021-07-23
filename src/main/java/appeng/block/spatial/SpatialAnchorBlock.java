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

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.tile.spatial.SpatialAnchorTileEntity;
import appeng.util.InteractionUtil;

/**
 * The block for our chunk loader
 */
public class SpatialAnchorBlock extends AEBaseTileBlock<SpatialAnchorTileEntity> {

    private static final BooleanProperty POWERED = net.minecraft.world.level.block.state.properties.BooleanProperty.create("powered");

    public SpatialAnchorBlock() {
        super(defaultProps(Material.METAL));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected net.minecraft.world.level.block.state.BlockState updateBlockStateFromTileEntity(BlockState currentState, SpatialAnchorTileEntity te) {
        return currentState.setValue(POWERED, te.isActive());
    }

    @Override
    public InteractionResult onActivated(final Level worldIn, final BlockPos pos, final Player p, final InteractionHand hand,
                                         final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        }

        final SpatialAnchorTileEntity tg = this.getTileEntity(worldIn, pos);
        if (tg != null) {
            if (!worldIn.isClientSide()) {
                ContainerOpener.openContainer(SpatialAnchorContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getDirection()));
            }
            return InteractionResult.sidedSuccess(worldIn.isClientSide());
        }
        return InteractionResult.PASS;
    }

}
