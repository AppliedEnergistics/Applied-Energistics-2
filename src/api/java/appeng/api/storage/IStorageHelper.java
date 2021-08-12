/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

package appeng.api.storage;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.data.IAEStack;

public interface IStorageHelper {

    /**
     * load a crafting link from nbt data.
     *
     * @param data to be loaded data
     * @return crafting link
     */
    ICraftingLink loadCraftingLink(CompoundTag data, ICraftingRequester req);

    /**
     * Extracts items from a {@link IMEInventory} respecting power requirements.
     *
     * @param energy  Energy source.
     * @param inv     Inventory to extract from.
     * @param request Requested item and count.
     * @param src     Action source.
     * @param mode    Simulate or modulate
     * @return extracted items or {@code null} of nothing was extracted.
     */
    <T extends IAEStack<T>> T poweredExtraction(final IEnergySource energy, final IMEInventory<T> inv, final T request,
            final IActionSource src, final Actionable mode);

    /**
     * Inserts items into a {@link IMEInventory} respecting power requirements.
     *
     * @param energy Energy source.
     * @param inv    Inventory to insert into.
     * @param input  Items to insert.
     * @param src    Action source.
     * @param mode   Simulate or modulate
     * @return items not inserted or {@code null} if everything was inserted.
     */
    <T extends IAEStack<T>> T poweredInsert(final IEnergySource energy, final IMEInventory<T> inv, final T input,
            final IActionSource src, final Actionable mode);

    /**
     * A utility function to notify the {@link IStorageService} of grid inventory changes that result from storage cells
     * being removed or added to the grid.
     *
     * @param gs          the storage service to notify
     * @param removedCell the removed cell. May be {@link ItemStack#EMPTY} if no cell was removed.
     * @param addedCell   the added cell. May be {@link ItemStack#EMPTY} if no cell was added.
     * @param src         the action source
     */
    void postChanges(@Nonnull final IStorageService gs, @Nonnull final ItemStack removedCell,
            @Nonnull final ItemStack addedCell, @Nonnull final IActionSource src);
}
