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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * A registry for 3D models used to render storage cells in the world, when they are inserted into a drive or similar
 * machines.
 */
@ThreadSafe
public final class StorageCellModels {

    private static final ResourceLocation MODEL_CELL_DEFAULT = new ResourceLocation(
            "ae2:block/drive/drive_cell");

    private static final Map<Item, ResourceLocation> registry = new IdentityHashMap<>();

    private StorageCellModels() {
    }

    /**
     * Register a new model for a storage cell item.
     * 
     * <p>
     * You are responsible for ensuring that the given model is actually loaded by the game. See
     * {@link net.minecraftforge.client.model.ModelLoader#addSpecialModel}.
     * 
     * This method only maps an {@link Item} to a {@link ResourceLocation} which can be looked up from the
     * {@link ModelBakery}. No validation about missing models will be done.
     * 
     * Will throw an exception in case a model is already registered for an item.
     * 
     * For examples look at our cell part models within the drive model directory.
     * 
     * @param itemLike The cell item
     * @param model    The {@link ResourceLocation} representing the model.
     */
    public synchronized static void registerModel(@Nonnull ItemLike itemLike, @Nonnull ResourceLocation model) {
        Objects.requireNonNull(itemLike, "itemLike");
        var item = Objects.requireNonNull(itemLike.asItem(), "item.asItem()");
        Objects.requireNonNull(model, "model");
        Preconditions.checkArgument(!registry.containsKey(item), "Cannot register an item twice.");

        registry.put(item, model);
    }

    /**
     * The {@link ResourceLocation} of the model used to render the given storage cell {@link Item} when inserted into a
     * drive or similar.
     * 
     * @param itemLike
     * @return null, if no model is registered.
     */
    @Nullable
    public synchronized static ResourceLocation model(@Nonnull ItemLike itemLike) {
        Objects.requireNonNull(itemLike, "itemLike");
        var item = Objects.requireNonNull(itemLike.asItem(), "itemLike.asItem()");

        return registry.get(item);
    }

    /**
     * A copy of all registered mappings.
     */
    @Nonnull
    public synchronized static Map<Item, ResourceLocation> models() {
        return new HashMap<>(registry);
    }

    /**
     * Returns the default model, which can be used when no explicit model is registered.
     */
    @Nonnull
    public static ResourceLocation getDefaultModel() {
        return MODEL_CELL_DEFAULT;
    }

}
