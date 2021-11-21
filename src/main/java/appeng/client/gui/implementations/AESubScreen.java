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

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.widgets.TabButton;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.menu.ISubMenu;

/**
 * Utility class for sub-screens of other menus that allow returning to the primary menu UI.
 */
public final class AESubScreen {
    private AESubScreen() {
    }

    public static void addBackButton(ISubMenu subMenu, String id, WidgetContainer widgets) {
        addBackButton(subMenu, id, widgets, null);
    }

    public static void addBackButton(ISubMenu subMenu, String id, WidgetContainer widgets,
            @Nullable Component label) {
        var icon = subMenu.getHost().getMainMenuIcon();
        if (label == null) {
            label = icon.getHoverName();
        }
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        TabButton button = new TabButton(icon, label,
                itemRenderer, btn -> {
                    goBack();
                });
        widgets.add(id, button);
    }

    public static void goBack() {
        NetworkHandler.instance().sendToServer(SwitchGuisPacket.returnToParentMenu());
    }

}
