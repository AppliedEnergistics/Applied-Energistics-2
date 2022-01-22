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

package appeng.menu.me.networktool;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.items.contents.NetworkToolMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.RestrictedInputSlot;

/**
 * @see appeng.client.gui.me.networktool.NetworkToolScreen
 */
public class NetworkToolMenu extends AEBaseMenu {

    private static final String ACTION_TOGGLE_FACADE_MODE = "toggleFacadeMode";

    public static final MenuType<NetworkToolMenu> TYPE = MenuTypeBuilder
            .create(NetworkToolMenu::new, NetworkToolMenuHost.class)
            .build("networktool");

    @GuiSync(1)
    public boolean facadeMode;

    public NetworkToolMenu(int id, Inventory ip, NetworkToolMenuHost host) {
        super(TYPE, id, ip, host);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.UPGRADES, host.getInventory(),
                    i), SlotSemantics.STORAGE);
        }

        this.createPlayerInventorySlots(ip);

        registerClientAction(ACTION_TOGGLE_FACADE_MODE, this::toggleFacadeMode);
    }

    public void toggleFacadeMode() {
        if (isClientSide()) {
            sendClientAction(ACTION_TOGGLE_FACADE_MODE);
            return;
        }

        var data = this.itemMenuHost.getItemStack().getOrCreateTag();
        data.putBoolean("hideFacades", !data.getBoolean("hideFacades"));
        this.broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (this.isValidMenu()) {
            var tag = itemMenuHost.getItemStack().getOrCreateTag();
            this.setFacadeMode(tag.getBoolean("hideFacades"));
        }
    }

    public boolean isFacadeMode() {
        return this.facadeMode;
    }

    private void setFacadeMode(boolean facadeMode) {
        this.facadeMode = facadeMode;
    }
}
