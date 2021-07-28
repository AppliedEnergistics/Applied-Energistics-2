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

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.IOrientable;
import appeng.block.networking.CableBusBlock;
import appeng.tile.AEBaseInvTileEntity;
import appeng.tile.AEBaseTileEntity;
import appeng.tile.networking.CableBusTileEntity;
import appeng.tile.storage.SkyChestTileEntity;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class AEBaseTileBlock<T extends AEBaseTileEntity> extends AEBaseBlock implements EntityBlock {

    @Nonnull
    private Class<T> tileEntityClass;
    @Nonnull
    private BlockEntityType.BlockEntitySupplier<T> tileEntityFactory;

    @Nullable
    private BlockEntityTicker<T> serverTicker;

    @Nullable
    private BlockEntityTicker<T> clientTicker;

    public AEBaseTileBlock(final BlockBehaviour.Properties props) {
        super(props);
    }

    // TODO : Was this change needed?
    public void setTileEntity(final Class<T> tileEntityClass,
            BlockEntityType.BlockEntitySupplier<T> factory,
            BlockEntityTicker<T> clientTicker,
            BlockEntityTicker<T> serverTicker) {
        this.tileEntityClass = tileEntityClass;
        this.tileEntityFactory = factory;
        this.serverTicker = serverTicker;
        this.clientTicker = clientTicker;
        this.setInventory(AEBaseInvTileEntity.class.isAssignableFrom(tileEntityClass));
    }

    @Nullable
    public T getTileEntity(final BlockGetter w, final int x, final int y, final int z) {
        return this.getTileEntity(w, new BlockPos(x, y, z));
    }

    @Nullable
    public T getTileEntity(final BlockGetter w, final BlockPos pos) {
        final BlockEntity te = w.getBlockEntity(pos);
        // FIXME: This gets called as part of building the block state cache
        if (this.tileEntityClass != null && this.tileEntityClass.isInstance(te)) {
            return this.tileEntityClass.cast(te);
        }

        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return tileEntityFactory.create(pos, state);
    }

    // TODO-1.17 Ticker

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState,
            BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) (level.isClientSide() ? clientTicker : serverTicker);
    }

    @Override
    public void onRemove(BlockState state, Level w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final AEBaseTileEntity te = this.getTileEntity(w, pos);
        if (te != null) {
            final ArrayList<ItemStack> drops = new ArrayList<>();
            if (te.dropItems()) {
                te.getDrops(w, pos, drops);
            } else {
                te.getNoDrops(w, pos, drops);
            }

            // Cry ;_; ...
            Platform.spawnDrops(w, pos, drops);
        }

        // super will remove the TE, as it is not an instance of BlockContainer
        super.onRemove(state, w, pos, newState, isMoving);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, final Level w, final BlockPos pos) {
        final BlockEntity te = this.getTileEntity(w, pos);
        if (te instanceof AEBaseInvTileEntity) {
            AEBaseInvTileEntity invTile = (AEBaseInvTileEntity) te;
            if (invTile.getInternalInventory().getSlots() > 0) {
                return ItemHandlerHelper.calcRedstoneFromInventory(invTile.getInternalInventory());
            }
        }
        return 0;
    }

    @Override
    public boolean triggerEvent(final BlockState state, final Level worldIn, final BlockPos pos, final int eventID,
            final int eventParam) {
        super.triggerEvent(state, worldIn, pos, eventID, eventParam);
        final BlockEntity tileentity = worldIn.getBlockEntity(pos);
        return tileentity != null ? tileentity.triggerEvent(eventID, eventParam) : false;
    }

    @Override
    public void setPlacedBy(final Level w, final BlockPos pos, final BlockState state, final LivingEntity placer,
            final ItemStack is) {
        // Inherit the item stack's display name, but only if it's a user defined string
        // rather
        // than a translation component, since our custom naming cannot handle
        // untranslated
        // I18N strings and we would translate it using the server's locale :-(
        AEBaseTileEntity te = this.getTileEntity(w, pos);
        if (te != null && is.hasCustomHoverName()) {
            Component displayName = is.getHoverName();
            if (displayName instanceof TextComponent) {
                te.setName(((TextComponent) displayName).getText());
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem;
        if (player != null && !player.getItemInHand(hand).isEmpty()) {
            heldItem = player.getItemInHand(hand);

            if (InteractionUtil.isWrench(player, heldItem, pos) && InteractionUtil.isInAlternateUseMode(player)) {
                final BlockState blockState = world.getBlockState(pos);
                final Block block = blockState.getBlock();

                final AEBaseTileEntity tile = this.getTileEntity(world, pos);

                if (tile == null) {
                    return InteractionResult.FAIL;
                }

                if (tile instanceof CableBusTileEntity || tile instanceof SkyChestTileEntity) {
                    return InteractionResult.FAIL;
                }

                final ItemStack[] itemDropCandidates = Platform.getBlockDrops(world, pos);
                final ItemStack op = new ItemStack(this);

                for (final ItemStack ol : itemDropCandidates) {
                    if (Platform.itemComparisons().isEqualItemType(ol, op)) {
                        final CompoundTag tag = tile.downloadSettings(SettingsFrom.DISMANTLE_ITEM);
                        if (tag != null) {
                            ol.setTag(tag);
                        }
                    }
                }

                if (block.removedByPlayer(blockState, world, pos, player, false, world.getFluidState(pos))) {
                    final List<ItemStack> itemsToDrop = Lists.newArrayList(itemDropCandidates);
                    Platform.spawnDrops(world, pos, itemsToDrop);
                    world.removeBlock(pos, false);
                }

                return InteractionResult.FAIL;
            }

            if (heldItem.getItem() instanceof IMemoryCard && !(this instanceof CableBusBlock)) {
                final IMemoryCard memoryCard = (IMemoryCard) heldItem.getItem();
                final AEBaseTileEntity tileEntity = this.getTileEntity(world, pos);

                if (tileEntity == null) {
                    return InteractionResult.FAIL;
                }

                final String name = this.getDescriptionId();

                if (InteractionUtil.isInAlternateUseMode(player)) {
                    final CompoundTag data = tileEntity.downloadSettings(SettingsFrom.MEMORY_CARD);
                    if (data != null) {
                        memoryCard.setMemoryCardContents(heldItem, name, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                    }
                } else {
                    final String savedName = memoryCard.getSettingsName(heldItem);
                    final CompoundTag data = memoryCard.getData(heldItem);

                    if (this.getDescriptionId().equals(savedName)) {
                        tileEntity.uploadSettings(SettingsFrom.MEMORY_CARD, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    } else {
                        memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                    }
                }

                return InteractionResult.sidedSuccess(world.isClientSide());
            }
        }

        return this.onActivated(world, pos, player, hand, player.getItemInHand(hand), hit);
    }

    public InteractionResult onActivated(final Level w, final BlockPos pos, final Player player,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public IOrientable getOrientable(final BlockGetter w, final BlockPos pos) {
        return this.getTileEntity(w, pos);
    }

    /**
     * Returns the BlockState based on the given BlockState while considering the state of the given TileEntity.
     * <p>
     * If the given TileEntity is not of the right type for this block, the state is returned unchanged, this is also
     * the case if the given block state does not belong to this block.
     */
    public final BlockState getTileEntityBlockState(BlockState current, BlockEntity te) {
        if (current.getBlock() != this || !tileEntityClass.isInstance(te)) {
            return current;
        }

        return updateBlockStateFromTileEntity(current, tileEntityClass.cast(te));
    }

    /**
     * Reimplement this in subclasses to allow tile-entities to update the state of their block when their own state
     * changes.
     * <p>
     * It is guaranteed that te is not-null and the block of the given block state is this exact block instance.
     */
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, T te) {
        return currentState;
    }

}
