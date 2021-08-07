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

package appeng.menu;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.core.AELog;

/**
 * Allows opening containers generically.
 */
public final class ContainerOpener {

    private ContainerOpener() {
    }

    private static final Map<MenuType<? extends AEBaseContainer>, Opener> registry = new HashMap<>();

    public static <T extends AEBaseContainer> void addOpener(MenuType<T> type, Opener opener) {
        registry.put(type, opener);
    }

    public static boolean openContainer(MenuType<?> type, Player player, ContainerLocator locator) {
        Opener opener = registry.get(type);
        if (opener == null) {
            AELog.warn("Trying to open menu for unknown menu type {}", type);
            return false;
        }

        return opener.open(player, locator);
    }

    @FunctionalInterface
    public interface Opener {

        boolean open(Player player, ContainerLocator locator);

    }

}
