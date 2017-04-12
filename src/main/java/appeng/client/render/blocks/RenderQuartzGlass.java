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


import appeng.api.AEApi;
import appeng.block.solids.BlockQuartzGlass;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.OffsetIcon;
import appeng.tile.AEBaseTile;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;


public class RenderQuartzGlass extends BaseBlockRender<BlockQuartzGlass, AEBaseTile>
{

	private static final byte[][][] OFFSETS = generateOffsets();

	public RenderQuartzGlass()
	{
		super( false, 0 );

	}

	private static byte[][][] generateOffsets()
	{
		final Random r = new Random( 924 );
		final byte[][][] offset = new byte[10][10][10];

		for( int x = 0; x < 10; x++ )
		{
			for( int y = 0; y < 10; y++ )
			{
				r.nextBytes( offset[x][y] );
			}
		}

		return offset;
	}

	@Override
	public void renderInventory( final BlockQuartzGlass block, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.overrideBlockTexture = ExtraBlockTextures.GlassFrame.getIcon();
		super.renderInventory( block, is, renderer, type, obj );
		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( final BlockQuartzGlass imb, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		final int cx = Math.abs( x % 10 );
		final int cy = Math.abs( y % 10 );
		final int cz = Math.abs( z % 10 );

		final int u = OFFSETS[cx][cy][cz] % 4;
		final int v = OFFSETS[9 - cx][9 - cy][9 - cz] % 4;

		switch( Math.abs( ( OFFSETS[cx][cy][cz] + ( x + y + z ) ) % 4 ) )
		{
			case 0:
				renderer.overrideBlockTexture = new OffsetIcon( imb.getIcon( 0, 0 ), u / 2, v / 2 );
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

		final boolean result = renderer.renderStandardBlock( imb, x, y, z );

		renderer.overrideBlockTexture = null;
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.EAST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.WEST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.NORTH );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.UP, ForgeDirection.SOUTH );

		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.EAST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.WEST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.NORTH );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.DOWN, ForgeDirection.SOUTH );

		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.UP );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.DOWN );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.NORTH );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.EAST, ForgeDirection.SOUTH );

		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.UP );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.DOWN );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.NORTH );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.WEST, ForgeDirection.SOUTH );

		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.EAST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.WEST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.UP );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.NORTH, ForgeDirection.DOWN );

		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.EAST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.WEST );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.UP );
		this.renderEdge( imb, world, x, y, z, renderer, ForgeDirection.SOUTH, ForgeDirection.DOWN );

		return result;
	}

	private void renderEdge( final BlockQuartzGlass imb, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer, final ForgeDirection side, final ForgeDirection direction )
	{
		if( !this.isFlush( imb, world, x + side.offsetX, y + side.offsetY, z + side.offsetZ ) )
		{
			if( !this.isFlush( imb, world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ ) )
			{
				float minX = 0.5f + ( side.offsetX + direction.offsetX ) / 2.0f;
				float minY = 0.5f + ( side.offsetY + direction.offsetY ) / 2.0f;
				float minZ = 0.5f + ( side.offsetZ + direction.offsetZ ) / 2.0f;
				float maxX = 0.5f + ( side.offsetX + direction.offsetX ) / 2.0f;
				float maxY = 0.5f + ( side.offsetY + direction.offsetY ) / 2.0f;
				float maxZ = 0.5f + ( side.offsetZ + direction.offsetZ ) / 2.0f;

				if( 0 == side.offsetX && 0 == direction.offsetX )
				{
					minX = 0.0f;
					maxX = 1.0f;
				}
				if( 0 == side.offsetY && 0 == direction.offsetY )
				{
					minY = 0.0f;
					maxY = 1.0f;
				}
				if( 0 == side.offsetZ && 0 == direction.offsetZ )
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
						renderer.renderFaceXNeg( imb, x, y, z, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case EAST:
						renderer.renderFaceXPos( imb, x, y, z, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case NORTH:
						renderer.renderFaceZNeg( imb, x, y, z, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case SOUTH:
						renderer.renderFaceZPos( imb, x, y, z, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case DOWN:
						renderer.renderFaceYNeg( imb, x, y, z, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					case UP:
						renderer.renderFaceYPos( imb, x, y, z, ExtraBlockTextures.GlassFrame.getIcon() );
						break;
					default:
						break;
				}
			}
		}
	}

	private boolean isFlush( final BlockQuartzGlass imb, final IBlockAccess world, final int x, final int y, final int z )
	{
		return this.isGlass( imb, world, x, y, z );
	}

	private boolean isGlass( final BlockQuartzGlass imb, final IBlockAccess world, final int x, final int y, final int z )
	{
		return this.isQuartzGlass( world, x, y, z ) || this.isVibrantQuartzGlass( world, x, y, z );
	}

	private boolean isQuartzGlass( final IBlockAccess world, final int x, final int y, final int z )
	{
		return AEApi.instance().definitions().blocks().quartzGlass().isSameAs( world, x, y, z );
	}

	private boolean isVibrantQuartzGlass( final IBlockAccess world, final int x, final int y, final int z )
	{
		return AEApi.instance().definitions().blocks().quartzVibrantGlass().isSameAs( world, x, y, z );
	}
}
