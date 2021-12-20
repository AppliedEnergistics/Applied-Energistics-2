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

package appeng.api.parts;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

//@formatter:off

/**
 * When implementing a custom part, you must create an item to both represent the part in NBT and Packet data, and to
 * actually place the part onto the bus. Implement this interface on your part {@link net.minecraft.world.item.Item}.
 * <p/>
 * To help with placing parts onto buses, use
 * {@link PartHelper#usePartItem(ItemStack, BlockPos, Direction, Player, InteractionHand, Level)} to implement your
 * items {@link net.minecraft.world.item.Item#useOn(UseOnContext)} method.
 */
public interface IPartItem<P extends IPart> extends ItemLike {
    /**
     * @return The class of the parts that will be created by this part item.
     */
    Class<P> getPartClass();

    /**
     * create a new part INSTANCE
     *
     * @return part from item
     */
    P createPart();

    /**
     * @return The registry id for this item.
     */
    static ResourceLocation getId(IPartItem<?> item) {
        var id = Registry.ITEM.getKey(item.asItem());
        if (id == Registry.ITEM.getDefaultKey()) {
            throw new IllegalStateException("Part item " + item + " is not registered");
        }
        return id;
    }

    /**
     * @return The registry id for this item, suitable for network serialization.
     */
    static int getNetworkId(IPartItem<?> item) {
        var id = Registry.ITEM.getId(item.asItem());
        if (id == 0) {
            throw new IllegalStateException("Part item " + item + " is not registered");
        }
        return id;
    }

    /**
     * Retrieve a part item by its {@link #getId(IPartItem) id}.
     */
    @Nullable
    static IPartItem<?> byId(ResourceLocation id) {
        var item = Registry.ITEM.get(id);
        if (item instanceof IPartItem<?>partItem) {
            return partItem;
        }
        return null;
    }

    /**
     * Retrieve a part item by its {@link #getNetworkId(IPartItem) id}.
     */
    @Nullable
    static IPartItem<?> byNetworkId(int id) {
        var item = Registry.ITEM.byId(id);
        if (item instanceof IPartItem<?>partItem) {
            return partItem;
        }
        return null;
    }
}
