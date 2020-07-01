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
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.MolecularAssemblerContainer;
import appeng.tile.crafting.MolecularAssemblerBlockEntity;

public class MolecularAssemblerBlock extends AEBaseTileBlock<MolecularAssemblerBlockEntity> {

    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public MolecularAssemblerBlock(Settings props) {
        super(props);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, MolecularAssemblerBlockEntity te) {
        return currentState.with(POWERED, te.isPowered());
    }

    @Override
    public ActionResult onUse(BlockState state, World w, BlockPos pos, PlayerEntity p, Hand hand,
                              BlockHitResult hit) {
        final MolecularAssemblerBlockEntity tg = this.getBlockEntity(w, pos);
        if (tg != null && !p.isInSneakingPose()) {
            if (!tg.isClient()) {
                ContainerOpener.openContainer(MolecularAssemblerContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getSide()));
            }
            return ActionResult.SUCCESS;
        }

        return super.onUse(state, w, pos, p, hand, hit);
    }

}
