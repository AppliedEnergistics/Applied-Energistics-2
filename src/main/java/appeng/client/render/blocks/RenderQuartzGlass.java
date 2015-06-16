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


import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.AEApi;
import appeng.api.util.AEPartLocation;
import appeng.block.solids.BlockQuartzGlass;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.IRenderHelper;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.OffsetIcon;
import appeng.tile.AEBaseTile;


public class RenderQuartzGlass extends BaseBlockRender<BlockQuartzGlass, AEBaseTile>
{

	static byte[][][] offsets;

	public RenderQuartzGlass()
	{
		super( false, 0 );
		if( offsets == null )
		{
			Random r = new Random( 924 );
			offsets = new byte[10][10][10];
			for( int x = 0; x < 10; x++ )
			{
				for( int y = 0; y < 10; y++ )
				{
					r.nextBytes( offsets[x][y] );
				}
			}
		}
	}

	@Override
	public void renderInventory( BlockQuartzGlass block, ItemStack is, IRenderHelper renderer, ItemRenderType type, Object[] obj )
	{
		renderer.overrideBlockTexture = ExtraBlockTextures.GlassFrame.getIcon();
		super.renderInventory( block, is, renderer, type, obj );
		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( BlockQuartzGlass imb, IBlockAccess world, BlockPos pos, IRenderHelper renderer )
	{
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		int cx = Math.abs( pos.getX() % 10 );
		int cy = Math.abs( pos.getY() % 10 );
		int cz = Math.abs( pos.getZ() % 10 );

		int u = offsets[cx][cy][cz] % 4;
		int v = offsets[9 - cx][9 - cy][9 - cz] % 4;

		switch( Math.abs( ( offsets[cx][cy][cz] + ( pos.getX()+pos.getY()+pos.getZ() ) ) % 4 ) )
		{
			case 0:
				renderer.overrideBlockTexture = new OffsetIcon( renderer.getIcon( world.getBlockState( pos ) )[0], u / 2, v / 2 );
				break;
			case 1:
				renderer.overrideBlockTexture = new OffsetIcon( ExtraBlockTextures.BlockQuartzGlassB.getIcon(), u / 2, v / 2 );
				break;
			case 2:
				renderer.overrideBlockTexture = new OffsetIcon( ExtraBlockTextures.BlockQuartzGlassC.getIcon(), u, v );
				break;
			case 3:
				renderer.overrideBlockTexture = new OffsetIcon( ExtraBlockTextures.BlockQuartzGlassD.getIcon(), u, v );
				break;
		}

		boolean result = renderer.renderStandardBlock( imb, pos );

		renderer.overrideBlockTexture = null;
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.UP, AEPartLocation.EAST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.UP, AEPartLocation.WEST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.UP, AEPartLocation.NORTH );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.UP, AEPartLocation.SOUTH );

		this.renderEdge( imb, world, pos, renderer, AEPartLocation.DOWN, AEPartLocation.EAST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.DOWN, AEPartLocation.WEST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.DOWN, AEPartLocation.NORTH );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.DOWN, AEPartLocation.SOUTH );

		this.renderEdge( imb, world, pos, renderer, AEPartLocation.EAST, AEPartLocation.UP );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.EAST, AEPartLocation.DOWN );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.EAST, AEPartLocation.NORTH );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.EAST, AEPartLocation.SOUTH );

		this.renderEdge( imb, world, pos, renderer, AEPartLocation.WEST, AEPartLocation.UP );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.WEST, AEPartLocation.DOWN );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.WEST, AEPartLocation.NORTH );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.WEST, AEPartLocation.SOUTH );

		this.renderEdge( imb, world, pos, renderer, AEPartLocation.NORTH, AEPartLocation.EAST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.NORTH, AEPartLocation.WEST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.NORTH, AEPartLocation.UP );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.NORTH, AEPartLocation.DOWN );

		this.renderEdge( imb, world, pos, renderer, AEPartLocation.SOUTH, AEPartLocation.EAST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.SOUTH, AEPartLocation.WEST );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.SOUTH, AEPartLocation.UP );
		this.renderEdge( imb, world, pos, renderer, AEPartLocation.SOUTH, AEPartLocation.DOWN );

		return result;
	}

	private void renderEdge( BlockQuartzGlass imb, IBlockAccess world, BlockPos pos, IRenderHelper renderer, AEPartLocation side, AEPartLocation direction )
	{
		if( !this.isFlush( imb, world, pos.getX() + side.xOffset, pos.getY() + side.yOffset, pos.getZ() + side.zOffset ) )
		{
			if( !this.isFlush( imb, world, pos.getX() + direction.xOffset, pos.getY() + direction.yOffset, pos.getZ() + direction.zOffset ) )
			{
				float minX = 0.5f + ( side.xOffset + direction.xOffset ) / 2.0f;
				float minY = 0.5f + ( side.yOffset + direction.yOffset ) / 2.0f;
				float minZ = 0.5f + ( side.zOffset + direction.zOffset ) / 2.0f;
				float maxX = 0.5f + ( side.xOffset + direction.xOffset ) / 2.0f;
				float maxY = 0.5f + ( side.yOffset + direction.yOffset ) / 2.0f;
				float maxZ = 0.5f + ( side.zOffset + direction.zOffset ) / 2.0f;

				if( 0 == side.xOffset && 0 == direction.xOffset )
				{
					minX = 0.0f;
					maxX = 1.0f;
				}
				if( 0 == side.yOffset && 0 == direction.yOffset )
				{
					minY = 0.0f;
					maxY = 1.0f;
				}
				if( 0 == side.zOffset && 0 == direction.zOffset )
				{
					minZ = 0.0f;
					maxZ = 1.0f;
				}

				if( maxX <= 0.001f )
				{
					maxX += 0.9f / 16.0f;
				}
				if( maxY <= 0.001f )
				{
					maxY += 0.9f / 16.0f;
				}
				if( maxZ <= 0.001f )
				{
					maxZ += 0.9f / 16.0f;
				}

				if( minX >= 0.999f )
				{
					minX -= 0.9f / 16.0f;
				}
				if( minY >= 0.999f )
				{
					minY -= 0.9f / 16.0f;
				}
				if( minZ >= 0.999f )
				{
					minZ -= 0.9f / 16.0f;
				}

				renderer.setRenderBounds( minX, minY, minZ, maxX, maxY, maxZ );

				switch( side )
				{
					case WEST:
						renderer.renderFaceXNeg( imb, pos, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case EAST:
						renderer.renderFaceXPos( imb, pos, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case NORTH:
						renderer.renderFaceZNeg( imb, pos, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case SOUTH:
						renderer.renderFaceZPos( imb, pos, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case DOWN:
						renderer.renderFaceYNeg( imb, pos, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case UP:
						renderer.renderFaceYPos( imb, pos, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					default:
						break;
				}
			}
		}
	}

	private boolean isFlush( BlockQuartzGlass imb, IBlockAccess world, int x, int y, int z )
	{
		return this.isGlass( imb, world, new BlockPos(x, y, z) );
	}

	private boolean isGlass( BlockQuartzGlass imb, IBlockAccess world,BlockPos pos)
	{
		return this.isQuartzGlass( world, pos ) || this.isVibrantQuartzGlass( world, pos );
	}

	private boolean isQuartzGlass( IBlockAccess world,BlockPos pos)
	{
		return AEApi.instance().definitions().blocks().quartzGlass().isSameAs( world, pos );
	}

	private boolean isVibrantQuartzGlass( IBlockAccess world,BlockPos pos)
	{
		return AEApi.instance().definitions().blocks().quartzVibrantGlass().isSameAs( world, pos );
	}
}
