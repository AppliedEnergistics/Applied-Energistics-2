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


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;


public interface IStorageHelper
{

	/**
	 * load a crafting link from nbt data.
	 *
	 * @param data to be loaded data
	 *
	 * @return crafting link
	 */
	ICraftingLink loadCraftingLink( NBTTagCompound data, ICraftingRequester req );

	/**
	 * @param is An ItemStack
	 *
	 * @return a new INSTANCE of {@link IAEItemStack} from a MC {@link ItemStack}
	 */
	IAEItemStack createItemStack( ItemStack is );

	/**
	 * @param is A FluidStack
	 *
	 * @return a new INSTANCE of {@link IAEFluidStack} from a Forge {@link FluidStack}
	 */
	IAEFluidStack createFluidStack( FluidStack is );

	/**
	 * @return a new INSTANCE of {@link IItemList} for items
	 */
	IItemList<IAEItemStack> createItemList();

	/**
	 * @return a new INSTANCE of {@link IItemList} for fluids
	 */
	IItemList<IAEFluidStack> createFluidList();

	/**
	 * Read a AE Item Stack from a byte stream, returns a AE item stack or null.
	 *
	 * @param input to be loaded data
	 *
	 * @return item based of data
	 *
	 * @throws IOException if file could not be read
	 */
	IAEItemStack readItemFromPacket( ByteBuf input ) throws IOException;

	/**
	 * Read a AE Fluid Stack from a byte stream, returns a AE fluid stack or null.
	 *
	 * @param input to be loaded data
	 *
	 * @return fluid based on data
	 *
	 * @throws IOException if file could not be written
	 */
	IAEFluidStack readFluidFromPacket( ByteBuf input ) throws IOException;

	/**
	 * use energy from energy, to remove request items from cell, at the request of src.
	 *
	 * @param energy  to be drained energy source
	 * @param cell    cell of requested items
	 * @param request requested items
	 * @param src     action source
	 *
	 * @return items that successfully extracted.
	 */
	IAEItemStack poweredExtraction( IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack request, BaseActionSource src );

	/**
	 * use energy from energy, to inject input items into cell, at the request of src
	 *
	 * @param energy to be added energy source
	 * @param cell   injected cell
	 * @param input  to be injected items
	 * @param src    action source
	 *
	 * @return items that failed to insert.
	 */
	IAEItemStack poweredInsert( IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack input, BaseActionSource src );
}
