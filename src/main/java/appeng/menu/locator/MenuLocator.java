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

package appeng.menu.locator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;

/**
 * Describes how a menu the player has opened was originally located. This can be one of four ways, see the {@code Type}
 * enum below.
 * <p>
 * Both the client and the server need a host to open a menu, but we open menus from the server. This class takes care
 * of serializing the original "location" of the menu so that the client can also have access to it.
 */
public interface MenuLocator {
    /**
     * Locates the menu host in the world and returns it if it satisfies the expected menu host interface.
     */
    @Nullable
    <T> T locate(Player player, Class<T> hostInterface);
}
