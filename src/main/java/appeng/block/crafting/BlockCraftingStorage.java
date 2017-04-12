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

package appeng.block.crafting;


import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingStorageTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import java.util.List;


public class BlockCraftingStorage extends BlockCraftingUnit
{
	public BlockCraftingStorage()
	{
		this.setTileEntity( TileCraftingStorageTile.class );
	}

	@Override
	public Class<ItemCraftingStorage> getItemBlockClass()
	{
		return ItemCraftingStorage.class;
	}

	@Override
	public IIcon getIcon( final int direction, final int metadata )
	{
		switch( metadata & ( ~4 ) )
		{
			default:

			case 0:
				return super.getIcon( 0, 0 );
			case 1:
				return ExtraBlockTextures.BlockCraftingStorage4k.getIcon();
			case 2:
				return ExtraBlockTextures.BlockCraftingStorage16k.getIcon();
			case 3:
				return ExtraBlockTextures.BlockCraftingStorage64k.getIcon();

			case FLAG_FORMED:
				return ExtraBlockTextures.BlockCraftingStorage1kFit.getIcon();
			case 1 | FLAG_FORMED:
				return ExtraBlockTextures.BlockCraftingStorage4kFit.getIcon();
			case 2 | FLAG_FORMED:
				return ExtraBlockTextures.BlockCraftingStorage16kFit.getIcon();
			case 3 | FLAG_FORMED:
				return ExtraBlockTextures.BlockCraftingStorage64kFit.getIcon();
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
		itemStacks.add( new ItemStack( this, 1, 1 ) );
		itemStacks.add( new ItemStack( this, 1, 2 ) );
		itemStacks.add( new ItemStack( this, 1, 3 ) );
	}

	@Override
	public String getUnlocalizedName( final ItemStack is )
	{
		if( is.getItemDamage() == 1 )
		{
			return "tile.appliedenergistics2.BlockCraftingStorage4k";
		}

		if( is.getItemDamage() == 2 )
		{
			return "tile.appliedenergistics2.BlockCraftingStorage16k";
		}

		if( is.getItemDamage() == 3 )
		{
			return "tile.appliedenergistics2.BlockCraftingStorage64k";
		}

		return this.getItemUnlocalizedName( is );
	}
}
