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


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.block.misc.BlockQuartzGrowthAccelerator;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.tile.misc.TileQuartzGrowthAccelerator;


public class RenderBlockQuartzAccelerator extends BaseBlockRender<BlockQuartzGrowthAccelerator, TileQuartzGrowthAccelerator>
{

	public RenderBlockQuartzAccelerator()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( final BlockQuartzGrowthAccelerator blk, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		final TileEntity te = world.getTileEntity( pos );
		if( te instanceof TileQuartzGrowthAccelerator )
		{
			if( ( (TileQuartzGrowthAccelerator) te ).isPowered() )
			{
				final IAESprite top_Bottom = ExtraBlockTextures.BlockQuartzGrowthAcceleratorOn.getIcon();
				final IAESprite side = ExtraBlockTextures.BlockQuartzGrowthAcceleratorSideOn.getIcon();
				blk.getRendererInstance().setTemporaryRenderIcons( top_Bottom, top_Bottom, side, side, side, side );
			}
		}

		final boolean out = super.renderInWorld( blk, world, pos, renderer );
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
