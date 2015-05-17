/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.api;


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.CraftingLink;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;


public final class ApiStorage implements IStorageHelper
{

	@Override
	public final ICraftingLink loadCraftingLink( NBTTagCompound data, ICraftingRequester req )
	{
		return new CraftingLink( data, req );
	}

	@Override
	public final IAEItemStack createItemStack( ItemStack is )
	{
		return AEItemStack.create( is );
	}

	@Override
	public final IAEFluidStack createFluidStack( FluidStack is )
	{
		return AEFluidStack.create( is );
	}

	@Override
	public final IItemList<IAEItemStack> createItemList()
	{
		return new ItemList<IAEItemStack>( IAEItemStack.class );
	}

	@Override
	public final IItemList<IAEFluidStack> createFluidList()
	{
		return new ItemList<IAEFluidStack>( IAEFluidStack.class );
	}

	@Override
	public final IAEItemStack readItemFromPacket( ByteBuf input ) throws IOException
	{
		return AEItemStack.loadItemStackFromPacket( input );
	}

	@Override
	public final IAEFluidStack readFluidFromPacket( ByteBuf input ) throws IOException
	{
		return AEFluidStack.loadFluidStackFromPacket( input );
	}

	@Override
	public final IAEItemStack poweredExtraction( IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack request, BaseActionSource src )
	{
		return Platform.poweredExtraction( energy, cell, request, src );
	}

	@Override
	public final IAEItemStack poweredInsert( IEnergySource energy, IMEInventory<IAEItemStack> cell, IAEItemStack input, BaseActionSource src )
	{
		return Platform.poweredInsert( energy, cell, input, src );
	}
}
