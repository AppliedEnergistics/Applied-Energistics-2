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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import appeng.api.config.SecurityPermissions;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.helpers.IInterfaceHost;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;
import appeng.menu.slot.FluidTankSlot;

/**
 * @see InterfaceScreen
 */
public class InterfaceMenu extends UpgradeableMenu<IInterfaceHost> {

    public static final MenuType<InterfaceMenu> ITEM_TYPE = MenuTypeBuilder
            .create(InterfaceMenu::new, IInterfaceHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("item_interface");

    public static final MenuType<InterfaceMenu> FLUID_TYPE = MenuTypeBuilder
            .create(InterfaceMenu::new, IInterfaceHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("fluid_interface");

    public InterfaceMenu(MenuType<InterfaceMenu> menuType, int id, Inventory ip, IInterfaceHost host) {
        super(menuType, id, ip, host);

        var duality = host.getInterfaceDuality();

        var config = duality.getConfig().createMenuWrapper();
        for (int x = 0; x < config.size(); x++) {
            this.addSlot(new FakeSlot(config, x), SlotSemantic.CONFIG);
        }

        var storage = duality.getStorage().createMenuWrapper();
        for (int x = 0; x < storage.size(); x++) {
            Slot slot;
            if (menuType == FLUID_TYPE) {
                // Special case for fluids -> Show as nice tank widget instead of slot
                slot = new FluidTankSlot(storage, x, duality.getStorage().getCapacity(), "tank" + (1 + x),
                        ip::placeItemBackInInventory);
            } else {
                slot = new AppEngSlot(storage, x);
            }
            this.addSlot(slot, SlotSemantic.STORAGE);
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }
}
