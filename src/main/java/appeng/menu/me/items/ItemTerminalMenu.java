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
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.helpers.InventoryAction;
import appeng.menu.MenuLocator;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.IClientRepo;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

/**
 * @see appeng.client.gui.me.items.ItemTerminalScreen
 */
public class ItemTerminalMenu extends MEMonitorableMenu<IAEItemStack> {

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
    protected void handleNetworkInteraction(ServerPlayer player, @Nullable IAEItemStack stack,
            InventoryAction action) {

        // Handle interactions where the player wants to put something into the network
        if (stack == null) {
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
                    CraftAmountMenu.open(player, locator, stack, 1);
                }
                break;

            case SHIFT_CLICK:
                moveOneStackToPlayer(stack);
                break;

            case ROLL_DOWN: {
                // Insert 1 of the carried stack into the network (or at least try to), regardless of what we're
                // hovering in the network inventory.
                var isg = getCarried();
                var ais = StorageChannels.items().createStack(isg);
                if (ais != null) {
                    ais.setStackSize(1);
                    ais = Platform.poweredInsert(powerSource, monitor, ais, this.getActionSource());
                    if (ais == null) {
                        getCarried().shrink(1);
                    }
                }
            }
                break;
            case ROLL_UP:
            case PICKUP_SINGLE:
                // Extract 1 of the hovered stack from the network (or at least try to), and add it to the carried item
                int liftQty = 1;
                var item = getCarried();

                if (!item.isEmpty()) {
                    if (item.getCount() >= item.getMaxStackSize()) {
                        liftQty = 0;
                    }
                    if (!Platform.itemComparisons().isSameItem(stack.getDefinition(), item)) {
                        liftQty = 0;
                    }
                }

                if (liftQty > 0) {
                    IAEItemStack ais = stack.copy().setStackSize(1);
                    ais = Platform.poweredExtraction(powerSource, monitor, ais, this.getActionSource());
                    if (ais != null) {
                        if (item.isEmpty()) {
                            setCarried(ais.createItemStack());
                        } else {
                            // we checked beforehand that max stack size was not reached
                            item.grow((int) ais.getStackSize());
                        }
                    }
                }
                break;
            case PICKUP_OR_SET_DOWN:
                if (!getCarried().isEmpty()) {
                    putCarriedItemIntoNetwork(false);
                } else {
                    IAEItemStack ais = stack.copy();
                    ais.setStackSize(ais.getDefinition().getMaxStackSize());
                    ais = Platform.poweredExtraction(powerSource, monitor, ais,
                            this.getActionSource());
                    if (ais != null) {
                        setCarried(ais.createItemStack());
                    } else {
                        setCarried(ItemStack.EMPTY);
                    }
                }

                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (!getCarried().isEmpty()) {
                    putCarriedItemIntoNetwork(true);
                } else {
                    IAEItemStack ais = stack.copy();
                    final long maxSize = ais.getDefinition().getMaxStackSize();
                    ais.setStackSize(maxSize);
                    ais = monitor.extractItems(ais, Actionable.SIMULATE, this.getActionSource());

                    if (ais != null) {
                        final long stackSize = Math.min(maxSize, ais.getStackSize());
                        ais.setStackSize(stackSize + 1 >> 1);
                        ais = Platform.poweredExtraction(powerSource, monitor, ais,
                                this.getActionSource());
                    }

                    if (ais != null) {
                        setCarried(ais.createItemStack());
                    } else {
                        setCarried(ItemStack.EMPTY);
                    }
                }

                break;
            case CREATIVE_DUPLICATE:
                if (player.getAbilities().instabuild) {
                    final ItemStack is = stack.createItemStack();
                    is.setCount(is.getMaxStackSize());
                    setCarried(is);
                }
                break;
            case MOVE_REGION:
                final int playerInv = player.getInventory().items.size();
                for (int slotNum = 0; slotNum < playerInv; slotNum++) {
                    if (!moveOneStackToPlayer(stack)) {
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

        IAEItemStack stackToInsert = AEItemStack.fromItemStack(heldStack);
        if (stackToInsert == null) {
            return;
        }

        if (singleItem) {
            stackToInsert.setStackSize(1);
        }

        IAEItemStack remainder = Platform.poweredInsert(powerSource, monitor, stackToInsert, this.getActionSource());
        long inserted = stackToInsert.getStackSize() - (remainder == null ? 0 : remainder.getStackSize());

        if (inserted >= heldStack.getCount()) {
            setCarried(ItemStack.EMPTY);
        } else {
            heldStack = heldStack.copy();
            heldStack.setCount(heldStack.getCount() - (int) inserted);
            setCarried(heldStack);
        }
    }

    private boolean moveOneStackToPlayer(IAEItemStack stack) {
        ItemStack myItem = stack.createItemStack();

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

        var ais = stack.copy().setStackSize(toExtract);
        ais = Platform.poweredExtraction(powerSource, monitor, ais, getActionSource());
        if (ais == null) {
            return false; // No items available
        }

        var itemInSlot = playerInv.getItem(slot);
        if (itemInSlot.isEmpty()) {
            playerInv.setItem(slot, ais.createItemStack());
        } else {
            itemInSlot.grow((int) ais.getStackSize());
        }
        return true;
    }

    @Override
    protected ItemStack transferStackToMenu(ItemStack input) {
        if (!canInteractWithGrid()) {
            return super.transferStackToMenu(input);
        }

        final IAEItemStack ais = Platform.poweredInsert(powerSource, monitor,
                StorageChannels.items().createStack(input),
                this.getActionSource());
        return ais == null ? ItemStack.EMPTY : ais.createItemStack();
    }

    public boolean hasItemType(ItemStack itemStack, int amount) {
        IClientRepo<IAEItemStack> clientRepo = getClientRepo();

        if (clientRepo != null) {
            for (GridInventoryEntry<IAEItemStack> stack : clientRepo.getAllEntries()) {
                if (stack.getStack().equals(itemStack)) {
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
