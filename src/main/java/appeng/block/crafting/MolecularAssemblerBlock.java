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

package appeng.block.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.MolecularAssemblerMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;

public class MolecularAssemblerBlock extends AEBaseEntityBlock<MolecularAssemblerBlockEntity> {

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public MolecularAssemblerBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, MolecularAssemblerBlockEntity be) {
        return currentState.setValue(POWERED, be.isPowered());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player p, InteractionHand hand,
            BlockHitResult hit) {
        final MolecularAssemblerBlockEntity tg = this.getBlockEntity(level, pos);
        if (tg != null && !InteractionUtil.isInAlternateUseMode(p)) {
            if (!level.isClientSide()) {
                hit.getDirection();
                MenuOpener.open(MolecularAssemblerMenu.TYPE, p,
                        MenuLocators.forBlockEntity(tg));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.use(state, level, pos, p, hand, hit);
    }

}
