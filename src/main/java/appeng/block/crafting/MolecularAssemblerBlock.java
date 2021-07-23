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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
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
import appeng.container.implementations.MolecularAssemblerContainer;
import appeng.tile.crafting.MolecularAssemblerTileEntity;
import appeng.util.InteractionUtil;

public class MolecularAssemblerBlock extends AEBaseTileBlock<MolecularAssemblerTileEntity> {

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public MolecularAssemblerBlock(AbstractBlock.Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, MolecularAssemblerTileEntity te) {
        return currentState.setValue(POWERED, te.isPowered());
    }

    @Override
    public ActionResultType use(BlockState state, World w, BlockPos pos, PlayerEntity p, Hand hand,
            BlockRayTraceResult hit) {
        final MolecularAssemblerTileEntity tg = this.getTileEntity(w, pos);
        if (tg != null && !InteractionUtil.isInAlternateUseMode(p)) {
            if (!w.isClientSide()) {
                ContainerOpener.openContainer(MolecularAssemblerContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getDirection()));
            }
            return ActionResultType.sidedSuccess(w.isClientSide());
        }

        return super.use(state, w, pos, p, hand, hit);
    }

}
