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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.PlayerMessages;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.util.InteractionUtil;

public abstract class AbstractCraftingUnitBlock<T extends CraftingBlockEntity> extends AEBaseEntityBlock<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCraftingUnitBlock.class);

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public final ICraftingUnitType type;

    public AbstractCraftingUnitBlock(Properties props, ICraftingUnitType type) {
        super(props);
        this.type = type;
        this.registerDefaultState(defaultBlockState().setValue(FORMED, false).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
        builder.add(FORMED);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te != null) {
            te.requestModelDataUpdate();
        }
        var cp = this.getBlockEntity(level, pos);
        if (cp != null) {
            cp.updateMultiBlock(neighborPos);
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final CraftingBlockEntity cp = this.getBlockEntity(level, pos);
        if (cp != null) {
            cp.breakCluster();
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            var result = this.removeUpgrade(level, player, pos, AEBlocks.CRAFTING_UNIT.block().defaultBlockState());
            if (result != InteractionResult.FAIL)
                return result;
        }

        if (level.getBlockEntity(pos) instanceof CraftingBlockEntity be && be.isFormed() && be.isActive()) {
            if (!level.isClientSide()) {
                MenuOpener.open(CraftingCPUMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }

            return InteractionResult.SUCCESS;
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel serverLevel) {
            if (this.upgrade(heldItem, state, serverLevel, pos, player, hit)) {
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    public boolean upgrade(ItemStack heldItem, BlockState state, ServerLevel level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (heldItem.isEmpty()) {
            return false;
        }

        var upgradedBlock = CraftingUnitTransformRecipe.getUpgradedBlock(level, heldItem);
        if (upgradedBlock == null) {
            return false;
        }

        if (!(upgradedBlock instanceof AbstractCraftingUnitBlock<?>)) {
            LOG.warn("Upgraded block for crafting unit upgrade with {} is not a crafting block: {}",
                    heldItem, upgradedBlock);
            return false;
        }

        if (upgradedBlock == state.getBlock()) {
            return false;
        }

        // If Upgrading is possible - but disassembly isn't, this will still make the hand animation play.
        if (level.isClientSide()) {
            return true;
        }

        var newState = upgradedBlock.defaultBlockState();

        // Makes sure Crafting Monitors are looking at the player.
        newState = newState.trySetValue(BlockStateProperties.FACING, hit.getDirection());

        // Crafting Unit doesn't have a disassembly recipe, so we can ignore the drops.
        InteractionResult result = state.getBlock() == AEBlocks.CRAFTING_UNIT.block()
                ? this.transform(level, pos, newState) ? InteractionResult.SUCCESS : InteractionResult.FAIL
                : this.removeUpgrade(level, player, pos, newState);

        if (result == InteractionResult.FAIL)
            return false;
        // Pass => Crafting Unit is busy!
        if (result == InteractionResult.PASS)
            return true;
        heldItem.consume(1, player);
        return true;
    }

    public InteractionResult removeUpgrade(Level level, Player player, BlockPos pos, BlockState newState) {
        if (this.type == CraftingUnitType.UNIT || !(level instanceof ServerLevel serverLevel))
            return InteractionResult.FAIL;

        var removedUpgrade = CraftingUnitTransformRecipe.getRemovedUpgrade(serverLevel, this);
        if (removedUpgrade.isEmpty()) {
            return InteractionResult.FAIL;
        }

        var cb = this.getBlockEntity(level, pos);
        if (cb != null && cb.getCluster() != null && cb.getCluster().isBusy()) {
            player.displayClientMessage(PlayerMessages.CraftingCpuBusy.text().withColor(0xFF1F1F), true);
            return InteractionResult.PASS;
        }

        if (!this.transform(level, pos, newState)) {
            return InteractionResult.FAIL;
        }

        player.getInventory().placeItemBackInInventory(removedUpgrade);

        return InteractionResult.SUCCESS;
    }

    private boolean transform(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide() || !level.removeBlock(pos, false) || !level.setBlock(pos, state, UPDATE_ALL)) {
            return false;
        }

        level.playSound(
                null,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.BLOCKS,
                0.5f,
                1f);
        return true;
    }
}
