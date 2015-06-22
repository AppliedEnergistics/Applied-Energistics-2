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


import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.block.misc.BlockPaint;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.helpers.Splotch;
import appeng.tile.misc.TilePaint;


public class RenderBlockPaint extends BaseBlockRender<BlockPaint, TilePaint>
{

	public RenderBlockPaint()
	{
		super( false, 0 );
	}

	@Override
	public void renderInventory( BlockPaint block, ItemStack is, ModelGenerator renderer, ItemRenderType type, Object[] obj )
	{

	}

	@Override
	public boolean renderInWorld( BlockPaint imb, IBlockAccess world, BlockPos pos, ModelGenerator tess )
	{
		TilePaint tp = imb.getTileEntity( world, pos );
		boolean out = false;
		if( tp != null )
		{
			// super.renderInWorld( imb, world, x, y, z, renderer );
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();

			IAESprite[] icoSet = new IAESprite[] { imb.getIcon( EnumFacing.UP, imb.getDefaultState() ), ExtraBlockTextures.BlockPaint2.getIcon(), ExtraBlockTextures.BlockPaint3.getIcon() };

			int lumen = 14 << 20 | 14 << 4;
			int brightness = imb.getMixedBrightnessForBlock( world, pos );

			double offsetConstant = 0.001;

			EnumSet<EnumFacing> validSides = EnumSet.noneOf( EnumFacing.class );

			for( EnumFacing side : EnumFacing.VALUES )
			{
				if( tp.isSideValid( side ) )
				{
					validSides.add( side );
				}
			}

			for( Splotch s : tp.getDots() )
			{
				if( !validSides.contains( s.side ) )
				{
					continue;
				}

				if( s.lumen )
				{
					tess.setColorOpaque_I( s.color.whiteVariant );
					tess.setBrightness( lumen );
				}
				else
				{
					tess.setColorOpaque_I( s.color.mediumVariant );
					tess.setBrightness( brightness );
				}

				double offset = offsetConstant;
				offsetConstant += 0.001;

				double buffer = 0.1;

				double pos_x = s.x();
				double pos_y = s.y();

				pos_x = Math.max( buffer, Math.min( 1.0 - buffer, pos_x ) );
				pos_y = Math.max( buffer, Math.min( 1.0 - buffer, pos_y ) );

				if( s.side == EnumFacing.SOUTH || s.side == EnumFacing.NORTH )
				{
					pos_x += x;
					pos_y += y;
				}

				else if( s.side == EnumFacing.UP || s.side == EnumFacing.DOWN )
				{
					pos_x += x;
					pos_y += z;
				}

				else
				{
					pos_x += y;
					pos_y += z;
				}
				
				IAESprite ico = icoSet[s.getSeed() % icoSet.length];
				EnumFacing rs = s.side.getOpposite();
				
				switch( s.side )
				{
					case UP:
						offset = 1.0 - offset;
						tess.addVertexWithUV( rs,pos_x - buffer, y + offset, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( rs,pos_x + buffer, y + offset, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( rs,pos_x + buffer, y + offset, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( rs,pos_x - buffer, y + offset, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case DOWN:
						tess.addVertexWithUV( rs, pos_x + buffer, y + offset, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( rs, pos_x - buffer, y + offset, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( rs, pos_x - buffer, y + offset, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( rs, pos_x + buffer, y + offset, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case EAST:
						offset = 1.0 - offset;
						tess.addVertexWithUV( rs, x + offset, pos_x + buffer, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( rs, x + offset, pos_x - buffer, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( rs, x + offset, pos_x - buffer, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( rs, x + offset, pos_x + buffer, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case WEST:
						tess.addVertexWithUV( rs, x + offset, pos_x - buffer, pos_y - buffer, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( rs, x + offset, pos_x + buffer, pos_y - buffer, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( rs, x + offset, pos_x + buffer, pos_y + buffer, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( rs, x + offset, pos_x - buffer, pos_y + buffer, ico.getMinU(), ico.getMaxV() );
						break;

					case SOUTH:
						offset = 1.0 - offset;
						tess.addVertexWithUV( rs, pos_x + buffer, pos_y - buffer, z + offset, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( rs, pos_x - buffer, pos_y - buffer, z + offset, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( rs, pos_x - buffer, pos_y + buffer, z + offset, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( rs, pos_x + buffer, pos_y + buffer, z + offset, ico.getMinU(), ico.getMaxV() );
						break;

					case NORTH:
						tess.addVertexWithUV( rs, pos_x - buffer, pos_y - buffer, z + offset, ico.getMinU(), ico.getMinV() );
						tess.addVertexWithUV( rs, pos_x + buffer, pos_y - buffer, z + offset, ico.getMaxU(), ico.getMinV() );
						tess.addVertexWithUV( rs, pos_x + buffer, pos_y + buffer, z + offset, ico.getMaxU(), ico.getMaxV() );
						tess.addVertexWithUV( rs, pos_x - buffer, pos_y + buffer, z + offset, ico.getMinU(), ico.getMaxV() );
						break;

					default:
				}
			}

			out = true;
		}

		return out;
	}
}
