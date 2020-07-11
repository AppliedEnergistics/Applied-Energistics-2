/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 TeamAppliedEnergistics
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

package appeng.api.client;

import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/**
 * A registry for 3D models used to render storage cells in the world, when they are 
 * inserted into a drive or similar machines.
 */
public interface ICellModelRegistry {

    /**
     * Register a new model for a storage cell item.
     * 
     * Model loading is not part of it at all. The only use case it provides is to
     * map an {@link Item} to a {@link ResourceLocation} which can be looked up from
     * the {@link ModelBakery}. No validation about missing models will be done.
     * 
     * For examples look at our cell part models within the drive model directory.
     * 
     * @param item  The cell item
     * @param model The {@link ResourceLocation} representing the model.
     * @return
     */
    boolean registerModel(@Nonnull Item item, @Nonnull ResourceLocation model);

    /**
     * The model represented as {@link ResourceLocation} for an {@link Item}
     * 
     * @param item
     * @return
     */
    @Nonnull
    ResourceLocation model(@Nonnull Item item);

    /**
     * An unmodifiable map of all registered mappings.
     * 
     * @return
     */
    @Nonnull
    Map<Item, ResourceLocation> models();

}
