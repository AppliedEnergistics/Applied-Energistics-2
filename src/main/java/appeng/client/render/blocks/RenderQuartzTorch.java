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


import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.tile.AEBaseTile;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class RenderQuartzTorch extends BaseBlockRender<AEBaseBlock, AEBaseTile>
{

	public RenderQuartzTorch()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( final AEBaseBlock blk, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		final Tessellator tess = Tessellator.instance;

		final float Point3 = 7.0f / 16.0f;
		final float Point12 = 9.0f / 16.0f;

		final float renderBottom = 5.0f / 16.0f;
		final float renderTop = 10.0f / 16.0f;

		final float xOff = 0.0f;
		final float yOff = 0.0f;
		final float zOff = 0.0f;

		renderer.setRenderBounds( Point3 + xOff, renderBottom + yOff, Point3 + zOff, Point12 + xOff, renderTop + yOff, Point12 + zOff );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		final float singlePixel = 1.0f / 16.0f;
		renderer.setRenderBounds( Point3 + xOff, renderTop + yOff, Point3 + zOff, Point3 + singlePixel + xOff, renderTop + singlePixel + yOff, Point3 + singlePixel + zOff );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		renderer.setRenderBounds( Point12 - singlePixel + xOff, renderBottom - singlePixel + yOff, Point12 - singlePixel + zOff, Point12 + xOff, renderBottom + yOff, Point12 + zOff );

		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( Blocks.hopper.getIcon( 0, 0 ) );
		renderer.renderAllFaces = true;

		final float top = 8.0f / 16.0f;
		final float bottom = 7.0f / 16.0f;
		final float Point13 = 10.0f / 16.0f;
		final float Point2 = 6.0f / 16.0f;
		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point2 + zOff, Point13 + xOff, top + yOff, Point3 + zOff );

		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );
		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point12 + zOff, Point13 + xOff, top + yOff, Point13 + zOff );

		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );
		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point3 + zOff, Point3 + xOff, top + yOff, Point12 + zOff );

		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );
		renderer.setRenderBounds( Point12 + xOff, bottom + yOff, Point3 + zOff, Point13 + xOff, top + yOff, Point12 + zOff );

		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		renderer.renderAllFaces = false;
		blk.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public boolean renderInWorld( final AEBaseBlock block, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final IOrientable te = block.getOrientable( world, x, y, z );

		float xOff = 0.0f;
		float yOff = 0.0f;
		float zOff = 0.0f;

		renderer.renderAllFaces = true;

		if( te != null )
		{
			final ForgeDirection forward = te.getUp();
			xOff = forward.offsetX * -( 4.0f / 16.0f );
			yOff = forward.offsetY * -( 4.0f / 16.0f );
			zOff = forward.offsetZ * -( 4.0f / 16.0f );
		}

		final float renderTop = 10.0f / 16.0f;
		final float renderBottom = 5.0f / 16.0f;
		final float Point12 = 9.0f / 16.0f;
		final float Point3 = 7.0f / 16.0f;
		renderer.setRenderBounds( Point3 + xOff, renderBottom + yOff, Point3 + zOff, Point12 + xOff, renderTop + yOff, Point12 + zOff );
		super.renderInWorld( block, world, x, y, z, renderer );

		final int r = ( x + y + z ) % 2;
		final float singlePixel = 1.0f / 16.0f;
		if( r == 0 )
		{
			renderer.setRenderBounds( Point3 + xOff, renderTop + yOff, Point3 + zOff, Point3 + singlePixel + xOff, renderTop + singlePixel + yOff, Point3 + singlePixel + zOff );
			super.renderInWorld( block, world, x, y, z, renderer );

			renderer.setRenderBounds( Point12 - singlePixel + xOff, renderBottom - singlePixel + yOff, Point12 - singlePixel + zOff, Point12 + xOff, renderBottom + yOff, Point12 + zOff );
			super.renderInWorld( block, world, x, y, z, renderer );
		}
		else
		{
			renderer.setRenderBounds( Point3 + xOff, renderBottom - singlePixel + yOff, Point3 + zOff, Point3 + singlePixel + xOff, renderBottom + yOff, Point3 + singlePixel + zOff );
			super.renderInWorld( block, world, x, y, z, renderer );

			renderer.setRenderBounds( Point12 - singlePixel + xOff, renderTop + yOff, Point12 - singlePixel + zOff, Point12 + xOff, renderTop + singlePixel + yOff, Point12 + zOff );
			super.renderInWorld( block, world, x, y, z, renderer );
		}

		block.getRendererInstance().setTemporaryRenderIcon( Blocks.hopper.getIcon( 0, 0 ) );

		final float top = 8.0f / 16.0f;
		final float bottom = 7.0f / 16.0f;
		final float Point13 = 10.0f / 16.0f;
		final float Point2 = 6.0f / 16.0f;
		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point2 + zOff, Point13 + xOff, top + yOff, Point3 + zOff );
		final boolean out = renderer.renderStandardBlock( block, x, y, z );

		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point12 + zOff, Point13 + xOff, top + yOff, Point13 + zOff );
		renderer.renderStandardBlock( block, x, y, z );

		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point3 + zOff, Point3 + xOff, top + yOff, Point12 + zOff );
		renderer.renderStandardBlock( block, x, y, z );

		renderer.setRenderBounds( Point12 + xOff, bottom + yOff, Point3 + zOff, Point13 + xOff, top + yOff, Point12 + zOff );
		renderer.renderStandardBlock( block, x, y, z );

		if( te != null )
		{
			final ForgeDirection forward = te.getUp();
			switch( forward )
			{
				case EAST:
					renderer.setRenderBounds( 0, bottom + yOff, bottom + zOff, Point2 + xOff, top + yOff, top + zOff );
					renderer.renderStandardBlock( block, x, y, z );
					break;
				case WEST:
					renderer.setRenderBounds( Point13 + xOff, bottom + yOff, bottom + zOff, 1.0, top + yOff, top + zOff );
					renderer.renderStandardBlock( block, x, y, z );
					break;
				case NORTH:
					renderer.setRenderBounds( bottom + xOff, bottom + yOff, Point13 + zOff, top + xOff, top + yOff, 1.0 );
					renderer.renderStandardBlock( block, x, y, z );
					break;
				case SOUTH:
					renderer.setRenderBounds( bottom + xOff, bottom + yOff, 0, top + xOff, top + yOff, Point2 + zOff );
					renderer.renderStandardBlock( block, x, y, z );
					break;
				case UP:
					renderer.setRenderBounds( Point2, 0, Point2, Point3, bottom + yOff, Point3 );
					renderer.renderStandardBlock( block, x, y, z );
					renderer.setRenderBounds( Point2, 0, Point12, Point3, bottom + yOff, Point13 );
					renderer.renderStandardBlock( block, x, y, z );
					renderer.setRenderBounds( Point12, 0, Point2, Point13, bottom + yOff, Point3 );
					renderer.renderStandardBlock( block, x, y, z );
					renderer.setRenderBounds( Point12, 0, Point12, Point13, bottom + yOff, Point13 );
					renderer.renderStandardBlock( block, x, y, z );
					break;
				case DOWN:
					renderer.setRenderBounds( Point2, top + yOff, Point2, Point3, 1.0, Point3 );
					renderer.renderStandardBlock( block, x, y, z );
					renderer.setRenderBounds( Point2, top + yOff, Point12, Point3, 1.0, Point13 );
					renderer.renderStandardBlock( block, x, y, z );
					renderer.setRenderBounds( Point12, top + yOff, Point2, Point13, 1.0, Point3 );
					renderer.renderStandardBlock( block, x, y, z );
					renderer.setRenderBounds( Point12, top + yOff, Point12, Point13, 1.0, Point13 );
					renderer.renderStandardBlock( block, x, y, z );
					break;
				default:
			}
		}

		renderer.renderAllFaces = false;
		block.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
