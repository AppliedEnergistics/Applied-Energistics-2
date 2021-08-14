/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 - 2015 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.features;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

import appeng.api.config.TunnelType;

/**
 * A Registry for how p2p Tunnels are attuned
 */
public final class P2PTunnelAttunementInternal {
    private static final int INITIAL_CAPACITY = 40;

    private static final Map<ItemStack, TunnelType> tunnels = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, TunnelType> modIdTunnels = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<Capability<?>, TunnelType> capTunnels = new HashMap<>(INITIAL_CAPACITY);

    public static void init() {
        P2PTunnelAttunement.addNewItemAttunement = P2PTunnelAttunementInternal::addNewItemAttunement;
        P2PTunnelAttunement.addNewModAttunement = P2PTunnelAttunementInternal::addNewModAttunement;
        P2PTunnelAttunement.addNewCapAttunement = P2PTunnelAttunementInternal::addNewCapAttunement;
        P2PTunnelAttunement.getTunnelTypeByItem = P2PTunnelAttunementInternal::getTunnelTypeByItem;
    }

    private static void addNewItemAttunement(@Nonnull ItemStack trigger, @Nullable TunnelType type) {
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(type, "type");
        Preconditions.checkArgument(!trigger.isEmpty(), "!trigger.isEmpty()");
        tunnels.put(trigger, type);
    }

    private static void addNewModAttunement(@Nonnull final String modId, @Nullable final TunnelType type) {
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(type, "type");
        modIdTunnels.put(modId, type);
    }

    private static void addNewCapAttunement(@Nonnull final Capability<?> cap, @Nullable final TunnelType type) {
        Objects.requireNonNull(cap, "cap");
        Objects.requireNonNull(type, "type");
        capTunnels.put(cap, type);
    }

    @Nullable
    private static TunnelType getTunnelTypeByItem(final ItemStack trigger) {
        if (!trigger.isEmpty()) {
            // First match exact items
            for (final Map.Entry<ItemStack, TunnelType> entry : tunnels.entrySet()) {
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
                for (Map.Entry<Capability<?>, TunnelType> entry : capTunnels.entrySet()) {
                    if (trigger.getCapability(entry.getKey(), face).isPresent()) {
                        return entry.getValue();
                    }
                }
            }

            // Use the mod id as last option.
            for (final Map.Entry<String, TunnelType> entry : modIdTunnels.entrySet()) {
                if (trigger.getItem().getRegistryName() != null
                        && trigger.getItem().getRegistryName().getNamespace().equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }
}
