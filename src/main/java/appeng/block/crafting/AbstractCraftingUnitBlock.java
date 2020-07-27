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
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.CraftingCPUContainer;
import appeng.tile.crafting.CraftingBlockEntity;

public abstract class AbstractCraftingUnitBlock<T extends CraftingBlockEntity> extends AEBaseTileBlock<T> {
    public static final BooleanProperty FORMED = BooleanProperty.of("formed");
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public final CraftingUnitType type;

    public AbstractCraftingUnitBlock(Settings props, final CraftingUnitType type) {
        super(props);
        this.type = type;
        this.setDefaultState(getDefaultState().with(FORMED, false).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
        builder.add(FORMED);
    }

    @Override
    public void neighborUpdate(final BlockState state, final World worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos, boolean isMoving) {
        final CraftingBlockEntity cp = this.getBlockEntity(worldIn, pos);
        if (cp != null) {
            cp.updateMultiBlock(fromPos);
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final CraftingBlockEntity cp = this.getBlockEntity(w, pos);
        if (cp != null) {
            cp.breakCluster();
        }

        super.onStateReplaced(state, w, pos, newState, isMoving);
    }

    @Override
    public ActionResult onUse(BlockState state, World w, BlockPos pos, PlayerEntity p, Hand hand, BlockHitResult hit) {
        final CraftingBlockEntity tg = this.getBlockEntity(w, pos);

        if (tg != null && !p.isInSneakingPose() && tg.isFormed() && tg.isActive()) {
            if (!w.isClient()) {
                ContainerOpener.openContainer(CraftingCPUContainer.TYPE, p,
                        ContainerLocator.forTileEntitySide(tg, hit.getSide()));
            }

            return ActionResult.SUCCESS;
        }

        return super.onUse(state, w, pos, p, hand, hit);
    }

    public enum CraftingUnitType {
        UNIT, ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, MONITOR
    }
}
