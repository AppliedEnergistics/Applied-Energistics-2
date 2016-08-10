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

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCraftingCPUMonitor;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingMonitorTile;


public class BlockCraftingMonitor extends BlockCraftingUnit
{
	public BlockCraftingMonitor()
	{
		this.setTileEntity( TileCraftingMonitorTile.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCraftingCPUMonitor.class;
	}

	@Override
	public IIcon getIcon( int direction, int metadata )
	{
		if( direction != ForgeDirection.SOUTH.ordinal() )
		{
			for( Block craftingUnitBlock : AEApi.instance().definitions().blocks().craftingUnit().maybeBlock().asSet() )
			{
				return craftingUnitBlock.getIcon( direction, metadata );
			}
		}

		switch( metadata )
		{
			default:
			case 0:
				return super.getIcon( 0, 0 );
			case FLAG_FORMED:
				return ExtraBlockTextures.BlockCraftingMonitorFit_Light.getIcon();
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( Item item, CreativeTabs tabs, List<ItemStack> itemStacks )
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
	}
}
