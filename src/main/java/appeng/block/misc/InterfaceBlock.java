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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.InterfaceContainer;
import appeng.tile.misc.InterfaceTileEntity;
import appeng.util.InteractionUtil;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.Material;

public class InterfaceBlock extends AEBaseTileBlock<InterfaceTileEntity> {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create("omnidirectional");

    public InterfaceBlock() {
        super(defaultProps(Material.METAL));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, net.minecraft.world.level.block.state.BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OMNIDIRECTIONAL);
    }

    @Override
    protected net.minecraft.world.level.block.state.BlockState updateBlockStateFromTileEntity(BlockState currentState, InterfaceTileEntity te) {
        return currentState.setValue(OMNIDIRECTIONAL, te.isOmniDirectional());
    }

    @Override
    public InteractionResult onActivated(final Level w, final BlockPos pos, final Player p, final InteractionHand hand,
                                         final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        }

        final InterfaceTileEntity tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (!w.isClientSide()) {
                ContainerOpener.openContainer(InterfaceContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getDirection()));
            }
            return InteractionResult.sidedSuccess(w.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(final IOrientable rotatable, final Direction axis) {
        if (rotatable instanceof InterfaceTileEntity) {
            ((InterfaceTileEntity) rotatable).setSide(axis);
        }
    }
}
