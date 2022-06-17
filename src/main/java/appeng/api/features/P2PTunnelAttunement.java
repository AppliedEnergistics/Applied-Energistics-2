/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import appeng.core.definitions.AEParts;
import appeng.items.parts.PartItem;
import appeng.parts.p2p.P2PTunnelPart;

/**
 * A Registry for how p2p Tunnels are attuned
 */
@ThreadSafe
public final class P2PTunnelAttunement {
    private static final int INITIAL_CAPACITY = 40;

    static final Map<Item, Item> tunnels = new HashMap<>(INITIAL_CAPACITY);
    static final Map<TagKey<Item>, Item> tagTunnels = new IdentityHashMap<>(INITIAL_CAPACITY);
    static final Map<String, Item> modIdTunnels = new HashMap<>(INITIAL_CAPACITY);
    static final List<ApiAttunement<?>> apiAttunements = new ArrayList<>(INITIAL_CAPACITY);

    /**
     * The default tunnel part for ME tunnels. Use this to register additional attunement options.
     */
    public static final ItemLike ME_TUNNEL = AEParts.ME_P2P_TUNNEL;

    /**
     * The default tunnel part for energy (i.e. Forge Energy) tunnels. Use this to register additional attunement
     * options.
     */
    public static final ItemLike ENERGY_TUNNEL = AEParts.FE_P2P_TUNNEL;

    /**
     * The default tunnel part for redstone tunnels. Use this to register additional attunement options.
     */
    public static final ItemLike REDSTONE_TUNNEL = AEParts.REDSTONE_P2P_TUNNEL;

    /**
     * The default tunnel part for fluid tunnels. Use this to register additional attunement options.
     */
    public static final ItemLike FLUID_TUNNEL = AEParts.FLUID_P2P_TUNNEL;

    /**
     * The default tunnel part for item tunnels. Use this to register additional attunement options.
     */
    public static final ItemLike ITEM_TUNNEL = AEParts.ITEM_P2P_TUNNEL;

    /**
     * The default tunnel part for light tunnels. Use this to register additional attunement options.
     */
    public static final ItemLike LIGHT_TUNNEL = AEParts.LIGHT_P2P_TUNNEL;

    private P2PTunnelAttunement() {
    }

    public static TagKey<Item> getAttunementTag(ItemLike tunnel) {
        Objects.requireNonNull(tunnel.asItem(), "tunnel.asItem()");
        var itemKey = Registry.ITEM.getKey(tunnel.asItem());
        if (itemKey.equals(Registry.ITEM.getDefaultKey())) {
            throw new IllegalArgumentException("Tunnel item must be registered first.");
        }
        return TagKey.create(Registry.ITEM_REGISTRY,
                new ResourceLocation(itemKey.getNamespace(), "p2p_attunements/" + itemKey.getPath()));
    }

    /**
     * Attunement based on the standard item tag: {@code <tunnel item namespace>:p2p_attunements/<tunnel item path>}
     */
    public synchronized static void registerAttunementTag(ItemLike tunnel) {
        Objects.requireNonNull(tunnel.asItem(), "tunnel.asItem()");
        addItemByTag(getAttunementTag(tunnel), tunnel);
    }

    /**
     * Attunement based on the ability of getting an API via Fabric API Lookup from the item.
     * 
     * @param tunnelPart  The P2P-tunnel part item.
     * @param description Description for display in REI/JEI.
     */
    public synchronized static <T> void registerAttunementApi(ItemLike tunnelPart, ItemApiLookup<?, T> api,
            Function<ItemStack, T> contextProvider, Component description) {
        Objects.requireNonNull(api, "api");
        Objects.requireNonNull(contextProvider, "contextProvider");
        apiAttunements.add(new ApiAttunement<>(api, contextProvider, validateTunnelPartItem(tunnelPart), description));
    }

    /**
     * Attunement based on the ability of getting a storage container API via Fabric API Lookup from the item.
     * 
     * @param tunnelPart  The P2P-tunnel part item.
     * @param description Description for display in REI/JEI.
     */
    public synchronized static void registerAttunementApi(ItemLike tunnelPart,
            ItemApiLookup<?, ContainerItemContext> api, Component description) {
        registerAttunementApi(tunnelPart, api, stack -> ContainerItemContext.ofSingleSlot(new SingleStackStorage() {
            ItemStack buffer = stack;

            @Override
            protected ItemStack getStack() {
                return buffer;
            }

            @Override
            protected void setStack(ItemStack stack) {
                buffer = stack;
            }
        }), description);
    }

    /**
     * Allows third parties to register items from their mod as potential attunements for AE's P2P Tunnels
     *
     * @param trigger    - the item which triggers attunement
     * @param tunnelPart The P2P-tunnel part item.
     * @deprecated Add your trigger to the {@linkplain #getAttunementTag standard tag} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.19")
    @Deprecated(forRemoval = true)
    public synchronized static void addItem(ItemLike trigger, ItemLike tunnelPart) {
        Objects.requireNonNull(trigger, "trigger");
        Item triggerItem = trigger.asItem();
        Objects.requireNonNull(triggerItem, "trigger.asItem()");
        Preconditions.checkArgument(triggerItem != Items.AIR, "trigger shouldn't be air!");
        tunnels.put(triggerItem, validateTunnelPartItem(tunnelPart));
    }

    /**
     * Allows third parties to register items in a tag as potential attunements for AE's P2P Tunnels
     *
     * @param trigger    The tag which triggers attunement
     * @param tunnelPart The P2P-tunnel part item.
     * @deprecated Add your trigger to the {@linkplain #getAttunementTag standard tag} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.19")
    @Deprecated(forRemoval = true)
    public synchronized static void addItemByTag(TagKey<Item> trigger, ItemLike tunnelPart) {
        Objects.requireNonNull(trigger, "trigger");
        tagTunnels.put(trigger, validateTunnelPartItem(tunnelPart));
    }

    /**
     * Adds all items from the given mod as attunement items for the given tunnel part.
     *
     * @param modId      The mod-id that triggers attunement into the given tunnel part.
     * @param tunnelPart The P2P-tunnel part item.
     * @deprecated Add relevant triggers to the {@linkplain #getAttunementTag} standard tag} instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.19")
    @Deprecated(forRemoval = true)
    public synchronized static void addItemByMod(String modId, ItemLike tunnelPart) {
        Objects.requireNonNull(modId, "modId");
        modIdTunnels.put(modId, validateTunnelPartItem(tunnelPart));
    }

    /**
     * @deprecated Use the overload with a description Component instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.19")
    @Deprecated(forRemoval = true)
    public synchronized static <T> void addItemByApi(ItemApiLookup<?, T> api,
            Function<ItemStack, T> contextProvider,
            ItemLike tunnelPart) {
        registerAttunementApi(tunnelPart, api, contextProvider, Component.literal("<missing description>"));
    }

    /**
     * @deprecated Use the overload with a description Component instead.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "1.19")
    @Deprecated(forRemoval = true)
    public synchronized static void addItemByApi(ItemApiLookup<?, ContainerItemContext> api,
            ItemLike tunnelPart) {
        registerAttunementApi(tunnelPart, api, Component.literal("<missing description>"));
    }

    /**
     * @param trigger attunement trigger
     * @return The part item for a P2P-Tunnel that should handle the given attunement, or an empty item stack.
     */
    public synchronized static ItemStack getTunnelPartByTriggerItem(ItemStack trigger) {
        if (trigger.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // First match exact items
        var tunnelItem = tunnels.get(trigger.getItem());
        if (tunnelItem != null) {
            return new ItemStack(tunnelItem);
        }

        // Tags second
        for (var tag : trigger.getTags().toList()) {
            var tagTunnelItem = tagTunnels.get(tag);
            if (tagTunnelItem != null) {
                return new ItemStack(tagTunnelItem);
            }
        }

        // Check provided APIs
        for (var apiAttunement : apiAttunements) {
            if (apiAttunement.hasApi(trigger)) {
                return new ItemStack(apiAttunement.tunnelType());
            }
        }

        // Use the mod id as last option.
        for (var entry : modIdTunnels.entrySet()) {
            var id = Registry.ITEM.getKey(trigger.getItem());
            if (id.getNamespace().equals(entry.getKey())) {
                return new ItemStack(entry.getValue());
            }
        }

        return ItemStack.EMPTY;
    }

    private static Item validateTunnelPartItem(ItemLike itemLike) {
        Objects.requireNonNull(itemLike, "item");
        var item = itemLike.asItem();
        Objects.requireNonNull(item, "item");
        if (!(item instanceof PartItem<?>partItem)) {
            throw new IllegalArgumentException("Given tunnel part item is not a part");
        }

        if (!P2PTunnelPart.class.isAssignableFrom((partItem.getPartClass()))) {
            throw new IllegalArgumentException("Given tunnel part item results in a part that is not a P2P tunnel: "
                    + partItem);
        }

        return item;
    }

    record ApiAttunement<T> (
            ItemApiLookup<?, T> api,
            Function<ItemStack, T> contextProvider,
            Item tunnelType,
            Component component) {
        public boolean hasApi(ItemStack stack) {
            return api.find(stack, contextProvider.apply(stack)) != null;
        }
    }
}
