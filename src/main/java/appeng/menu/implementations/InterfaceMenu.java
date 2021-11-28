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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import appeng.api.config.SecurityPermissions;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.InterfaceScreen;
import appeng.helpers.InterfaceLogicHost;
import appeng.menu.SlotSemantic;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.FakeSlot;

/**
 * @see InterfaceScreen
 */
public class InterfaceMenu extends UpgradeableMenu<InterfaceLogicHost> {

    public static final String ACTION_OPEN_SET_AMOUNT = "setAmount";

    public static final MenuType<InterfaceMenu> TYPE = MenuTypeBuilder
            .create(InterfaceMenu::new, InterfaceLogicHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("interface");

    public InterfaceMenu(MenuType<InterfaceMenu> menuType, int id, Inventory ip, InterfaceLogicHost host) {
        super(menuType, id, ip, host);

        registerClientAction(ACTION_OPEN_SET_AMOUNT, Integer.class, this::openSetAmountMenu);

        var duality = host.getInterfaceLogic();

        var config = duality.getConfig().createMenuWrapper();
        for (int x = 0; x < config.size(); x++) {
            this.addSlot(new FakeSlot(config, x), SlotSemantic.CONFIG);
        }

        var storage = duality.getStorage().createMenuWrapper();
        for (int x = 0; x < storage.size(); x++) {
            Slot slot;
//            if (menuType == FLUID_TYPE) {
//                // Special case for fluids -> Show as nice tank widget instead of slot
//                slot = new FluidTankSlot(storage, x, duality.getStorage().getCapacity(), "tank" + (1 + x),
//                        ip::placeItemBackInInventory);
//            } else {
            slot = new AppEngSlot(storage, x);
//            }
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

    /**
     * Opens a sub-menu to enter the amount for a config-slot
     *
     * @param configSlot The config slot to enter the amount for.
     */
    public void openSetAmountMenu(int configSlot) {
        if (isClientSide()) {
            sendClientAction(ACTION_OPEN_SET_AMOUNT, configSlot);
        } else {
            var stack = getHost().getConfig().getStack(configSlot);
            if (stack != null) {
                SetStockAmountMenu.open((ServerPlayer) getPlayer(), getLocator(), configSlot,
                        stack.what(), (int) stack.amount());
            }
        }
    }
}
