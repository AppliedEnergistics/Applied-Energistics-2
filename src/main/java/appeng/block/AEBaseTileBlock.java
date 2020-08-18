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
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.IOrientable;
import appeng.block.networking.CableBusBlock;
import appeng.tile.AEBaseBlockEntity;
import appeng.tile.AEBaseInvBlockEntity;
import appeng.tile.networking.CableBusBlockEntity;
import appeng.tile.storage.SkyChestBlockEntity;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class AEBaseTileBlock<T extends AEBaseBlockEntity> extends AEBaseBlock
        implements BlockEntityProvider, AttributeProvider {

    @Nonnull
    private Class<T> blockEntityClass;
    @Nonnull
    private Supplier<T> tileEntityFactory;

    public AEBaseTileBlock(final Settings props) {
        super(props);
    }

    // TODO : Was this change needed?
    public void setTileEntity(final Class<T> tileEntityClass, Supplier<T> factory) {
        this.blockEntityClass = tileEntityClass;
        this.tileEntityFactory = factory;
        this.setInventory(AEBaseInvBlockEntity.class.isAssignableFrom(tileEntityClass));
    }

    public Class<T> getBlockEntityClass() {
        return this.blockEntityClass;
    }

    @Nullable
    public T getBlockEntity(final BlockView w, final int x, final int y, final int z) {
        return this.getBlockEntity(w, new BlockPos(x, y, z));
    }

    @Nullable
    public T getBlockEntity(final BlockView w, final BlockPos pos) {

        final BlockEntity te = w.getBlockEntity(pos);
        // FIXME: This gets called as part of building the block state cache
        if (this.blockEntityClass != null && this.blockEntityClass.isInstance(te)) {
            return this.blockEntityClass.cast(te);
        }

        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return this.tileEntityFactory.get();
    }

    @Override
    public void onStateReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final AEBaseBlockEntity te = this.getBlockEntity(w, pos);
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
        super.onStateReplaced(state, w, pos, newState, isMoving);
    }

    @Override
    public int getComparatorOutput(BlockState state, final World w, final BlockPos pos) {
        final BlockEntity te = this.getBlockEntity(w, pos);
        if (te instanceof AEBaseInvBlockEntity) {
            AEBaseInvBlockEntity invTile = (AEBaseInvBlockEntity) te;
            if (invTile.getInternalInventory().getSlotCount() > 0) {
                return getRedstoneFromFixedItemInv(invTile.getInternalInventory());
            }
        }
        return 0;
    }

    /**
     * Calculate redstone output level. 0 if completely empty, 1 if _any_ item is
     * present, up to 15 if all slots are full.
     */
    private int getRedstoneFromFixedItemInv(FixedItemInv inv) {
        boolean foundAnything = false; // ANY slots non-empty?
        float fillRatio = 0;

        for (int i = 0; i < inv.getSlotCount(); ++i) {
            ItemStack stack = inv.getInvStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            int slotMaxCount = inv.getMaxAmount(i, stack);
            fillRatio += stack.getCount() / (float) Math.min(slotMaxCount, stack.getMaxCount());
            foundAnything = true;
        }

        // Average the ratio across all slots
        fillRatio /= inv.getSlotCount();

        // Always return at least non-zero if _any_ slots are non-empty
        return (foundAnything ? 1 : 0) + MathHelper.floor(fillRatio * 14.0f);
    }

    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        super.onSyncedBlockEvent(state, world, pos, type, data);
        final BlockEntity tileentity = world.getBlockEntity(pos);
        return tileentity != null && tileentity.onSyncedBlockEvent(type, data);
    }

    @Override
    public void onPlaced(final World w, final BlockPos pos, final BlockState state, final LivingEntity placer,
            final ItemStack is) {
        // Inherit the item stack's display name, but only if it's a user defined string
        // rather
        // than a translation component, since our custom naming cannot handle
        // untranslated
        // I18N strings and we would translate it using the server's locale :-(
        AEBaseBlockEntity te = this.getBlockEntity(w, pos);
        if (te != null && is.hasCustomName()) {
            Text displayName = is.getName();
            if (displayName instanceof LiteralText) {
                te.setName(((LiteralText) displayName).getRawString());
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
            BlockHitResult hit) {
        ItemStack heldItem;
        if (player != null && !player.getStackInHand(hand).isEmpty()) {
            heldItem = player.getStackInHand(hand);

            if (Platform.isWrench(player, heldItem, pos) && player.isInSneakingPose()) {
                final BlockState blockState = world.getBlockState(pos);
                final Block block = blockState.getBlock();

                final AEBaseBlockEntity tile = this.getBlockEntity(world, pos);

                if (tile == null) {
                    return ActionResult.FAIL;
                }

                if (tile instanceof CableBusBlockEntity || tile instanceof SkyChestBlockEntity) {
                    return ActionResult.FAIL;
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

                block.onBreak(world, pos, blockState, player);
                boolean bl = world.removeBlock(pos, false);
                if (bl) {
                    block.onBroken(world, pos, blockState);
                    final List<ItemStack> itemsToDrop = Lists.newArrayList(itemDropCandidates);
                    Platform.spawnDrops(world, pos, itemsToDrop);
                }

                return ActionResult.SUCCESS;
            }

            if (heldItem.getItem() instanceof IMemoryCard && !(this instanceof CableBusBlock)) {
                final IMemoryCard memoryCard = (IMemoryCard) heldItem.getItem();
                final AEBaseBlockEntity tileEntity = this.getBlockEntity(world, pos);

                if (tileEntity == null) {
                    return ActionResult.FAIL;
                }

                final String name = this.getTranslationKey();

                if (player.isInSneakingPose()) {
                    final CompoundTag data = tileEntity.downloadSettings(SettingsFrom.MEMORY_CARD);
                    if (data != null) {
                        memoryCard.setMemoryCardContents(heldItem, name, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                    }
                } else {
                    final String savedName = memoryCard.getSettingsName(heldItem);
                    final CompoundTag data = memoryCard.getData(heldItem);

                    if (this.getTranslationKey().equals(savedName)) {
                        tileEntity.uploadSettings(SettingsFrom.MEMORY_CARD, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    } else {
                        memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                    }
                }

                return ActionResult.SUCCESS;
            }
        }

        return this.onActivated(world, pos, player, hand, player.getStackInHand(hand), hit);
    }

    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        return ActionResult.PASS;
    }

    @Override
    public IOrientable getOrientable(final BlockView w, final BlockPos pos) {
        return this.getBlockEntity(w, pos);
    }

    /**
     * Returns the BlockState based on the given BlockState while considering the
     * state of the given TileEntity.
     * <p>
     * If the given TileEntity is not of the right type for this block, the state is
     * returned unchanged, this is also the case if the given block state does not
     * belong to this block.
     */
    public final BlockState getBlockEntityBlockState(BlockState current, BlockEntity te) {
        if (current.getBlock() != this || !blockEntityClass.isInstance(te)) {
            return current;
        }

        return updateBlockStateFromTileEntity(current, blockEntityClass.cast(te));
    }

    /**
     * Reimplement this in subclasses to allow tile-entities to update the state of
     * their block when their own state changes.
     * <p>
     * It is guaranteed that te is not-null and the block of the given block state
     * is this exact block instance.
     */
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, T te) {
        return currentState;
    }

    // Gives our tile entity a chance to provide it's attributes
    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        T te = getBlockEntity(world, pos);
        if (te != null) {
            te.addAllAttributes(world, pos, state, to);
        }
    }

}
