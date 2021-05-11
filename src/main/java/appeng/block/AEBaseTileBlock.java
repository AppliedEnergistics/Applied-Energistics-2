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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
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

public abstract class AEBaseTileBlock<T extends AEBaseTileEntity> extends AEBaseBlock {

    @Nonnull
    private Class<T> tileEntityClass;
    @Nonnull
    private Supplier<T> tileEntityFactory;

    public AEBaseTileBlock(final AbstractBlock.Properties props) {
        super(props);
    }

    // TODO : Was this change needed?
    public void setTileEntity(final Class<T> tileEntityClass, Supplier<T> factory) {
        this.tileEntityClass = tileEntityClass;
        this.tileEntityFactory = factory;
        this.setInventory(AEBaseInvTileEntity.class.isAssignableFrom(tileEntityClass));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return this.hasBlockTileEntity();
    }

    private boolean hasBlockTileEntity() {
        return true;
    }

    public Class<T> getTileEntityClass() {
        return this.tileEntityClass;
    }

    @Nullable
    public T getTileEntity(final IBlockReader w, final int x, final int y, final int z) {
        return this.getTileEntity(w, new BlockPos(x, y, z));
    }

    @Nullable
    public T getTileEntity(final IBlockReader w, final BlockPos pos) {
        if (!this.hasBlockTileEntity()) {
            return null;
        }

        final TileEntity te = w.getTileEntity(pos);
        // FIXME: This gets called as part of building the block state cache
        if (this.tileEntityClass != null && this.tileEntityClass.isInstance(te)) {
            return this.tileEntityClass.cast(te);
        }

        return null;
    }

    @Override
    public final TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return this.tileEntityFactory.get();
    }

    @Override
    public void onReplaced(BlockState state, World w, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == state.getBlock()) {
            return; // Just a block state change
        }

        final AEBaseTileEntity te = this.getTileEntity(w, pos);
        if (te != null) {
            final ArrayList<ItemStack> drops = new ArrayList<>();
            te.getDrops(w, pos, drops);

            // Cry ;_; ...
            Platform.spawnDrops(w, pos, drops);
        }

        // super will remove the TE, as it is not an instance of BlockContainer
        super.onReplaced(state, w, pos, newState, isMoving);
    }

    @Override
    public int getComparatorInputOverride(BlockState state, final World w, final BlockPos pos) {
        final TileEntity te = this.getTileEntity(w, pos);
        if (te instanceof AEBaseInvTileEntity) {
            AEBaseInvTileEntity invTile = (AEBaseInvTileEntity) te;
            if (invTile.getInternalInventory().getSlots() > 0) {
                return ItemHandlerHelper.calcRedstoneFromInventory(invTile.getInternalInventory());
            }
        }
        return 0;
    }

    @Override
    public boolean eventReceived(final BlockState state, final World worldIn, final BlockPos pos, final int eventID,
            final int eventParam) {
        super.eventReceived(state, worldIn, pos, eventID, eventParam);
        final TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null ? tileentity.receiveClientEvent(eventID, eventParam) : false;
    }

    @Override
    public void onBlockPlacedBy(final World w, final BlockPos pos, final BlockState state, final LivingEntity placer,
            final ItemStack is) {
        // Inherit the item stack's display name, but only if it's a user defined string
        // rather
        // than a translation component, since our custom naming cannot handle
        // untranslated
        // I18N strings and we would translate it using the server's locale :-(
        AEBaseTileEntity te = this.getTileEntity(w, pos);
        if (te != null && is.hasDisplayName()) {
            ITextComponent displayName = is.getDisplayName();
            if (displayName instanceof StringTextComponent) {
                te.setName(((StringTextComponent) displayName).getText());
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
            Hand hand, BlockRayTraceResult hit) {
        ItemStack heldItem;
        if (player != null && !player.getHeldItem(hand).isEmpty()) {
            heldItem = player.getHeldItem(hand);

            if (InteractionUtil.isWrench(player, heldItem, pos) && InteractionUtil.isInAlternateUseMode(player)) {
                final BlockState blockState = world.getBlockState(pos);
                final Block block = blockState.getBlock();

                final AEBaseTileEntity tile = this.getTileEntity(world, pos);

                if (tile == null) {
                    return ActionResultType.FAIL;
                }

                if (tile instanceof CableBusTileEntity || tile instanceof SkyChestTileEntity) {
                    return ActionResultType.FAIL;
                }

                final ItemStack[] itemDropCandidates = Platform.getBlockDrops(world, pos);
                final ItemStack op = new ItemStack(this);

                for (final ItemStack ol : itemDropCandidates) {
                    if (Platform.itemComparisons().isEqualItemType(ol, op)) {
                        final CompoundNBT tag = tile.downloadSettings(SettingsFrom.DISMANTLE_ITEM);
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

                return ActionResultType.FAIL;
            }

            if (heldItem.getItem() instanceof IMemoryCard && !(this instanceof CableBusBlock)) {
                final IMemoryCard memoryCard = (IMemoryCard) heldItem.getItem();
                final AEBaseTileEntity tileEntity = this.getTileEntity(world, pos);

                if (tileEntity == null) {
                    return ActionResultType.FAIL;
                }

                final String name = this.getTranslationKey();

                if (InteractionUtil.isInAlternateUseMode(player)) {
                    final CompoundNBT data = tileEntity.downloadSettings(SettingsFrom.MEMORY_CARD);
                    if (data != null) {
                        memoryCard.setMemoryCardContents(heldItem, name, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                    }
                } else {
                    final String savedName = memoryCard.getSettingsName(heldItem);
                    final CompoundNBT data = memoryCard.getData(heldItem);

                    if (this.getTranslationKey().equals(savedName)) {
                        tileEntity.uploadSettings(SettingsFrom.MEMORY_CARD, data);
                        memoryCard.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    } else {
                        memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                    }
                }

                return ActionResultType.func_233537_a_(world.isRemote());
            }
        }

        return this.onActivated(world, pos, player, hand, player.getHeldItem(hand), hit);
    }

    public ActionResultType onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockRayTraceResult hit) {
        return ActionResultType.PASS;
    }

    @Override
    public IOrientable getOrientable(final IBlockReader w, final BlockPos pos) {
        return this.getTileEntity(w, pos);
    }

    /**
     * Returns the BlockState based on the given BlockState while considering the state of the given TileEntity.
     * <p>
     * If the given TileEntity is not of the right type for this block, the state is returned unchanged, this is also
     * the case if the given block state does not belong to this block.
     */
    public final BlockState getTileEntityBlockState(BlockState current, TileEntity te) {
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
