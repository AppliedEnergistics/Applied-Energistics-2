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


import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;


public interface IStorageHelper
{

	/**
	 * Register a new storage channel.
	 * 
	 * AE2 already provides native channels for {@link IAEItemStack} and {@link IAEFluidStack}.
	 * 
	 * Each {@link IAEStack} subtype can only have a single factory instance. Overwriting is not intended.
	 * Each subtype should be a direct one, this might be enforced at any time.
	 * 
	 * Channel class and factory instance can be used interchangeable as identifier. In most cases the factory instance
	 * is used as key as having direct access the methods is more beneficial compared to being forced to query the
	 * registry each time.
	 * 
	 * Caching the factory instance in a field or local variable is perfectly for performance reasons. But do not use
	 * any AE2 internal field as they can change randomly between releases.
	 * 
	 * @param channel The channel type, must be a subtype of {@link IStorageChannel}
	 * @param factory An instance implementing the channel, must be be an instance of channel
	 */
	@Nonnull
	<T extends IAEStack<T>, C extends IStorageChannel<T>> void registerStorageChannel( @Nonnull Class<C> channel, @Nonnull C factory );

	/**
	 * Fetch the factory instance for a specific storage channel.
	 * 
	 * Channel must be a direct subtype of {@link IStorageChannel}.
	 * 
	 * @throws NullPointerException when fetching an unregistered channel.
	 * @param channel The channel type
	 * @return the factory instance
	 */
	@Nonnull
	<T extends IAEStack<T>, C extends IStorageChannel<T>> C getStorageChannel( @Nonnull Class<C> channel );

	/**
	 * An unmodifiable collection of all registered factory instance.
	 * 
	 * This is mainly used as helper to let storage grids construct their internal storage for each type.
	 */
	@Nonnull
	Collection<IStorageChannel<? extends IAEStack<?>>> storageChannels();

	/**
	 * load a crafting link from nbt data.
	 *
	 * @param data to be loaded data
	 *
	 * @return crafting link
	 */
	ICraftingLink loadCraftingLink( NBTTagCompound data, ICraftingRequester req );

	/**
	 * Extracts items from a {@link IMEInventory} respecting power requirements.
	 * 
	 * @param energy Energy source.
	 * @param inv Inventory to extract from.
	 * @param request Requested item and count.
	 * @param src Action source.
	 * @param mode Simulate or modulate
	 * @return extracted items or {@code null} of nothing was extracted.
	 */
	<T extends IAEStack<T>> T poweredExtraction( final IEnergySource energy, final IMEInventory<T> inv, final T request, final IActionSource src, final Actionable mode );

	/**
	 * Inserts items into a {@link IMEInventory} respecting power requirements.
	 * 
	 * @param energy Energy source.
	 * @param inv Inventory to insert into.
	 * @param request Items to insert.
	 * @param src Action source.
	 * @param mode Simulate or modulate
	 * @return items not inserted or {@code null} if everything was inserted.
	 */
	<T extends IAEStack<T>> T poweredInsert( final IEnergySource energy, final IMEInventory<T> inv, final T input, final IActionSource src, final Actionable mode );

	/**
	 * Posts alteration of stored items to the provided {@link IStorageGrid}.
	 * This can be used by cell containers to notify the grid of storage cell changes.
	 * 
	 * @param gs the storage grid.
	 * @param removedCell the removed cell itemstack
	 * @param addedCell the added cell itemstack
	 * @param src the action source
	 */
	void postChanges( @Nonnull final IStorageGrid gs, @Nonnull final ItemStack removedCell, @Nonnull final ItemStack addedCell, @Nonnull final IActionSource src );
}
