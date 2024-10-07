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

import java.util.List;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.util.InteractionUtil;

public abstract class AbstractCraftingUnitBlock<T extends CraftingBlockEntity> extends AEBaseEntityBlock<T> {
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level,
            BlockPos currentPos, BlockPos facingPos) {
        BlockEntity te = level.getBlockEntity(currentPos);
        if (te != null) {
            te.requestModelDataUpdate();
        }
        return super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn,
            BlockPos fromPos, boolean isMoving) {
        final CraftingBlockEntity cp = this.getBlockEntity(level, pos);
        if (cp != null) {
            cp.updateMultiBlock(fromPos);
        }
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
            InteractionResult result = this.disassemble(level, player, pos, state,
                    AEBlocks.CRAFTING_UNIT.block().defaultBlockState());
            if (result != InteractionResult.FAIL)
                return result;
        }

        if (level.getBlockEntity(pos) instanceof CraftingBlockEntity be && be.isFormed() && be.isActive()) {
            if (!level.isClientSide()) {
                MenuOpener.open(CraftingCPUMenu.TYPE, player, MenuLocators.forBlockEntity(be));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.upgrade(heldItem, state, level, pos, player))
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    public boolean upgrade(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player) {
        if (heldItem.isEmpty())
            return false;

        var upgradeRecipe = CraftingUnitTransformRecipe.getUpgradeRecipe(level, heldItem);
        if (upgradeRecipe == null)
            return false;
        if (level.isClientSide())
            return true;

        Block newBlock = BuiltInRegistries.BLOCK.get(upgradeRecipe.getBlock());
        if (newBlock == state.getBlock())
            return false;

        BlockState newState = newBlock.defaultBlockState();
        newState.setValue(POWERED, state.getValue(POWERED));
        newState.setValue(FORMED, state.getValue(FORMED));

        // Crafting Unit doesn't have a disassembly recipe, so we can ignore the drops.
        InteractionResult result = state.getBlock() == AEBlocks.CRAFTING_UNIT.block()
                ? this.transform(level, pos, newState) ? InteractionResult.SUCCESS : InteractionResult.FAIL
                : this.disassemble(level, player, pos, state, newState);

        if (result == InteractionResult.FAIL)
            return false;
        // Pass => Crafting Unit is busy!
        if (result == InteractionResult.PASS)
            return true;
        heldItem.consume(1, player);
        return true;
    }

    public InteractionResult disassemble(Level level, Player player, BlockPos pos, BlockState state,
            BlockState newState) {
        if (this.type == CraftingUnitType.UNIT || level.isClientSide())
            return InteractionResult.FAIL;

        var recipe = CraftingUnitTransformRecipe.getDisassemblyRecipe(level,
                AppEng.makeId("upgrade/" + Objects.requireNonNull(this.getRegistryName()).getPath()),
                this.getRegistryName());
        if (recipe == null)
            return InteractionResult.FAIL;

        final CraftingBlockEntity cp = this.getBlockEntity(level, pos);
        if (cp != null && cp.getCluster() != null && cp.getCluster().isBusy()) {
            player.displayClientMessage(Component.translatable("ae2.crafting_unit_busy").withColor(0xFF1F1F), true);
            return InteractionResult.PASS;
        }

        if (!this.transform(level, pos, newState))
            return InteractionResult.FAIL;

        List<ItemStack> drop;
        if (recipe.useLootTable()) {
            LootParams params = new LootParams.Builder((ServerLevel) level)
                    .withParameter(LootContextParams.BLOCK_STATE, state)
                    .withParameter(LootContextParams.TOOL, player.getUseItem())
                    .create(LootContextParamSets.EMPTY);
            drop = recipe.getDisassemblyLoot(level, params);
        } else {
            drop = recipe.getDisassemblyItems();
        }
        drop.forEach(item -> player.getInventory().placeItemBackInInventory(item));

        return InteractionResult.SUCCESS;
    }

    private boolean transform(Level level, BlockPos pos, BlockState state) {
        if (!level.removeBlock(pos, false) || !level.setBlock(pos, state, 3))
            return false;

        level.playSound(
                null,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                SoundEvents.AMETHYST_BLOCK_HIT,
                SoundSource.BLOCKS,
                1f,
                1f);
        return true;
    }
}
