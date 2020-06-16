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
import appeng.container.implementations.ContainerInscriber;
import appeng.container.implementations.ContainerMAC;
import appeng.tile.crafting.TileMolecularAssembler;

public class BlockMolecularAssembler extends AEBaseTileBlock<TileMolecularAssembler> {

    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public BlockMolecularAssembler(Block.Properties props) {
        super(props);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, TileMolecularAssembler te) {
        return currentState.with(POWERED, te.isPowered());
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World w, BlockPos pos, PlayerEntity p, Hand hand,
            BlockRayTraceResult hit) {
        final TileMolecularAssembler tg = this.getTileEntity(w, pos);
        if (tg != null && !p.isCrouching()) {
            if (!tg.isRemote()) {
                ContainerOpener.openContainer(ContainerMAC.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace()));
            }
            return ActionResultType.SUCCESS;
        }

        return super.onBlockActivated(state, w, pos, p, hand, hit);
    }

}
