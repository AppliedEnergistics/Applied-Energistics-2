/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.menu.me.items;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEItemKey;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.menu.MenuLocator;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.util.Platform;

/**
 * @see appeng.client.gui.me.items.ItemTerminalScreen
 */
public class ItemTerminalMenu extends MEMonitorableMenu<AEItemKey> {

    public static final MenuType<ItemTerminalMenu> TYPE = MenuTypeBuilder
            .create(ItemTerminalMenu::new, ITerminalHost.class)
            .build("item_terminal");

    public ItemTerminalMenu(int id, Inventory ip, ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public ItemTerminalMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory,
                StorageChannels.items());
    }

    @Override
    protected void handleNetworkInteraction(ServerPlayer player, @Nullable AEItemKey clickedKey,
            InventoryAction action) {

        // Handle interactions where the player wants to put something into the network
        if (clickedKey == null) {
            if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE || action == InventoryAction.ROLL_DOWN) {
                putCarriedItemIntoNetwork(true);
            } else if (action == InventoryAction.PICKUP_OR_SET_DOWN) {
                putCarriedItemIntoNetwork(false);
            }
            return;
        }

        switch (action) {
            case AUTO_CRAFT:
                final MenuLocator locator = getLocator();
                if (locator != null) {
                    CraftAmountMenu.open(player, locator, clickedKey, 1);
                }
                break;

            case SHIFT_CLICK:
                moveOneStackToPlayer(clickedKey);
                break;

            case ROLL_DOWN: {
                // Insert 1 of the carried stack into the network (or at least try to), regardless of what we're
                // hovering in the network inventory.
                var carried = getCarried();
                if (!carried.isEmpty()) {
                    var what = AEItemKey.of(carried);
                    var inserted = StorageHelper.poweredInsert(powerSource, monitor, what, 1, this.getActionSource());
                    if (inserted > 0) {
                        getCarried().shrink(1);
                    }
                }
            }
                break;
            case ROLL_UP:
            case PICKUP_SINGLE: {
                // Extract 1 of the hovered stack from the network (or at least try to), and add it to the carried item
                var item = getCarried();

                if (!item.isEmpty()) {
                    if (item.getCount() >= item.getMaxStackSize()) {
                        return; // Max stack size reached
                    }
                    if (!clickedKey.matches(item)) {
                        return; // Not stackable
                    }
                }

                var extracted = StorageHelper.poweredExtraction(powerSource, monitor, clickedKey, 1,
                        this.getActionSource());
                if (extracted > 0) {
                    if (item.isEmpty()) {
                        setCarried(clickedKey.toStack());
                    } else {
                        // we checked beforehand that max stack size was not reached
                        item.grow(1);
                    }
                }
            }
                break;
            case PICKUP_OR_SET_DOWN: {
                if (!getCarried().isEmpty()) {
                    putCarriedItemIntoNetwork(false);
                } else {
                    var extracted = StorageHelper.poweredExtraction(
                            powerSource,
                            monitor,
                            clickedKey,
                            clickedKey.getItem().getMaxStackSize(),
                            this.getActionSource());
                    if (extracted > 0) {
                        setCarried(clickedKey.toStack((int) extracted));
                    } else {
                        setCarried(ItemStack.EMPTY);
                    }
                }
            }
                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (!getCarried().isEmpty()) {
                    putCarriedItemIntoNetwork(true);
                } else {
                    var extracted = monitor.extract(
                            clickedKey,
                            clickedKey.getItem().getMaxStackSize(),
                            Actionable.SIMULATE,
                            this.getActionSource());

                    if (extracted > 0) {
                        // Half
                        extracted = extracted + 1 >> 1;
                        extracted = StorageHelper.poweredExtraction(powerSource, monitor, clickedKey, extracted,
                                this.getActionSource());
                    }

                    if (extracted > 0) {
                        setCarried(clickedKey.toStack((int) extracted));
                    } else {
                        setCarried(ItemStack.EMPTY);
                    }
                }

                break;
            case CREATIVE_DUPLICATE:
                if (player.getAbilities().instabuild) {
                    var is = clickedKey.toStack();
                    is.setCount(is.getMaxStackSize());
                    setCarried(is);
                }
                break;
            case MOVE_REGION:
                final int playerInv = player.getInventory().items.size();
                for (int slotNum = 0; slotNum < playerInv; slotNum++) {
                    if (!moveOneStackToPlayer(clickedKey)) {
                        break;
                    }
                }
                break;
            default:
                AELog.warn("Received unhandled inventory action %s from client in %s", action, getClass());
                break;
        }
    }

    protected void putCarriedItemIntoNetwork(boolean singleItem) {
        var heldStack = getCarried();

        var what = AEItemKey.of(heldStack);
        if (what == null) {
            return;
        }

        var amount = heldStack.getCount();
        if (singleItem) {
            amount = 1;
        }

        var inserted = StorageHelper.poweredInsert(powerSource, monitor, what, amount,
                this.getActionSource());
        setCarried(Platform.getInsertionRemainder(heldStack, inserted));
    }

    private boolean moveOneStackToPlayer(AEItemKey stack) {
        ItemStack myItem = stack.toStack();

        var playerInv = getPlayerInventory();
        var slot = playerInv.getSlotWithRemainingSpace(myItem);
        int toExtract;
        if (slot != -1) {
            // Try to fill up existing slot with item
            toExtract = myItem.getMaxStackSize() - playerInv.getItem(slot).getCount();
        } else {
            slot = playerInv.getFreeSlot();
            if (slot == -1) {
                return false; // No more free space
            }
            toExtract = myItem.getMaxStackSize();
        }
        if (toExtract <= 0) {
            return false;
        }

        var extracted = StorageHelper.poweredExtraction(powerSource, monitor, stack, toExtract, getActionSource());
        if (extracted == 0) {
            return false; // No items available
        }

        var itemInSlot = playerInv.getItem(slot);
        if (itemInSlot.isEmpty()) {
            playerInv.setItem(slot, stack.toStack((int) extracted));
        } else {
            itemInSlot.grow((int) extracted);
        }
        return true;
    }

    /**
     * Try to transfer an item stack into the grid.
     */
    @Override
    protected ItemStack transferStackToMenu(ItemStack input) {
        if (!canInteractWithGrid()) {
            return super.transferStackToMenu(input);
        }

        var key = AEItemKey.of(input);
        if (key == null) {
            return input;
        }

        var inserted = StorageHelper.poweredInsert(powerSource, monitor,
                key, input.getCount(),
                this.getActionSource());
        return Platform.getInsertionRemainder(input, inserted);
    }

    public boolean hasItemType(ItemStack itemStack, int amount) {
        var clientRepo = getClientRepo();

        if (clientRepo != null) {
            for (var stack : clientRepo.getAllEntries()) {
                if (stack.getWhat().matches(itemStack)) {
                    if (stack.getStoredAmount() >= amount) {
                        return true;
                    }
                    amount -= stack.getStoredAmount();
                }
            }
        }

        return false;
    }

}
