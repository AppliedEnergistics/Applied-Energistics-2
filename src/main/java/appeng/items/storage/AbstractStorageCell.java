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

import alexiil.mc.lib.attributes.item.FixedItemInv;
import appeng.hooks.AEToolItem;
import net.fabricmc.api.EnvType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.fabricmc.api.Environment;

import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.api.features.AEFeature;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.materials.MaterialType;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

/**
 * @author DrummerMC
 * @version rv6 - 2018-01-17
 * @since rv6 2018-01-17
 */
public abstract class AbstractStorageCell<T extends IAEStack<T>> extends AEBaseItem implements IStorageCell<T>, AEToolItem {
    protected final MaterialType component;
    protected final int totalBytes;

    public AbstractStorageCell(Settings properties, final MaterialType whichCell, final int kilobytes) {
        super(properties);
        this.totalBytes = kilobytes * 1024;
        this.component = whichCell;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(final ItemStack stack, final World world, final List<Text> lines,
            final TooltipContext advancedTooltips) {
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
    public FixedItemInv getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public FixedItemInv getConfigInventory(final ItemStack is) {
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
    public TypedActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        this.disassembleDrive(player.getStackInHand(hand), world, player);
        return new TypedActionResult<>(ActionResult.SUCCESS, player.getStackInHand(hand));
    }

    private boolean disassembleDrive(final ItemStack stack, final World world, final PlayerEntity player) {
        if (player.isInSneakingPose()) {
            if (Platform.isClient()) {
                return false;
            }

            final PlayerInventory playerInventory = player.inventory;
            final IMEInventoryHandler inv = Api.instance().registries().cell().getCellInventory(stack, null,
                    this.getChannel());
            if (inv != null && playerInventory.getMainHandStack() == stack) {
                final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player);
                final IItemList<IAEItemStack> list = inv.getAvailableItems(this.getChannel().createList());
                if (list.isEmpty() && ia != null) {
                    playerInventory.setStack(playerInventory.selectedSlot, ItemStack.EMPTY);

                    // drop core
                    final ItemStack extraB = ia.addItems(this.component.stack(1));
                    if (!extraB.isEmpty()) {
                        player.dropItem(extraB, false);
                    }

                    // drop upgrades
                    final FixedItemInv upgradesInventory = this.getUpgradesInventory(stack);
                    for (int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSlotCount(); upgradeIndex++) {
                        final ItemStack upgradeStack = upgradesInventory.getInvStack(upgradeIndex);
                        final ItemStack leftStack = ia.addItems(upgradeStack);
                        if (!leftStack.isEmpty() && upgradeStack.getItem() instanceof IUpgradeModule) {
                            player.dropItem(upgradeStack, false);
                        }
                    }

                    // drop empty storage cell case
                    this.dropEmptyStorageCellCase(ia, player);

                    if (player.currentScreenHandler != null) {
                        player.currentScreenHandler.sendContentUpdates();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    protected abstract void dropEmptyStorageCellCase(final InventoryAdaptor ia, final PlayerEntity player);

    @Override
    public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
        return this.disassembleDrive(stack, context.getWorld(), context.getPlayer()) ? ActionResult.SUCCESS
                : ActionResult.PASS;
    }

// FIXME FABRIC: Handle this in the disassemble recipe
// FIXME FABRIC    @Override
// FIXME FABRIC    public ItemStack getRecipeRemainder(final ItemStack itemStack) {
// FIXME FABRIC        return Api.instance().definitions().materials().emptyStorageCell().maybeStack(1)
// FIXME FABRIC                .orElseThrow(() -> new MissingDefinitionException(
// FIXME FABRIC                        "Tried to use empty storage cells while basic storage cells are defined."));
// FIXME FABRIC    }
// FIXME FABRIC
// FIXME FABRIC    @Override
// FIXME FABRIC    public boolean hasRecipeRemainder(final ItemStack stack) {
// FIXME FABRIC        return AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_DISASSEMBLY_CRAFTING);
// FIXME FABRIC    }

}
