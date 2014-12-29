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

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.misc.TileInterface;

public class RenderBlockInterface extends BaseBlockRender
{

	public RenderBlockInterface() {
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileInterface ti = block.getTileEntity( world, x, y, z );

		BlockRenderInfo info = block.getRendererInstance();

		if ( ti.getForward() != ForgeDirection.UNKNOWN )
		{
			IIcon side = ExtraBlockTextures.BlockInterfaceAlternateArrow.getIcon();
			info.setTemporaryRenderIcons( ExtraBlockTextures.BlockInterfaceAlternate.getIcon(), block.getIcon( 0, 0 ), side, side, side, side );
		}

		boolean fz = super.renderInWorld( block, world, x, y, z, renderer );

		info.setTemporaryRenderIcon( null );

		return fz;
	}
}
