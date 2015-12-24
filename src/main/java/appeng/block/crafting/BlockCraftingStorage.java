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


import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingStorageTile;


public class BlockCraftingStorage extends BlockCraftingUnit
{

	public BlockCraftingStorage( final CraftingUnitType type )
	{
		super( type );
		this.setTileEntity( TileCraftingStorageTile.class );
	}

	@Override
	public Class<ItemCraftingStorage> getItemBlockClass()
	{
		return ItemCraftingStorage.class;
	}

	@Override
	public appeng.client.texture.IAESprite getIcon( final net.minecraft.util.EnumFacing side, final net.minecraft.block.state.IBlockState state )
	{
		final boolean formed = (boolean) state.getValue( FORMED );
		switch( this.type )
		{
			default:
			case STORAGE_1K:
				return formed ?
						super.getIcon( side, state ) :
						ExtraBlockTextures.BlockCraftingStorage1kFit.getIcon();
			case STORAGE_4K:
				return formed ?
						ExtraBlockTextures.BlockCraftingStorage4k.getIcon() :
						ExtraBlockTextures.BlockCraftingStorage1kFit.getIcon();
			case STORAGE_16K:
				return formed ?
						ExtraBlockTextures.BlockCraftingStorage16k.getIcon() :
						ExtraBlockTextures.BlockCraftingStorage16kFit.getIcon();
			case STORAGE_64K:
				return formed ?
						ExtraBlockTextures.BlockCraftingStorage64k.getIcon() :
						ExtraBlockTextures.BlockCraftingStorage64kFit.getIcon();

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
