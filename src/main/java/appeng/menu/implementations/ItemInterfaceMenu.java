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

import appeng.api.config.SecurityPermissions;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.ItemInterfaceScreen;
import appeng.helpers.DualityItemInterface;
import appeng.helpers.IItemInterfaceHost;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;

/**
 * @see ItemInterfaceScreen
 */
public class ItemInterfaceMenu extends UpgradeableMenu<IItemInterfaceHost> {

    public static final MenuType<ItemInterfaceMenu> TYPE = MenuTypeBuilder
            .create(ItemInterfaceMenu::new, IItemInterfaceHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("interface");

    public ItemInterfaceMenu(int id, final Inventory ip, IItemInterfaceHost host) {
        super(TYPE, id, ip, host);

        DualityItemInterface duality = host.getInterfaceDuality();

        for (int x = 0; x < DualityItemInterface.NUMBER_OF_CONFIG_SLOTS; x++) {
            this.addSlot(new FakeSlot(duality.getConfig(), x), SlotSemantic.CONFIG);
        }

        for (int x = 0; x < DualityItemInterface.NUMBER_OF_STORAGE_SLOTS; x++) {
            this.addSlot(new AppEngSlot(duality.getStorage(), x), SlotSemantic.STORAGE);
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
