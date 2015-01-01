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

package appeng.tile.crafting;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;

public class TileCraftingStorageTile extends TileCraftingTile
{

	static final ItemStack STACK_4K_STORAGE = AEApi.instance().blocks().blockCraftingStorage4k.stack( 1 );
	static final ItemStack STACK_16K_STORAGE = AEApi.instance().blocks().blockCraftingStorage16k.stack( 1 );
	static final ItemStack STACK_64K_STORAGE = AEApi.instance().blocks().blockCraftingStorage64k.stack( 1 );

	@Override
	protected ItemStack getItemFromTile(Object obj)
	{
		int storage = ((TileCraftingTile) obj).getStorageBytes() / 1024;

		if ( storage == 4 )
			return STACK_4K_STORAGE;
		if ( storage == 16 )
			return STACK_16K_STORAGE;
		if ( storage == 64 )
			return STACK_64K_STORAGE;

		return super.getItemFromTile( obj );
	}

	@Override
	public boolean isAccelerator()
	{
		return false;
	}

	@Override
	public boolean isStorage()
	{
		return true;
	}

	@Override
	public int getStorageBytes()
	{
		if ( this.worldObj == null || this.notLoaded() )
			return 0;

		switch (this.worldObj.getBlockMetadata( this.xCoord, this.yCoord, this.zCoord ) & 3)
		{
		default:
		case 0:
			return 1024;
		case 1:
			return 4 * 1024;
		case 2:
			return 16 * 1024;
		case 3:
			return 64 * 1024;
		}
	}
}
