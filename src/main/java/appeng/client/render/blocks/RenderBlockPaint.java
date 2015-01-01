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

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.helpers.Splotch;
import appeng.tile.misc.TilePaint;

public class RenderBlockPaint extends BaseBlockRender
{

	public RenderBlockPaint() {
		super( false, 0 );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{

	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TilePaint tp = imb.getTileEntity( world, x, y, z );
		boolean out = false;
		if ( tp != null )
		{
			// super.renderInWorld( imb, world, x, y, z, renderer );

			IIcon[] icoSet = new IIcon[] { imb.getIcon( 0, 0 ), ExtraBlockTextures.BlockPaint2.getIcon(), ExtraBlockTextures.BlockPaint3.getIcon() };

			Tessellator tess = Tessellator.instance;

			int lumen = 14 << 20 | 14 << 4;
			int brightness = imb.getMixedBrightnessForBlock( world, x, y, z );

			double offsetConstant = 0.001;

			EnumSet<ForgeDirection> validSides = EnumSet.noneOf( ForgeDirection.class );

			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				if ( tp.isSideValid( side ) )
					validSides.add( side );
			}

			for (Splotch s : tp.getDots())
			{
				if ( !validSides.contains( s.side ) )
					continue;

				if ( s.lumen )
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

				double H = 0.1;

				double pos_x = s.x();
				double pos_y = s.y();

				pos_x = Math.max( H, Math.min( 1.0 - H, pos_x ) );
				pos_y = Math.max( H, Math.min( 1.0 - H, pos_y ) );

				if ( s.side == ForgeDirection.SOUTH || s.side == ForgeDirection.NORTH )
				{
					pos_x += x;
					pos_y += y;
				}

				else if ( s.side == ForgeDirection.UP || s.side == ForgeDirection.DOWN )
				{
					pos_x += x;
					pos_y += z;
				}

				else
				{
					pos_x += y;
					pos_y += z;
				}

				IIcon ico = icoSet[s.getSeed() % icoSet.length];

				switch (s.side)
				{
				case UP:
					offset = 1.0 - offset;
					tess.addVertexWithUV( pos_x - H, y + offset, pos_y - H, ico.getMinU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x + H, y + offset, pos_y - H, ico.getMaxU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x + H, y + offset, pos_y + H, ico.getMaxU(), ico.getMaxV() );
					tess.addVertexWithUV( pos_x - H, y + offset, pos_y + H, ico.getMinU(), ico.getMaxV() );
					break;

				case DOWN:
					tess.addVertexWithUV( pos_x + H, y + offset, pos_y - H, ico.getMinU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x - H, y + offset, pos_y - H, ico.getMaxU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x - H, y + offset, pos_y + H, ico.getMaxU(), ico.getMaxV() );
					tess.addVertexWithUV( pos_x + H, y + offset, pos_y + H, ico.getMinU(), ico.getMaxV() );
					break;

				case EAST:
					offset = 1.0 - offset;
					tess.addVertexWithUV( x + offset, pos_x + H, pos_y - H, ico.getMinU(), ico.getMinV() );
					tess.addVertexWithUV( x + offset, pos_x - H, pos_y - H, ico.getMaxU(), ico.getMinV() );
					tess.addVertexWithUV( x + offset, pos_x - H, pos_y + H, ico.getMaxU(), ico.getMaxV() );
					tess.addVertexWithUV( x + offset, pos_x + H, pos_y + H, ico.getMinU(), ico.getMaxV() );
					break;

				case WEST:
					tess.addVertexWithUV( x + offset, pos_x - H, pos_y - H, ico.getMinU(), ico.getMinV() );
					tess.addVertexWithUV( x + offset, pos_x + H, pos_y - H, ico.getMaxU(), ico.getMinV() );
					tess.addVertexWithUV( x + offset, pos_x + H, pos_y + H, ico.getMaxU(), ico.getMaxV() );
					tess.addVertexWithUV( x + offset, pos_x - H, pos_y + H, ico.getMinU(), ico.getMaxV() );
					break;

				case SOUTH:
					offset = 1.0 - offset;
					tess.addVertexWithUV( pos_x + H, pos_y - H, z + offset, ico.getMinU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x - H, pos_y - H, z + offset, ico.getMaxU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x - H, pos_y + H, z + offset, ico.getMaxU(), ico.getMaxV() );
					tess.addVertexWithUV( pos_x + H, pos_y + H, z + offset, ico.getMinU(), ico.getMaxV() );
					break;

				case NORTH:
					tess.addVertexWithUV( pos_x - H, pos_y - H, z + offset, ico.getMinU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x + H, pos_y - H, z + offset, ico.getMaxU(), ico.getMinV() );
					tess.addVertexWithUV( pos_x + H, pos_y + H, z + offset, ico.getMaxU(), ico.getMaxV() );
					tess.addVertexWithUV( pos_x - H, pos_y + H, z + offset, ico.getMinU(), ico.getMaxV() );
					break;

				default:
				}
			}

			out = true;
		}

		return out;
	}
}
