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

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.ModelGenerator;
import appeng.tile.AEBaseTile;


public class RenderQuartzTorch extends BaseBlockRender<AEBaseBlock, AEBaseTile>
{

	public RenderQuartzTorch()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( AEBaseBlock blk, ItemStack is, ModelGenerator renderer, ItemRenderType type, Object[] obj )
	{
		float Point2 = 6.0f / 16.0f;
		float Point3 = 7.0f / 16.0f;
		float Point13 = 10.0f / 16.0f;
		float Point12 = 9.0f / 16.0f;

		float singlePixel = 1.0f / 16.0f;
		float renderBottom = 5.0f / 16.0f;
		float renderTop = 10.0f / 16.0f;

		float bottom = 7.0f / 16.0f;
		float top = 8.0f / 16.0f;

		float xOff = 0.0f;
		float yOff = 0.0f;
		float zOff = 0.0f;

		renderer.setRenderBounds( Point3 + xOff, renderBottom + yOff, Point3 + zOff, Point12 + xOff, renderTop + yOff, Point12 + zOff );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		renderer.setRenderBounds( Point3 + xOff, renderTop + yOff, Point3 + zOff, Point3 + singlePixel + xOff, renderTop + singlePixel + yOff, Point3 + singlePixel + zOff );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		renderer.setRenderBounds( Point12 - singlePixel + xOff, renderBottom - singlePixel + yOff, Point12 - singlePixel + zOff, Point12 + xOff, renderBottom + yOff, Point12 + zOff );

		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( renderer.getIcon( Blocks.hopper.getDefaultState() )[0] );
		renderer.renderAllFaces = true;

		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point2 + zOff, Point13 + xOff, top + yOff, Point3 + zOff );

		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is,  0xffffff, renderer );
		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point12 + zOff, Point13 + xOff, top + yOff, Point13 + zOff );

		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );
		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point3 + zOff, Point3 + xOff, top + yOff, Point12 + zOff );

		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );
		renderer.setRenderBounds( Point12 + xOff, bottom + yOff, Point3 + zOff, Point13 + xOff, top + yOff, Point12 + zOff );

		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		renderer.renderAllFaces = false;
		blk.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public boolean renderInWorld( AEBaseBlock blk, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{
		IOrientable te = ( (IOrientableBlock) blk ).getOrientable( world, pos );

		float Point2 = 6.0f / 16.0f;
		float Point3 = 7.0f / 16.0f;
		float Point13 = 10.0f / 16.0f;
		float Point12 = 9.0f / 16.0f;

		float singlePixel = 1.0f / 16.0f;
		float renderBottom = 5.0f / 16.0f;
		float renderTop = 10.0f / 16.0f;

		float bottom = 7.0f / 16.0f;
		float top = 8.0f / 16.0f;

		float xOff = 0.0f;
		float yOff = 0.0f;
		float zOff = 0.0f;

		renderer.renderAllFaces = true;
		if( te != null )
		{
			AEPartLocation forward = AEPartLocation.fromFacing( te.getUp() );
			xOff = forward.xOffset * -( 4.0f / 16.0f );
			yOff = forward.yOffset * -( 4.0f / 16.0f );
			zOff = forward.zOffset * -( 4.0f / 16.0f );
		}

		renderer.setRenderBounds( Point3 + xOff, renderBottom + yOff, Point3 + zOff, Point12 + xOff, renderTop + yOff, Point12 + zOff );
		super.renderInWorld( blk, world, pos, renderer );

		int r = ( pos.getX() + pos.getY() + pos.getZ() ) % 2;
		if( r == 0 )
		{
			renderer.setRenderBounds( Point3 + xOff, renderTop + yOff, Point3 + zOff, Point3 + singlePixel + xOff, renderTop + singlePixel + yOff, Point3 + singlePixel + zOff );
			super.renderInWorld( blk, world, pos, renderer );

			renderer.setRenderBounds( Point12 - singlePixel + xOff, renderBottom - singlePixel + yOff, Point12 - singlePixel + zOff, Point12 + xOff, renderBottom + yOff, Point12 + zOff );
			super.renderInWorld( blk, world, pos, renderer );
		}
		else
		{
			renderer.setRenderBounds( Point3 + xOff, renderBottom - singlePixel + yOff, Point3 + zOff, Point3 + singlePixel + xOff, renderBottom + yOff, Point3 + singlePixel + zOff );
			super.renderInWorld( blk, world, pos, renderer );

			renderer.setRenderBounds( Point12 - singlePixel + xOff, renderTop + yOff, Point12 - singlePixel + zOff, Point12 + xOff, renderTop + singlePixel + yOff, Point12 + zOff );
			super.renderInWorld( blk, world, pos, renderer );
		}

		blk.getRendererInstance().setTemporaryRenderIcon( renderer.getIcon( Blocks.hopper.getDefaultState() )[0] );

		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point2 + zOff, Point13 + xOff, top + yOff, Point3 + zOff );
		boolean out = renderer.renderStandardBlock( blk, pos );

		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point12 + zOff, Point13 + xOff, top + yOff, Point13 + zOff );
		renderer.renderStandardBlock( blk, pos );

		renderer.setRenderBounds( Point2 + xOff, bottom + yOff, Point3 + zOff, Point3 + xOff, top + yOff, Point12 + zOff );
		renderer.renderStandardBlock( blk, pos );

		renderer.setRenderBounds( Point12 + xOff, bottom + yOff, Point3 + zOff, Point13 + xOff, top + yOff, Point12 + zOff );
		renderer.renderStandardBlock( blk, pos );

		if( te != null )
		{
			AEPartLocation forward = AEPartLocation.fromFacing( te.getUp());
			switch( forward )
			{
				case EAST:
					renderer.setRenderBounds( 0, bottom + yOff, bottom + zOff, Point2 + xOff, top + yOff, top + zOff );
					renderer.renderStandardBlock( blk, pos );
					break;
				case WEST:
					renderer.setRenderBounds( Point13 + xOff, bottom + yOff, bottom + zOff, 1.0, top + yOff, top + zOff );
					renderer.renderStandardBlock( blk, pos );
					break;
				case NORTH:
					renderer.setRenderBounds( bottom + xOff, bottom + yOff, Point13 + zOff, top + xOff, top + yOff, 1.0 );
					renderer.renderStandardBlock( blk, pos );
					break;
				case SOUTH:
					renderer.setRenderBounds( bottom + xOff, bottom + yOff, 0, top + xOff, top + yOff, Point2 + zOff );
					renderer.renderStandardBlock( blk, pos );
					break;
				case UP:
					renderer.setRenderBounds( Point2, 0, Point2, Point3, bottom + yOff, Point3 );
					renderer.renderStandardBlock( blk, pos );
					renderer.setRenderBounds( Point2, 0, Point12, Point3, bottom + yOff, Point13 );
					renderer.renderStandardBlock( blk, pos );
					renderer.setRenderBounds( Point12, 0, Point2, Point13, bottom + yOff, Point3 );
					renderer.renderStandardBlock( blk, pos );
					renderer.setRenderBounds( Point12, 0, Point12, Point13, bottom + yOff, Point13 );
					renderer.renderStandardBlock( blk, pos );
					break;
				case DOWN:
					renderer.setRenderBounds( Point2, top + yOff, Point2, Point3, 1.0, Point3 );
					renderer.renderStandardBlock( blk, pos );
					renderer.setRenderBounds( Point2, top + yOff, Point12, Point3, 1.0, Point13 );
					renderer.renderStandardBlock( blk, pos );
					renderer.setRenderBounds( Point12, top + yOff, Point2, Point13, 1.0, Point3 );
					renderer.renderStandardBlock( blk, pos );
					renderer.setRenderBounds( Point12, top + yOff, Point12, Point13, 1.0, Point13 );
					renderer.renderStandardBlock( blk, pos );
					break;
				default:
			}
		}

		renderer.renderAllFaces = false;
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		return out;
	}
}
