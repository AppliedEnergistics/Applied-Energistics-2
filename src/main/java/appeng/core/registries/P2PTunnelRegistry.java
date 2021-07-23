/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.registries;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;

import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;

public final class P2PTunnelRegistry implements IP2PTunnelRegistry {
    private static final int INITIAL_CAPACITY = 40;

    private final Map<net.minecraft.world.item.ItemStack, TunnelType> tunnels = new HashMap<>(INITIAL_CAPACITY);
    private final Map<String, TunnelType> modIdTunnels = new HashMap<>(INITIAL_CAPACITY);
    private final Map<Capability<?>, TunnelType> capTunnels = new HashMap<>(INITIAL_CAPACITY);

    @Override
    public void addNewAttunement(@Nonnull final String modId, @Nullable final TunnelType type) {
        if (type == null || modId == null) {
            return;
        }
        this.modIdTunnels.put(modId, type);
    }

    @Override
    public void addNewAttunement(@Nonnull final Capability<?> cap, @Nullable final TunnelType type) {
        if (type == null || cap == null) {
            return;
        }
        this.capTunnels.put(cap, type);
    }

    @Override
    public void addNewAttunement(@Nonnull final ItemStack trigger, @Nullable final TunnelType type) {
        if (type == null || trigger.isEmpty()) {
            return;
        }

        this.tunnels.put(trigger, type);
    }

    @Nullable
    @Override
    public TunnelType getTunnelTypeByItem(final ItemStack trigger) {
        if (!trigger.isEmpty()) {
            // First match exact items
            for (final Entry<ItemStack, TunnelType> entry : this.tunnels.entrySet()) {
                final ItemStack is = entry.getKey();

                if (is.getItem() == trigger.getItem()) {
                    return entry.getValue();
                }

                if (ItemStack.isSame(is, trigger)) {
                    return entry.getValue();
                }
            }

            // Next, check if the Item you're holding supports any registered capability
            for (Direction face : Direction.values()) {
                for (Entry<Capability<?>, TunnelType> entry : this.capTunnels.entrySet()) {
                    if (trigger.getCapability(entry.getKey(), face).isPresent()) {
                        return entry.getValue();
                    }
                }
            }

            // Use the mod id as last option.
            for (final Entry<String, TunnelType> entry : this.modIdTunnels.entrySet()) {
                if (trigger.getItem().getRegistryName() != null
                        && trigger.getItem().getRegistryName().getNamespace().equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

}
