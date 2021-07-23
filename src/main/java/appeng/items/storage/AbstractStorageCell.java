/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.items.storage;

import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InteractionUtil;
import appeng.util.InventoryAdaptor;

import net.minecraft.world.item.Item.Properties;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public abstract class AbstractStorageCell<T extends IAEStack<T>> extends AEBaseItem implements IStorageCell<T> {
    /**
     * This can be retrieved when disassembling the storage cell.
     */
    protected final ItemLike coreItem;
    protected final int totalBytes;

    public AbstractStorageCell(Item.Properties properties, final ItemLike coreItem, final int kilobytes) {
        super(properties);
        this.totalBytes = kilobytes * 1024;
        this.coreItem = coreItem;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level world, final List<Component> lines,
                                final TooltipFlag advancedTooltips) {
        Api.instance().client().addCellInformation(
                Api.instance().registries().cell().getCellInventory(stack, null, this.getChannel()), lines);
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return this.totalBytes;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 63;
    }

    @Override
    public boolean isBlackListed(final ItemStack cellItem, final T requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        if (fz.isEmpty()) {
            return FuzzyMode.IGNORE_ALL;
        }
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player player, final InteractionHand hand) {
        this.disassembleDrive(player.getItemInHand(hand), world, player);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(world.isClientSide()), player.getItemInHand(hand));
    }

    private boolean disassembleDrive(final ItemStack stack, final Level world, final Player player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (world.isClientSide()) {
                return false;
            }

            final Inventory playerInventory = player.inventory;
            final IMEInventoryHandler inv = Api.instance().registries().cell().getCellInventory(stack, null,
                    this.getChannel());
            if (inv != null && playerInventory.getSelected() == stack) {
                final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player);
                final IItemList<IAEItemStack> list = inv.getAvailableItems(this.getChannel().createList());
                if (list.isEmpty() && ia != null) {
                    playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);

                    // drop core
                    final ItemStack extraB = ia.addItems(new ItemStack(coreItem));
                    if (!extraB.isEmpty()) {
                        player.drop(extraB, false);
                    }

                    // drop upgrades
                    final IItemHandler upgradesInventory = this.getUpgradesInventory(stack);
                    for (int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSlots(); upgradeIndex++) {
                        final ItemStack upgradeStack = upgradesInventory.getStackInSlot(upgradeIndex);
                        final ItemStack leftStack = ia.addItems(upgradeStack);
                        if (!leftStack.isEmpty() && upgradeStack.getItem() instanceof IUpgradeModule) {
                            player.drop(upgradeStack, false);
                        }
                    }

                    // drop empty storage cell case
                    this.dropEmptyStorageCellCase(ia, player);

                    if (player.inventoryMenu != null) {
                        player.inventoryMenu.broadcastChanges();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    protected abstract void dropEmptyStorageCellCase(final InventoryAdaptor ia, final Player player);

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.disassembleDrive(stack, context.getLevel(), context.getPlayer())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide())
                : InteractionResult.PASS;
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        return AEItems.EMPTY_STORAGE_CELL.stack();
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return AEConfig.instance().isDisassemblyCraftingEnabled();
    }

}
