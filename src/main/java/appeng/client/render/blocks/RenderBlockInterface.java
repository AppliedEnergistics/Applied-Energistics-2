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


import appeng.block.misc.BlockInterface;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.misc.TileInterface;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;


public class RenderBlockInterface extends BaseBlockRender<BlockInterface, TileInterface>
{

	public RenderBlockInterface()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( final BlockInterface block, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final TileInterface ti = block.getTileEntity( world, x, y, z );
		final BlockRenderInfo info = block.getRendererInstance();

		if( ti != null && ti.getForward() != ForgeDirection.UNKNOWN )
		{
			final IIcon side = ExtraBlockTextures.BlockInterfaceAlternateArrow.getIcon();
			info.setTemporaryRenderIcons( ExtraBlockTextures.BlockInterfaceAlternate.getIcon(), block.getIcon( 0, 0 ), side, side, side, side );
		}

		final boolean fz = super.renderInWorld( block, world, x, y, z, renderer );

		info.setTemporaryRenderIcon( null );

		return fz;
	}
}
