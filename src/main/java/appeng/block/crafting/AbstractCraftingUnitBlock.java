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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.crafting.CraftingCPUContainer;
import appeng.tile.crafting.CraftingTileEntity;
import appeng.util.InteractionUtil;

public abstract class AbstractCraftingUnitBlock<T extends CraftingTileEntity> extends AEBaseTileBlock<T> {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public final CraftingUnitType type;

    public AbstractCraftingUnitBlock(AbstractBlock.Properties props, final CraftingUnitType type) {
        super(props);
        this.type = type;
        this.setDefaultState(getDefaultState().with(FORMED, false).with(POWERED, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(POWERED);
        builder.add(FORMED);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
            BlockPos currentPos, BlockPos facingPos) {
        TileEntity te = worldIn.getTileEntity(currentPos);
        if (te != null) {
            te.requestModelDataUpdate();
        }
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(final BlockState state, final World worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, boolean isMoving) {
        final CraftingTileEntity cp = this.getTileEntity(worldIn, pos);
        if (cp != null) {
            cp.updateMultiBlock(fromPos);
        }
    }

    @Override
    public void onReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final CraftingTileEntity cp = this.getTileEntity(w, pos);
        if (cp != null) {
            cp.breakCluster();
        }

        super.onReplaced(state, w, pos, newState, isMoving);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World w, BlockPos pos, PlayerEntity p, Hand hand,
            BlockRayTraceResult hit) {
        final CraftingTileEntity tg = this.getTileEntity(w, pos);

        if (tg != null && !InteractionUtil.isInAlternateUseMode(p) && tg.isFormed() && tg.isActive()) {
            if (!w.isRemote()) {
                ContainerOpener.openContainer(CraftingCPUContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace()));
            }

            return ActionResultType.func_233537_a_(w.isRemote());
        }

        return super.onBlockActivated(state, w, pos, p, hand, hit);
    }

    public enum CraftingUnitType {
        UNIT, ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, MONITOR
    }
}
