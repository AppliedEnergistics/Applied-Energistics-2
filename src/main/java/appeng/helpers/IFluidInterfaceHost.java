/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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
import appeng.api.util.IConfigManager;
import appeng.menu.implementations.FluidInterfaceMenu;
import appeng.parts.automation.EmptyUpgradeInventory;

public interface IFluidInterfaceHost extends IInterfaceHost {
    DualityFluidInterface getDualityFluidInterface();

    @Override
    default IConfigManager getConfigManager() {
        return getDualityFluidInterface().getConfigManager();
    }

    @Override
    default IUpgradeInventory getUpgrades() {
        return EmptyUpgradeInventory.INSTANCE;
    }

    @Override
    default int getPriority() {
        return getDualityFluidInterface().getPriority();
    }

    @Override
    default void setPriority(final int newValue) {
        getDualityFluidInterface().setPriority(newValue);
    }

    @Override
    default MenuType<?> getMenuType() {
        return FluidInterfaceMenu.TYPE;
    }

}
