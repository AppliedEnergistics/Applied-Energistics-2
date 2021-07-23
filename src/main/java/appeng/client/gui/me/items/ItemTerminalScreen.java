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

package appeng.client.gui.me.items;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IScrollSource;
import appeng.container.me.common.GridInventoryEntry;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.util.prioritylist.IPartitionList;

public class ItemTerminalScreen<C extends MEMonitorableContainer<IAEItemStack>>
        extends MEMonitorableScreen<IAEItemStack, C> {
    public ItemTerminalScreen(C container, Inventory playerInventory, Component title,
                              ScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    @Override
    protected Repo<IAEItemStack> createRepo(IScrollSource scrollSource) {
        return new ItemRepo(scrollSource, this);
    }

    @Override
    protected IPartitionList<IAEItemStack> createPartitionList(List<ItemStack> viewCells) {
        return ViewCellItem.createFilter(viewCells);
    }

    @Override
    protected void renderGridInventoryEntry(PoseStack matrices, int x, int y,
                                            GridInventoryEntry<IAEItemStack> entry) {
        // Annoying but easier than trying to splice into render item
        ItemStack displayStack = entry.getStack().asItemStackRepresentation();
        SimpleContainer displayInv = new SimpleContainer(displayStack);
        super.renderSlot(matrices, new Slot(displayInv, 0, x, y));
    }

    @Override
    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<IAEItemStack> entry, int mouseButton,
            ClickType clickType) {
        if (entry == null) {
            // The only interaction allowed on an empty virtual slot is putting down the currently held item
            if (clickType == ClickType.PICKUP && !inventory.getCarried().isEmpty()) {
                InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                        : InventoryAction.PICKUP_OR_SET_DOWN;
                menu.handleInteraction(-1, action);
            }
            return;
        }

        long serial = entry.getSerial();

        // Move as many items of a single type as possible
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_SPACE)) {
            menu.handleInteraction(serial, InventoryAction.MOVE_REGION);
        } else {
            InventoryAction action = null;

            switch (clickType) {
                case PICKUP: // pickup / set-down.
                    action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE
                            : InventoryAction.PICKUP_OR_SET_DOWN;

                    if (action == InventoryAction.PICKUP_OR_SET_DOWN
                            && shouldCraftOnClick(entry)
                            && inventory.getCarried().isEmpty()) {
                        menu.handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                        return;
                    }

                    break;
                case QUICK_MOVE:
                    action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                    break;

                case CLONE: // creative dupe:
                    if (entry.isCraftable()) {
                        menu.handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                        return;
                    } else if (inventory.player.abilities.instabuild) {
                        action = InventoryAction.CREATIVE_DUPLICATE;
                    }
                    break;

                default:
                case THROW: // drop item:
            }

            if (action != null) {
                menu.handleInteraction(serial, action);
            }
        }
    }

    private boolean shouldCraftOnClick(GridInventoryEntry<IAEItemStack> entry) {
        // Always auto-craft when viewing only craftable items
        if (isViewOnlyCraftable()) {
            return true;
        }

        // Otherwise only craft if there are no stored items
        return entry.getStoredAmount() == 0 && entry.isCraftable();
    }

}
