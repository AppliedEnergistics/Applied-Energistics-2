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

package appeng.client.gui.implementations;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.blockentity.storage.ChestBlockEntity;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.widgets.TabButton;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.menu.implementations.ChestMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.ItemTerminalMenu;
import appeng.menu.me.items.PatternTermMenu;
import appeng.menu.me.items.WirelessTermMenu;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.ItemTerminalPart;
import appeng.parts.reporting.PatternTerminalPart;

/**
 * Utility class for sub-screens of other menus that allow returning to the primary menu UI.
 */
public final class AESubScreen {

    private final MenuType<?> previousMenuType;
    private final ItemStack previousMenuIcon;

    /**
     * Based on the menu we're opening for, try to determine what it's "primary" GUI would be so that we can go back to
     * it.
     */
    public AESubScreen(Object menuHost) {

        if (menuHost instanceof ChestBlockEntity) {
            // A chest is also a priority host, but the priority _interface_ can only be
            // opened from the
            // chest ui that doesn't actually show the contents of the inserted cell.
            IPriorityHost priorityHost = (IPriorityHost) menuHost;
            this.previousMenuIcon = priorityHost.getItemStackRepresentation();
            this.previousMenuType = ChestMenu.TYPE;
        } else if (menuHost instanceof IPriorityHost priorityHost) {
            this.previousMenuIcon = priorityHost.getItemStackRepresentation();
            this.previousMenuType = priorityHost.getMenuType();
        } else if (menuHost instanceof WirelessTerminalGuiObject) {
            this.previousMenuIcon = AEItems.WIRELESS_TERMINAL.stack();
            this.previousMenuType = WirelessTermMenu.TYPE;
        } else if (menuHost instanceof ItemTerminalPart) {
            this.previousMenuIcon = AEParts.TERMINAL.stack();
            this.previousMenuType = ItemTerminalMenu.TYPE;
        } else if (menuHost instanceof CraftingTerminalPart) {
            this.previousMenuIcon = AEParts.CRAFTING_TERMINAL.stack();
            this.previousMenuType = CraftingTermMenu.TYPE;
        } else if (menuHost instanceof PatternTerminalPart) {
            this.previousMenuIcon = AEParts.PATTERN_TERMINAL.stack();
            this.previousMenuType = PatternTermMenu.TYPE;
        } else {
            this.previousMenuIcon = null;
            this.previousMenuType = null;
        }
    }

    public TabButton addBackButton(String id, WidgetContainer widgets) {
        return addBackButton(id, widgets, null);
    }

    public TabButton addBackButton(String id, WidgetContainer widgets, @Nullable Component label) {
        if (this.previousMenuType != null && !previousMenuIcon.isEmpty()) {
            if (label == null) {
                label = previousMenuIcon.getHoverName();
            }
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            TabButton button = new TabButton(previousMenuIcon, label,
                    itemRenderer, btn -> goBack());
            widgets.add(id, button);
            return button;
        }
        return null;
    }

    public void goBack() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(this.previousMenuType));
    }

}
