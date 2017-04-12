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


import appeng.block.networking.BlockController;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.networking.TileController;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;


public class RenderBlockController extends BaseBlockRender<BlockController, TileController>
{

	public RenderBlockController()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( final BlockController blk, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{

		final boolean xx = this.getTileEntity( world, x - 1, y, z ) instanceof TileController && this.getTileEntity( world, x + 1, y, z ) instanceof TileController;
		final boolean yy = this.getTileEntity( world, x, y - 1, z ) instanceof TileController && this.getTileEntity( world, x, y + 1, z ) instanceof TileController;
		final boolean zz = this.getTileEntity( world, x, y, z - 1 ) instanceof TileController && this.getTileEntity( world, x, y, z + 1 ) instanceof TileController;

		final int meta = world.getBlockMetadata( x, y, z );
		final boolean hasPower = meta > 0;
		final boolean isConflict = meta == 2;

		ExtraBlockTextures lights = null;

		if( xx && !yy && !zz )
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumnPowered.getIcon() );
				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerColumnConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerColumnLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumn.getIcon() );
			}

			renderer.uvRotateEast = 1;
			renderer.uvRotateWest = 1;
			renderer.uvRotateTop = 1;
			renderer.uvRotateBottom = 1;
		}
		else if( !xx && yy && !zz )
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumnPowered.getIcon() );
				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerColumnConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerColumnLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumn.getIcon() );
			}

			renderer.uvRotateEast = 0;
			renderer.uvRotateNorth = 0;
		}
		else if( !xx && !yy && zz )
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumnPowered.getIcon() );
				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerColumnConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerColumnLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumn.getIcon() );
			}

			renderer.uvRotateNorth = 1;
			renderer.uvRotateSouth = 1;
			renderer.uvRotateTop = 0;
		}
		else if( ( xx ? 1 : 0 ) + ( yy ? 1 : 0 ) + ( zz ? 1 : 0 ) >= 2 )
		{
			final int v = ( Math.abs( x ) + Math.abs( y ) + Math.abs( z ) ) % 2;

			renderer.uvRotateEast = renderer.uvRotateBottom = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

			if( v == 0 )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerInsideA.getIcon() );
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerInsideB.getIcon() );
			}
		}
		else
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerPowered.getIcon() );

				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( null );
			}
		}

		final boolean out = renderer.renderStandardBlock( blk, x, y, z );

		if( lights != null )
		{
			Tessellator.instance.setColorOpaque_F( 1.0f, 1.0f, 1.0f );
			Tessellator.instance.setBrightness( 14 << 20 | 14 << 4 );
			renderer.renderFaceXNeg( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceXPos( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceYNeg( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceYPos( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceZNeg( blk, x, y, z, lights.getIcon() );
			renderer.renderFaceZPos( blk, x, y, z, lights.getIcon() );
		}

		blk.getRendererInstance().setTemporaryRenderIcon( null );
		renderer.uvRotateEast = renderer.uvRotateBottom = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		return out;
	}

	private TileEntity getTileEntity( final IBlockAccess world, final int x, final int y, final int z )
	{
		if( y >= 0 )
		{
			return world.getTileEntity( x, y, z );
		}
		return null;
	}
}
