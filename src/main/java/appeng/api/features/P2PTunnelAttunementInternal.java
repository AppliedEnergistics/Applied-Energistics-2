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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Internal methods that complement {@link P2PTunnelAttunement} and which are not part of the public API.
 */
public final class P2PTunnelAttunementInternal {

    private P2PTunnelAttunementInternal() {
    }

    /**
     * Gets a report which sources of attunement exist for a given tunnel type.
     */
    public static AttunementInfo getAttunementInfo(ItemLike tunnelType) {
        var tunnelItem = tunnelType.asItem();

        Set<Capability<?>> caps = new HashSet<>();

        for (var entry : P2PTunnelAttunement.apiAttunements) {
            if (entry.tunnelType() == tunnelItem) {
                caps.add(entry.capability());
            }
        }

        return new AttunementInfo(caps);
    }

    public static List<Resultant> getApiTunnels() {
        return P2PTunnelAttunement.apiAttunements.stream()
                .map(info -> new Resultant(info.component(), info.tunnelType(), info::hasApi)).toList();
    }

    public static Map<TagKey<Item>, Item> getTagTunnels() {
        return P2PTunnelAttunement.tagTunnels;
    }

    public record AttunementInfo(Set<Capability<?>> apis) {
    }

    public record Resultant(Component description, Item tunnelType, Predicate<ItemStack> stackPredicate) {
    }
}
