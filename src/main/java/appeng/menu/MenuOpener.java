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

import com.google.common.base.Preconditions;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import appeng.core.AELog;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuHostLocator;

/**
 * Forwards a menu open request to the {@link MenuTypeBuilder} that was used to build a {@link MenuType}.
 */
public final class MenuOpener {

    private MenuOpener() {
    }

    private static final Map<MenuType<? extends AEBaseMenu>, Opener> registry = new HashMap<>();

    public static <T extends AEBaseMenu> void addOpener(MenuType<T> type, Opener opener) {
        registry.put(type, opener);
    }

    public static boolean returnTo(MenuType<?> type, Player player, MenuHostLocator locator) {
        return open(type, player, locator, true);
    }

    public static boolean open(MenuType<?> type, Player player, MenuHostLocator locator) {
        return open(type, player, locator, false);
    }

    public static boolean open(MenuType<?> type, Player player, MenuHostLocator locator, boolean fromSubMenu) {
        Preconditions.checkArgument(!player.level().isClientSide(), "Menus must be opened on the server.");
        Opener opener = registry.get(type);
        if (opener == null) {
            AELog.warn("Trying to open menu for unknown menu type {}", type);
            return false;
        }

        return opener.open(player, locator, fromSubMenu);
    }

    @FunctionalInterface
    public interface Opener {

        boolean open(Player player, MenuHostLocator locator, boolean fromSubMenu);

    }

}
