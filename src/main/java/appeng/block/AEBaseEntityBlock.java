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

package appeng.block;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.IOrientable;
import appeng.block.networking.CableBusBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

/**
 * Base class for blocks that have a {@link BlockEntity}.
 * 
 * @param <T> The type of {@link BlockEntity} or this block.
 */
public abstract class AEBaseEntityBlock<T extends AEBaseBlockEntity> extends AEBaseBlock implements EntityBlock {

    @Nonnull
    private Class<T> blockEntityClass;
    @Nonnull
    private BlockEntityType<T> blockEntityType;

    @Nullable
    private BlockEntityTicker<T> serverTicker;

    @Nullable
    private BlockEntityTicker<T> clientTicker;

    public AEBaseEntityBlock(final BlockBehaviour.Properties props) {
        super(props);
    }

    // TODO : Was this change needed?
    public void setBlockEntity(final Class<T> blockEntityClass,
            BlockEntityType<T> blockEntityType,
            BlockEntityTicker<T> clientTicker,
            BlockEntityTicker<T> serverTicker) {
        this.blockEntityClass = blockEntityClass;
        this.blockEntityType = blockEntityType;
        this.serverTicker = serverTicker;
        this.clientTicker = clientTicker;
        this.setInventory(AEBaseInvBlockEntity.class.isAssignableFrom(blockEntityClass));
    }

    @Nullable
    public T getBlockEntity(final BlockGetter level, final int x, final int y, final int z) {
        return this.getBlockEntity(level, new BlockPos(x, y, z));
    }

    @Nullable
    public T getBlockEntity(final BlockGetter level, final BlockPos pos) {
        final BlockEntity te = level.getBlockEntity(pos);
        // FIXME: This gets called as part of building the block state cache
        if (this.blockEntityClass != null && this.blockEntityClass.isInstance(te)) {
            return this.blockEntityClass.cast(te);
        }

        return null;
    }

    @Nonnull
    public BlockEntityType<T> getBlockEntityType() {
        return blockEntityType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityType.create(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState,
            BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) (level.isClientSide() ? clientTicker : serverTicker);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final AEBaseBlockEntity te = this.getBlockEntity(level, pos);
        if (te != null) {
            final ArrayList<ItemStack> drops = new ArrayList<>();
            if (te.dropItems()) {
                te.getDrops(level, pos, drops);
            } else {
                te.getNoDrops(level, pos, drops);
            }

            // Cry ;_; ...
            Platform.spawnDrops(level, pos, drops);
        }

        // super will remove the TE, as it is not an instance of BlockContainer
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, final Level level, final BlockPos pos) {
        final BlockEntity te = this.getBlockEntity(level, pos);
        if (te instanceof AEBaseInvBlockEntity invBlockEntity) {
            if (invBlockEntity.getInternalInventory().size() > 0) {
                return invBlockEntity.getInternalInventory().getRedstoneSignal();
            }
        }
        return 0;
    }

    @Override
    public boolean triggerEvent(final BlockState state, final Level level, final BlockPos pos, final int eventID,
            final int eventParam) {
        super.triggerEvent(state, level, pos, eventID, eventParam);
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null ? blockEntity.triggerEvent(eventID, eventParam) : false;
    }

    @Override
    public void setPlacedBy(final Level level, final BlockPos pos, final BlockState state, final LivingEntity placer,
            final ItemStack is) {
        // Inherit the item stack's display name, but only if it's a user defined string rather than a translation
        // component, since our custom naming cannot handle untranslated I18N strings and we would translate it using
        // the server's locale :-(
        var blockEntity = this.getBlockEntity(level, pos);

        if (blockEntity == null) {
            return;
        }

        var hoverName = is.getHoverName();
        if (hoverName instanceof TextComponent text) {
            blockEntity.setName(text.getText());
        }

        if (is.hasTag()) {
            blockEntity.importSettings(SettingsFrom.DISMANTLE_ITEM, is.getTag());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem;
        if (player != null && !player.getItemInHand(hand).isEmpty()) {
            heldItem = player.getItemInHand(hand);

            if (heldItem.getItem() instanceof IMemoryCard memoryCard && !(this instanceof CableBusBlock)) {
                final AEBaseBlockEntity blockEntity = this.getBlockEntity(level, pos);

                if (blockEntity == null) {
                    return InteractionResult.FAIL;
                }

                final String name = this.getDescriptionId();

                if (InteractionUtil.isInAlternateUseMode(player)) {
                    var data = new CompoundTag();
                    blockEntity.exportSettings(SettingsFrom.MEMORY_CARD, data);
                    if (!data.isEmpty()) {
                        memoryCard.setMemoryCardContents(heldItem, name, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                    }
                } else {
                    final String savedName = memoryCard.getSettingsName(heldItem);
                    final CompoundTag data = memoryCard.getData(heldItem);

                    if (this.getDescriptionId().equals(savedName)) {
                        blockEntity.importSettings(SettingsFrom.MEMORY_CARD, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    } else {
                        memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        return this.onActivated(level, pos, player, hand, player.getItemInHand(hand), hit);
    }

    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player player,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public IOrientable getOrientable(final BlockGetter level, final BlockPos pos) {
        return this.getBlockEntity(level, pos);
    }

    /**
     * Returns the BlockState based on the given BlockState while considering the state of the given block entity.
     * <p>
     * If the given block entity is not of the right type for this block, the state is returned unchanged, this is also
     * the case if the given block state does not belong to this block.
     */
    public final BlockState getBlockEntityBlockState(BlockState current, BlockEntity te) {
        if (current.getBlock() != this || !blockEntityClass.isInstance(te)) {
            return current;
        }

        return updateBlockStateFromBlockEntity(current, blockEntityClass.cast(te));
    }

    /**
     * Reimplement this in subclasses to allow block entities to update the state of their block when their own state
     * changes.
     * <p>
     * It is guaranteed that be is not-null and the block of the given block state is this exact block instance.
     */
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, T be) {
        return currentState;
    }

    /**
     * Exports settings of the block entity when it is broken.
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        var drops = super.getDrops(state, builder);
        for (var drop : drops) {
            if (drop.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == this) {
                var lootContext = builder.withParameter(LootContextParams.BLOCK_STATE, state)
                        .create(LootContextParamSets.BLOCK);
                var be = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
                if (be instanceof AEBaseBlockEntity aeBaseBlockEntity) {
                    if (drop.hasTag()) {
                        aeBaseBlockEntity.exportSettings(SettingsFrom.DISMANTLE_ITEM, drop.getTag());
                    } else {
                        var tag = new CompoundTag();
                        aeBaseBlockEntity.exportSettings(SettingsFrom.DISMANTLE_ITEM, tag);
                        if (!tag.isEmpty()) {
                            drop.setTag(tag);
                        }
                    }
                }
                // Export settings at most for one item
                break;
            }
        }
        return drops;
    }
}
