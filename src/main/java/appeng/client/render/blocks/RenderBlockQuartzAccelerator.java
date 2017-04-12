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


import appeng.block.misc.BlockQuartzGrowthAccelerator;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;


public class RenderBlockQuartzAccelerator extends BaseBlockRender<BlockQuartzGrowthAccelerator, TileQuartzGrowthAccelerator>
{

	public RenderBlockQuartzAccelerator()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( final BlockQuartzGrowthAccelerator blk, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final TileEntity te = world.getTileEntity( x, y, z );

		if( te instanceof TileQuartzGrowthAccelerator )
		{
			final TileQuartzGrowthAccelerator tileCGA = (TileQuartzGrowthAccelerator) te;

			if( tileCGA.isPowered() )
			{
				final IIcon top_Bottom = ExtraBlockTextures.BlockQuartzGrowthAcceleratorOn.getIcon();
				final IIcon side = ExtraBlockTextures.BlockQuartzGrowthAcceleratorSideOn.getIcon();

				blk.getRendererInstance().setTemporaryRenderIcons( top_Bottom, top_Bottom, side, side, side, side );
			}
		}

		final boolean out = super.renderInWorld( blk, world, x, y, z, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
