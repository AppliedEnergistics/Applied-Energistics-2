/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.helpers;


import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.integration.modules.LogisticsPipes;
import appeng.util.item.AEItemStack;


public final class LPInventory implements IMEInventory<IAEItemStack>
{

	private final TileEntity pipe;

	public LPInventory( final TileEntity te )
	{
		this.pipe = te;
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable type, final BaseActionSource src )
	{
		return input;
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final BaseActionSource src )
	{
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( final IItemList<IAEItemStack> out )
	{
		for( final ItemStack is : LogisticsPipes.instance.getProvidedItems( this.pipe ) )
		{
			for( final IAEItemStack l : out )
			{
				if( l.equals( is ) )
				{
					l.incCountRequestable( is.stackSize );
					break;
				}
			}
			final AEItemStack x = AEItemStack.create( is );
			x.setStackSize( 0 );
			x.setCountRequestable( is.stackSize );
			out.add( x );
		}

		for( final ItemStack is : LogisticsPipes.instance.getCraftedItems( this.pipe ) )
		{
			for( final IAEItemStack l : out )
			{
				if( l.equals( is ) )
				{
					l.setCraftable( true );
					break;
				}
			}

			final AEItemStack x = AEItemStack.create( is );
			x.setStackSize( 0 );
			x.setCraftable( true );
			out.add( x );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

}
