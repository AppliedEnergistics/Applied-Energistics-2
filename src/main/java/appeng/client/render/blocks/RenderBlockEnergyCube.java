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

package appeng.client.render.blocks;


import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.block.networking.BlockEnergyCell;
import appeng.client.render.BaseBlockRender;
import appeng.tile.networking.TileEnergyCell;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;


public class RenderBlockEnergyCube extends BaseBlockRender<BlockEnergyCell, TileEnergyCell>
{

	public RenderBlockEnergyCube()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( final BlockEnergyCell blk, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		final IAEItemPowerStorage myItem = (IAEItemPowerStorage) is.getItem();
		final double internalCurrentPower = myItem.getAECurrentPower( is );
		final double internalMaxPower = myItem.getAEMaxPower( is );

		int meta = (int) ( 8.0 * ( internalCurrentPower / internalMaxPower ) );

		if( meta > 7 )
		{
			meta = 7;
		}
		if( meta < 0 )
		{
			meta = 0;
		}

		renderer.setOverrideBlockTexture( blk.getIcon( 0, meta ) );
		super.renderInventory( blk, is, renderer, type, obj );
		renderer.setOverrideBlockTexture( null );
	}

	@Override
	public boolean renderInWorld( final BlockEnergyCell blk, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final int meta = world.getBlockMetadata( x, y, z );

		renderer.overrideBlockTexture = blk.getIcon( 0, meta );

		final boolean out = renderer.renderStandardBlock( blk, x, y, z );
		renderer.overrideBlockTexture = null;

		return out;
	}
}
