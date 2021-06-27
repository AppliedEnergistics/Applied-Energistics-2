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

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
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

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public abstract class AbstractStorageCell<T extends IAEStack<T>> extends AEBaseItem implements IStorageCell<T> {
    /**
     * This can be retrieved when disassembling the storage cell.
     */
    protected final IItemProvider coreItem;
    protected final int totalBytes;

    public AbstractStorageCell(Properties properties, final IItemProvider coreItem, final int kilobytes) {
        super(properties);
        this.totalBytes = kilobytes * 1024;
        this.coreItem = coreItem;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
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
    public ActionResult<ItemStack> onItemRightClick(final World world, final PlayerEntity player, final Hand hand) {
        this.disassembleDrive(player.getHeldItem(hand), world, player);
        return new ActionResult<>(ActionResultType.func_233537_a_(world.isRemote()), player.getHeldItem(hand));
    }

    private boolean disassembleDrive(final ItemStack stack, final World world, final PlayerEntity player) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            if (world.isRemote()) {
                return false;
            }

            final PlayerInventory playerInventory = player.inventory;
            final IMEInventoryHandler inv = Api.instance().registries().cell().getCellInventory(stack, null,
                    this.getChannel());
            if (inv != null && playerInventory.getCurrentItem() == stack) {
                final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player);
                final IItemList<IAEItemStack> list = inv.getAvailableItems(this.getChannel().createList());
                if (list.isEmpty() && ia != null) {
                    playerInventory.setInventorySlotContents(playerInventory.currentItem, ItemStack.EMPTY);

                    // drop core
                    final ItemStack extraB = ia.addItems(new ItemStack(coreItem));
                    if (!extraB.isEmpty()) {
                        player.dropItem(extraB, false);
                    }

                    // drop upgrades
                    final IItemHandler upgradesInventory = this.getUpgradesInventory(stack);
                    for (int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSlots(); upgradeIndex++) {
                        final ItemStack upgradeStack = upgradesInventory.getStackInSlot(upgradeIndex);
                        final ItemStack leftStack = ia.addItems(upgradeStack);
                        if (!leftStack.isEmpty() && upgradeStack.getItem() instanceof IUpgradeModule) {
                            player.dropItem(upgradeStack, false);
                        }
                    }

                    // drop empty storage cell case
                    this.dropEmptyStorageCellCase(ia, player);

                    if (player.container != null) {
                        player.container.detectAndSendChanges();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    protected abstract void dropEmptyStorageCellCase(final InventoryAdaptor ia, final PlayerEntity player);

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        return this.disassembleDrive(stack, context.getWorld(), context.getPlayer())
                ? ActionResultType.func_233537_a_(context.getWorld().isRemote())
                : ActionResultType.PASS;
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
