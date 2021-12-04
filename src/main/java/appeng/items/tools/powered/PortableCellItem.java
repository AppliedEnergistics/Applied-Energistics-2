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

package appeng.items.tools.powered;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.stacks.AEKey;
import appeng.core.AEConfig;
import appeng.hooks.ICustomReequipAnimation;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.contents.PortableCellMenuHost;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.ConfigInventory;

public class PortableCellItem extends AEBasePoweredItem
        implements IBasicCellItem, IMenuItem, ICustomReequipAnimation {

    public static final StorageTier SIZE_1K = new StorageTier(512, 54, 8);
    public static final StorageTier SIZE_4K = new StorageTier(2048, 45, 32);
    public static final StorageTier SIZE_16K = new StorageTier(8192, 36, 128);
    public static final StorageTier SIZE_64K = new StorageTier(16834, 27, 512);

    private final StorageTier tier;
    private final AEKeyType keyType;
    private final MenuType<?> menuType;

    public PortableCellItem(AEKeyType keyType, MenuType<?> menuType, StorageTier tier, Properties props) {
        super(AEConfig.instance().getPortableCellBattery(), props);
        this.menuType = menuType;
        this.tier = tier;
        this.keyType = keyType;
    }

    @Override
    public double getChargeRate() {
        return 80d;
    }

    /**
     * Open a wireless terminal from a slot in the player inventory, i.e. activated via hotkey.
     *
     * @return True if the menu was opened.
     */
    public boolean openFromInventory(Player player, int inventorySlot) {
        var is = player.getInventory().getItem(inventorySlot);
        if (is.getItem() == this) {
            return MenuOpener.open(getMenuType(), player, MenuLocator.forInventorySlot(inventorySlot));
        } else {
            return false;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        if (!level.isClientSide()) {
            MenuOpener.open(getMenuType(), player, MenuLocator.forHand(player, hand));
        }
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level level, final List<Component> lines,
            final TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return this.tier.bytes();
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return this.tier.bytesPerType();
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return this.tier.types();
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public UpgradeInventory getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public ConfigInventory getConfigInventory(final ItemStack is) {
        return CellConfig.create(keyType.filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
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
    public PortableCellMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, BlockPos pos) {
        return new PortableCellMenuHost(player, inventorySlot, this, stack,
                (p, sm) -> openFromInventory(p, inventorySlot));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    /**
     * Tries inserting into a portable cell without having to open it.
     * 
     * @return Amount inserted.
     */
    public long insert(Player player, ItemStack itemStack, AEKey what, long amount, Actionable mode) {
        if (keyType.tryCast(what) == null) {
            return 0;
        }

        var host = getMenuHost(player, -1, itemStack, null);
        if (host == null) {
            return 0;
        }

        var inv = host.getInventory();
        if (inv != null) {
            return StorageHelper.poweredInsert(
                    host,
                    inv,
                    what,
                    amount,
                    new PlayerSource(player),
                    mode);
        }
        return 0;
    }

    public record StorageTier(int bytes, int types, int bytesPerType) {
    }

    @Override
    public AEKeyType getKeyType() {
        return keyType;
    }

    public MenuType<?> getMenuType() {
        return menuType;
    }
}
