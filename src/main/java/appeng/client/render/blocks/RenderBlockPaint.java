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


import appeng.block.misc.BlockPaint;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.helpers.Splotch;
import appeng.tile.misc.TilePaint;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class RenderBlockPaint extends BaseBlockRender<BlockPaint, TilePaint>
{

	public RenderBlockPaint()
	{
		super( false, 0 );
	}

	@Override
	public void renderInventory( final BlockPaint block, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{

	}

	@Override
	public boolean renderInWorld( final BlockPaint imb, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final TilePaint tp = imb.getTileEntity( world, x, y, z );
		boolean out = false;

		if( tp != null )
		{
			// super.renderInWorld( imb, world, x, y, z, renderer );

			final IIcon[] icoSet = { imb.getIcon( 0, 0 ), ExtraBlockTextures.BlockPaint2.getIcon(), ExtraBlockTextures.BlockPaint3.getIcon() };

			final Tessellator tess = Tessellator.instance;

			final int brightness = imb.getMixedBrightnessForBlock( world, x, y, z );

			final EnumSet<ForgeDirection> validSides = EnumSet.noneOf( ForgeDirection.class );

			for( final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
			{
				if( tp.isSideValid( side ) )
				{
					validSides.add( side );
				}
			}

			double offsetConstant = 0.001;
			final int lumen = 14 << 20 | 14 << 4;
			for( final Splotch s : tp.getDots() )
			{
				if( !validSides.contains( s.getSide() ) )
				{
					continue;
				}

				if( s.isLumen() )
				{
					tess.setColorOpaque_I( s.getColor().whiteVariant );
					tess.setBrightness( lumen );
				}
				else
				{
					tess.setColorOpaque_I( s.getColor().mediumVariant );
					tess.setBrightness( brightness );
				}

				double offset = offsetConstant;
				offsetConstant += 0.001;

				final double buffer = 0.1;

				double pos_x = s.x();
				double pos_y = s.y();

				pos_x = Math.max( buffer, Math.min( 1.0 - buffer, pos_x ) );
				pos_y = Math.max( buffer, Math.min( 1.0 - buffer, pos_y ) );

				if( s.getSide() == ForgeDirection.SOUTH || s.getSide() == ForgeDirection.NORTH )
				{
					pos_x += x;
					pos_y += y;
				}

				else if( s.getSide() == ForgeDirection.UP || s.getSide() == ForgeDirection.DOWN )
				{
					pos_x += x;
					pos_y += z;
				}

				else
				{
					pos_x += y;
					pos_y += z;
				}

				final IIcon ico = icoSet[s.getSeed() % icoSet.length];

				switch( s.getSide() )
				{
					case UP:
						offset = 1.0 - offset;
						tess.addVertexWithUV( pos_x - buffer, y + offset, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x + buffer, y + offset, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x + buffer, y + offset, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( pos_x - buffer, y + offset, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case DOWN:
						tess.addVertexWithUV( pos_x + buffer, y + offset, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x - buffer, y + offset, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x - buffer, y + offset, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( pos_x + buffer, y + offset, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case EAST:
						offset = 1.0 - offset;
						tess.addVertexWithUV( x + offset, pos_x + buffer, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( x + offset, pos_x - buffer, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( x + offset, pos_x - buffer, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( x + offset, pos_x + buffer, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case WEST:
						tess.addVertexWithUV( x + offset, pos_x - buffer, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( x + offset, pos_x + buffer, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( x + offset, pos_x + buffer, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( x + offset, pos_x - buffer, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case SOUTH:
						offset = 1.0 - offset;
						tess.addVertexWithUV( pos_x + buffer, pos_y - buffer, z + offset, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x - buffer, pos_y - buffer, z + offset, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x - buffer, pos_y + buffer, z + offset, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( pos_x + buffer, pos_y + buffer, z + offset, ico.getMinU(), ico.getMaxV() );
						break;

					case NORTH:
						tess.addVertexWithUV( pos_x - buffer, pos_y - buffer, z + offset, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x + buffer, pos_y - buffer, z + offset, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( pos_x + buffer, pos_y + buffer, z + offset, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( pos_x - buffer, pos_y + buffer, z + offset, ico.getMinU(), ico.getMaxV() );
						break;

					default:
				}
			}

			out = true;
		}

		return out;
	}
}
