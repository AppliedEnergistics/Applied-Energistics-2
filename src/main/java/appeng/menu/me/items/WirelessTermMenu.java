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

package appeng.menu.me.items;

import net.minecraft.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.menu.implementations.MenuTypeBuilder;

/**
 * @see appeng.client.gui.implementations.WirelessScreen
 */
public class WirelessTermMenu extends MEPortableCellMenu {

    public static final MenuType<WirelessTermMenu> TYPE = MenuTypeBuilder
            .create(WirelessTermMenu::new, WirelessTerminalGuiObject.class)
            .build("wirelessterm");

    private final WirelessTerminalGuiObject wirelessTerminalGUIObject;

    public WirelessTermMenu(int id, final Inventory ip, final WirelessTerminalGuiObject gui) {
        super(TYPE, id, ip, gui);
        this.wirelessTerminalGUIObject = gui;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!this.wirelessTerminalGUIObject.rangeCheck()) {
            if (isServer() && this.isValidMenu()) {
                this.getPlayerInventory().player.sendMessage(PlayerMessages.OutOfRange.get(), Util.NIL_UUID);
            }

            this.setValidMenu(false);
        } else {
            this.setPowerMultiplier(
                    AEConfig.instance().wireless_getDrainRate(this.wirelessTerminalGUIObject.getRange()));
        }
    }
}
