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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.misc.ItemInterfaceBlockEntity;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.ItemInterfaceMenu;
import appeng.util.InteractionUtil;

public class ItemInterfaceBlock extends AEBaseEntityBlock<ItemInterfaceBlockEntity> {

    private static final BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create("omnidirectional");

    public ItemInterfaceBlock() {
        super(defaultProps(Material.METAL));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OMNIDIRECTIONAL);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, ItemInterfaceBlockEntity be) {
        return currentState.setValue(OMNIDIRECTIONAL, be.isOmniDirectional());
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        }

        final ItemInterfaceBlockEntity tg = this.getBlockEntity(level, pos);
        if (tg != null) {
            if (!level.isClientSide()) {
                MenuOpener.open(ItemInterfaceMenu.TYPE, p,
                        MenuLocator.forBlockEntitySide(tg, hit.getDirection()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(final IOrientable rotatable, final Direction axis) {
        if (rotatable instanceof ItemInterfaceBlockEntity) {
            ((ItemInterfaceBlockEntity) rotatable).setSide(axis);
        }
    }
}
