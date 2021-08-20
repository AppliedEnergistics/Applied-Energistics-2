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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.config.TunnelType;

/**
 * A Registry for how p2p Tunnels are attuned
 */
@ThreadSafe
public final class P2PTunnelAttunement {
    private static final int INITIAL_CAPACITY = 40;

    private static final Map<ItemStack, TunnelType> tunnels = new HashMap<>(INITIAL_CAPACITY);
    private static final Map<String, TunnelType> modIdTunnels = new HashMap<>(INITIAL_CAPACITY);
    private static final List<ApiAttunement<?>> apiAttunements = new ArrayList<>();

    private P2PTunnelAttunement() {
    }

    /**
     * Allows third parties to register items from their mod as potential attunements for AE's P2P Tunnels
     *
     * @param trigger - the item which triggers attunement
     * @param type    - the type of tunnel
     */
    public synchronized static void addNewAttunement(@Nonnull ItemStack trigger, @Nonnull TunnelType type) {
        Objects.requireNonNull(trigger, "trigger");
        Objects.requireNonNull(type, "type");
        Preconditions.checkArgument(!trigger.isEmpty(), "!trigger.isEmpty()");
        tunnels.put(trigger, type);
    }

    public synchronized static void addNewAttunement(@Nonnull ItemLike trigger, @Nonnull TunnelType type) {
        Objects.requireNonNull(trigger, "trigger");
        addNewAttunement(new ItemStack(trigger), type);
    }

    public synchronized static void addNewAttunement(@Nonnull String modId, @Nonnull TunnelType type) {
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(type, "type");
        modIdTunnels.put(modId, type);
    }

    /**
     * Attunement based on the ability of getting an API via Fabric API Lookup from the item.
     */
    public synchronized static <T> void addNewAttunement(ItemApiLookup<?, T> api,
            Function<ItemStack, T> contextProvider,
            TunnelType type) {
        Objects.requireNonNull(api, "api");
        Objects.requireNonNull(contextProvider, "contextProvider");
        Objects.requireNonNull(type, "type");
        apiAttunements.add(new ApiAttunement<>(api, contextProvider, type));
    }

    /**
     * Attunement based on the ability of getting a storage container API via Fabric API Lookup from the item.
     */
    public synchronized static void addNewAttunement(@Nonnull ItemApiLookup<?, ContainerItemContext> api,
            @Nonnull TunnelType type) {
        addNewAttunement(api, stack -> ContainerItemContext.ofSingleSlot(new SingleStackStorage() {
            ItemStack buffer = stack;

            @Override
            protected ItemStack getStack() {
                return buffer;
            }

            @Override
            protected void setStack(ItemStack stack) {
                buffer = stack;
            }
        }), type);
    }

    /**
     * returns null if no attunement can be found.
     *
     * @param trigger attunement trigger
     * @return null if no attunement can be found or attunement
     */
    @Nullable
    public synchronized static TunnelType getTunnelTypeByItem(ItemStack trigger) {
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

            // Check provided APIs
            for (var apiAttunement : apiAttunements) {
                if (apiAttunement.hasApi(trigger)) {
                    return apiAttunement.type();
                }
            }

            // Use the mod id as last option.
            for (final Map.Entry<String, TunnelType> entry : modIdTunnels.entrySet()) {
                var id = Registry.ITEM.getKey(trigger.getItem());
                if (id.getNamespace().equals(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    private record ApiAttunement<T> (
            ItemApiLookup<?, T> api,
            Function<ItemStack, T> contextProvider,
            TunnelType type) {
        public boolean hasApi(ItemStack stack) {
            return api.find(stack, contextProvider.apply(stack)) != null;
        }
    }

}
