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

package appeng.helpers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.helpers.iface.GenericStackInv;
import appeng.menu.ISubMenu;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.InterfaceMenu;

/**
 * Interface that must be implemented by machines hosting {@link InterfaceLogic}.
 */
public interface InterfaceLogicHost extends IConfigurableObject, IUpgradeableObject, IPriorityHost, IConfigInvHost {
    /**
     * @return The block entity that is in-world and hosts the interface.
     */
    BlockEntity getBlockEntity();

    void saveChanges();

    InterfaceLogic getInterfaceLogic();

    @Override
    default IConfigManager getConfigManager() {
        return getInterfaceLogic().getConfigManager();
    }

    @Override
    default IUpgradeInventory getUpgrades() {
        return getInterfaceLogic().getUpgrades();
    }

    @Override
    default int getPriority() {
        return getInterfaceLogic().getPriority();
    }

    @Override
    default void setPriority(final int newValue) {
        getInterfaceLogic().setPriority(newValue);
    }

    @Override
    default GenericStackInv getConfig() {
        return getInterfaceLogic().getConfig();
    }

    default void openMenu(Player player, MenuLocator locator) {
        MenuOpener.open(InterfaceMenu.TYPE, player, locator);
    }

    @Override
    default void returnToMainMenu(Player player, ISubMenu subMenu) {
        openMenu(player, subMenu.getLocator());
    }
}
