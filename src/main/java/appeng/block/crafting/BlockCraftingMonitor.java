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


import appeng.api.AEApi;
import appeng.client.render.blocks.RenderBlockCraftingCPUMonitor;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingMonitorTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;


public class BlockCraftingMonitor extends BlockCraftingUnit
{
	public BlockCraftingMonitor()
	{
		this.setTileEntity( TileCraftingMonitorTile.class );
	}

	@Override
	@SideOnly( Side.CLIENT )
	protected RenderBlockCraftingCPUMonitor getRenderer()
	{
		return new RenderBlockCraftingCPUMonitor();
	}

	@Override
	public IIcon getIcon( final int direction, final int metadata )
	{
		if( direction != ForgeDirection.SOUTH.ordinal() )
		{
			for( final Block craftingUnitBlock : AEApi.instance().definitions().blocks().craftingUnit().maybeBlock().asSet() )
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
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
	}
}
