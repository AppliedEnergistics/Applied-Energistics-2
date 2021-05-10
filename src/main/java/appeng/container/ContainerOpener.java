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

package appeng.container;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;

import appeng.core.AELog;

/**
 * Allows opening containers generically.
 */
public final class ContainerOpener {

    private ContainerOpener() {
    }

    private static final Map<ContainerType<? extends AEBaseContainer>, Opener> registry = new HashMap<>();

    public static <T extends AEBaseContainer> void addOpener(ContainerType<T> type, Opener opener) {
        registry.put(type, opener);
    }

    public static boolean openContainer(ContainerType<?> type, PlayerEntity player, ContainerLocator locator) {
        Opener opener = registry.get(type);
        if (opener == null) {
            AELog.warn("Trying to open container for unknown container type {}", type);
            return false;
        }

        return opener.open(player, locator);
    }

    @FunctionalInterface
    public interface Opener {

        boolean open(PlayerEntity player, ContainerLocator locator);

    }

}
