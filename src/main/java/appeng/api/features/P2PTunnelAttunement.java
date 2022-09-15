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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.concurrent.ThreadSafe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.capabilities.Capability;

import appeng.core.definitions.AEParts;
import appeng.items.parts.PartItem;
import appeng.parts.p2p.P2PTunnelPart;

/**
 * A Registry for how p2p Tunnels are attuned
 */
@ThreadSafe
public final class P2PTunnelAttunement {
    private static final int INITIAL_CAPACITY = 40;

    static final Map<TagKey<Item>, Item> tagTunnels = new IdentityHashMap<>(INITIAL_CAPACITY);
    static final List<ApiAttunement> apiAttunements = new ArrayList<>(INITIAL_CAPACITY);

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
        var itemKey = BuiltInRegistries.ITEM.getKey(tunnel.asItem());
        if (itemKey.equals(BuiltInRegistries.ITEM.getDefaultKey())) {
            throw new IllegalArgumentException("Tunnel item must be registered first.");
        }
        return TagKey.create(Registries.ITEM,
                new ResourceLocation(itemKey.getNamespace(), "p2p_attunements/" + itemKey.getPath()));
    }

    /**
     * Attunement based on the standard item tag: {@code <tunnel item namespace>:p2p_attunements/<tunnel item path>}
     */
    public synchronized static void registerAttunementTag(ItemLike tunnel) {
        Objects.requireNonNull(tunnel.asItem(), "tunnel.asItem()");
        tagTunnels.put(getAttunementTag(tunnel), validateTunnelPartItem(tunnel));
    }

    /**
     * Attunement based on the ability of getting a capability from the item.
     * 
     * @param tunnelPart  The P2P-tunnel part item.
     * @param description Description for display in REI/JEI.
     */
    public synchronized static <T> void registerAttunementApi(ItemLike tunnelPart, Capability<?> cap,
            Component description) {
        Objects.requireNonNull(cap, "cap");
        apiAttunements.add(new ApiAttunement(cap, validateTunnelPartItem(tunnelPart), description));
    }

    /**
     * @param trigger attunement trigger
     * @return The part item for a P2P-Tunnel that should handle the given attunement, or an empty item stack.
     */
    public synchronized static ItemStack getTunnelPartByTriggerItem(ItemStack trigger) {
        if (trigger.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Tags first
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

    record ApiAttunement(
            Capability<?> capability,
            Item tunnelType,
            Component component) {
        public boolean hasApi(ItemStack stack) {
            return stack.getCapability(capability).isPresent();
        }
    }
}
