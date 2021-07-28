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

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.ChestContainer;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.container.me.items.WirelessTermContainer;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.IPriorityHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.TerminalPart;
import appeng.tile.storage.ChestTileEntity;

/**
 * Utility class for sub-screens of other containers that allow returning to the primary container UI.
 */
public final class AESubScreen {

    private final MenuType<?> previousContainerType;
    private final ItemStack previousContainerIcon;

    /**
     * Based on the container we're opening for, try to determine what it's "primary" GUI would be so that we can go
     * back to it.
     */
    public AESubScreen(Object containerHost) {

        if (containerHost instanceof ChestTileEntity) {
            // A chest is also a priority host, but the priority _interface_ can only be
            // opened from the
            // chest ui that doesn't actually show the contents of the inserted cell.
            IPriorityHost priorityHost = (IPriorityHost) containerHost;
            this.previousContainerIcon = priorityHost.getItemStackRepresentation();
            this.previousContainerType = ChestContainer.TYPE;
        } else if (containerHost instanceof IPriorityHost) {
            IPriorityHost priorityHost = (IPriorityHost) containerHost;
            this.previousContainerIcon = priorityHost.getItemStackRepresentation();
            this.previousContainerType = priorityHost.getContainerType();
        } else if (containerHost instanceof WirelessTerminalGuiObject) {
            this.previousContainerIcon = AEItems.WIRELESS_TERMINAL.stack();
            this.previousContainerType = WirelessTermContainer.TYPE;
        } else if (containerHost instanceof TerminalPart) {
            this.previousContainerIcon = AEParts.TERMINAL.stack();
            this.previousContainerType = ItemTerminalContainer.TYPE;
        } else if (containerHost instanceof CraftingTerminalPart) {
            this.previousContainerIcon = AEParts.CRAFTING_TERMINAL.stack();
            this.previousContainerType = CraftingTermContainer.TYPE;
        } else if (containerHost instanceof PatternTerminalPart) {
            this.previousContainerIcon = AEParts.PATTERN_TERMINAL.stack();
            this.previousContainerType = PatternTermContainer.TYPE;
        } else {
            this.previousContainerIcon = null;
            this.previousContainerType = null;
        }
    }

    public TabButton addBackButton(String id, WidgetContainer widgets) {
        return addBackButton(id, widgets, null);
    }

    public TabButton addBackButton(String id, WidgetContainer widgets, @Nullable Component label) {
        if (this.previousContainerType != null && !previousContainerIcon.isEmpty()) {
            if (label == null) {
                label = previousContainerIcon.getHoverName();
            }
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            TabButton button = new TabButton(previousContainerIcon, label,
                    itemRenderer, btn -> goBack());
            widgets.add(id, button);
            return button;
        }
        return null;
    }

    public void goBack() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(this.previousContainerType));
    }

}
