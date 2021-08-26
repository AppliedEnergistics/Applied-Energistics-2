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

import net.minecraft.world.inventory.MenuType;

import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.blockentities.ISegmentedInventory;
import appeng.api.util.IConfigManager;
import appeng.menu.implementations.ItemInterfaceMenu;

public interface IItemInterfaceHost extends IInterfaceHost, ISegmentedInventory {
    DualityItemInterface getInterfaceDuality();

    @Override
    default IConfigManager getConfigManager() {
        return getInterfaceDuality().getConfigManager();
    }

    @Override
    default IUpgradeInventory getUpgrades() {
        return getInterfaceDuality().getUpgrades();
    }

    @Override
    default int getPriority() {
        return getInterfaceDuality().getPriority();
    }

    @Override
    default void setPriority(final int newValue) {
        getInterfaceDuality().setPriority(newValue);
    }

    @Override
    default MenuType<?> getMenuType() {
        return ItemInterfaceMenu.TYPE;
    }
}
