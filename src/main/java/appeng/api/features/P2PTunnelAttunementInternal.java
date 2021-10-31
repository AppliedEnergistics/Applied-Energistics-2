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

package appeng.api.features;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * Internal methods that complement {@link P2PTunnelAttunement} and which are not part of the public API.
 */
public final class P2PTunnelAttunementInternal {
    /**
     * Gets a report which sources of attunement exist for a given tunnel type.
     */
    public static AttunementInfo getAttunementInfo(ItemLike tunnelType) {
        var tunnelItem = tunnelType.asItem();

        Set<Item> items = new HashSet<>();
        Set<String> mods = new HashSet<>();
        Set<ItemApiLookup<?, ?>> apis = new HashSet<>();

        for (var entry : P2PTunnelAttunement.tunnels.entrySet()) {
            if (entry.getValue() == tunnelItem) {
                items.add(entry.getKey());
            }
        }

        for (var entry : P2PTunnelAttunement.modIdTunnels.entrySet()) {
            if (entry.getValue() == tunnelItem) {
                mods.add(entry.getKey());
            }
        }

        for (var apiAttunement : P2PTunnelAttunement.apiAttunements) {
            if (apiAttunement.tunnelType() == tunnelItem) {
                apis.add(apiAttunement.api());
            }
        }

        return new AttunementInfo(items, mods, apis);
    }

    public record AttunementInfo(Set<Item> items, Set<String> mods, Set<ItemApiLookup<?, ?>> apis) {
    }
}
