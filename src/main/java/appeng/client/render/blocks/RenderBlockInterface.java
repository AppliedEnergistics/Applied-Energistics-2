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


import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import appeng.block.misc.BlockInterface;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.IRenderHelper;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.tile.misc.TileInterface;


public class RenderBlockInterface extends BaseBlockRender<BlockInterface, TileInterface>
{

	public RenderBlockInterface()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( BlockInterface block, IBlockAccess world, BlockPos pos, IRenderHelper renderer )
	{
		TileInterface ti = block.getTileEntity( world, pos );
		BlockRenderInfo info = block.getRendererInstance();

		if( ti != null && ti.getForward() != null )
		{
			IAESprite side = ExtraBlockTextures.BlockInterfaceAlternateArrow.getIcon();
			info.setTemporaryRenderIcons( ExtraBlockTextures.BlockInterfaceAlternate.getIcon(), renderer.getIcon( world.getBlockState( pos ) )[0], side, side, side, side );
		}

		boolean fz = super.renderInWorld( block, world, pos, renderer );

		info.setTemporaryRenderIcon( null );

		return fz;
	}
}
