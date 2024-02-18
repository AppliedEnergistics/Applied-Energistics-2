/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.items.contents;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.features.HotkeyAction;
import appeng.api.implementations.menuobjects.IPortableTerminal;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.items.tools.powered.AbstractPortableCell;
import appeng.me.helpers.PlayerSource;
import appeng.me.storage.SupplierStorage;
import appeng.menu.ISubMenu;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.ConfigManager;

/**
 * Hosts the terminal interface for a {@link AbstractPortableCell}.
 */
public class PortableCellMenuHost<T extends AbstractPortableCell> extends ItemMenuHost<T> implements IPortableTerminal {
    private final BiConsumer<Player, ISubMenu> returnMainMenu;
    private final MEStorage cellStorage;
    private final AbstractPortableCell item;
    private ILinkStatus linkStatus = ILinkStatus.ofDisconnected();

    public PortableCellMenuHost(T item, Player player, ItemMenuHostLocator locator,
            BiConsumer<Player, ISubMenu> returnMainMenu) {
        super(item, player, locator);
        Preconditions.checkArgument(getItemStack().is(item), "Stack doesn't match item");
        this.returnMainMenu = returnMainMenu;
        this.cellStorage = new SupplierStorage(new CellStorageSupplier());
        Objects.requireNonNull(cellStorage, "Portable cell doesn't expose a cell inventory.");
        this.item = item;
        this.updateLinkStatus();
    }

    @Override
    public void tick() {
        super.tick();
        consumeIdlePower(Actionable.MODULATE);
        updateLinkStatus();
    }

    @Override
    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        if (linkStatus.connected()) {
            var inv = getInventory();
            if (inv == null) {
                return 0;
            }

            return StorageHelper.poweredInsert(this, inv, what, amount, new PlayerSource(player), mode);
        } else {
            var statusText = linkStatus.statusDescription();
            if (isClientSide() && statusText != null && !mode.isSimulate()) {
                player.displayClientMessage(statusText, false);
            }
            return 0;
        }
    }

    private void updateLinkStatus() {
        if (!consumeIdlePower(Actionable.SIMULATE)) {
            this.linkStatus = ILinkStatus.ofDisconnected(GuiText.OutOfPower.text());
        } else {
            this.linkStatus = ILinkStatus.ofConnected();
        }
    }

    @Override
    public ILinkStatus getLinkStatus() {
        return linkStatus;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);

        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.item.getAECurrentPower(getItemStack())));
        }

        return usePowerMultiplier.divide(this.item.extractAEPower(getItemStack(), amt, Actionable.MODULATE));
    }

    @Override
    public MEStorage getInventory() {
        return cellStorage;
    }

    @Override
    public IConfigManager getConfigManager() {
        var out = new ConfigManager((manager, settingName) -> {
            manager.writeToNBT(getItemStack().getOrCreateTag());
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(getItemStack().getOrCreateTag().copy());
        return out;
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        returnMainMenu.accept(player, subMenu);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return getItemStack();
    }

    public String getCloseHotkey() {
        if (item instanceof IBasicCellItem cellItem) {
            if (cellItem.getKeyType().equals(AEKeyType.items())) {
                return HotkeyAction.PORTABLE_ITEM_CELL;
            } else if (cellItem.getKeyType().equals(AEKeyType.fluids())) {
                return HotkeyAction.PORTABLE_FLUID_CELL;
            }
        }

        return null; // We don't know
    }

    private class CellStorageSupplier implements Supplier<MEStorage> {
        private MEStorage currentStorage;
        private ItemStack currentStack;

        @Override
        public MEStorage get() {
            var stack = getItemStack();
            if (stack != currentStack) {
                currentStorage = StorageCells.getCellInventory(stack, null);
                currentStack = stack;
            }
            return currentStorage;
        }
    }
}
