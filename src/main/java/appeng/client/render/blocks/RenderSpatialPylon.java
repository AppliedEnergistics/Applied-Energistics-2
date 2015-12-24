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


import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.util.AEPartLocation;
import appeng.block.spatial.BlockSpatialPylon;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.tile.spatial.TileSpatialPylon;


public class RenderSpatialPylon extends BaseBlockRender<BlockSpatialPylon, TileSpatialPylon>
{

	public RenderSpatialPylon()
	{
		super( false, 0 );
	}

	@Override
	public void renderInventory( final BlockSpatialPylon block, final ItemStack is, final ModelGenerator renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.setOverrideBlockTexture( ExtraBlockTextures.BlockSpatialPylon_dim.getIcon() );
		super.renderInventory( block, is, renderer, type, obj );
		renderer.setOverrideBlockTexture( null );
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( final BlockSpatialPylon imb, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		final TileSpatialPylon sp = imb.getTileEntity( world, pos );

		final int displayBits = ( sp == null ) ? 0 : sp.getDisplayBits();

		if( displayBits != 0 )
		{
			EnumFacing ori = null;// AEPartLocation.INTERNAL;
			if( ( displayBits & TileSpatialPylon.DISPLAY_Z ) == TileSpatialPylon.DISPLAY_X )
			{
				ori = EnumFacing.EAST;
				if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					renderer.setUvRotateEast( 1 );
					renderer.setUvRotateWest( 2 );
					renderer.setUvRotateTop( 2 );
					renderer.setUvRotateBottom( 1 );
				}
				else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					renderer.setUvRotateEast( 2 );
					renderer.setUvRotateWest( 1 );
					renderer.setUvRotateTop( 1 );
					renderer.setUvRotateBottom( 2 );
				}
				else
				{
					renderer.setUvRotateEast( 1 );
					renderer.setUvRotateWest( 1 );
					renderer.setUvRotateTop( 1 );
					renderer.setUvRotateBottom( 1 );
				}
			}

			else if( ( displayBits & TileSpatialPylon.DISPLAY_Z ) == TileSpatialPylon.DISPLAY_Y )
			{
				ori = EnumFacing.UP;
				if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					renderer.setUvRotateNorth( 3 );
					renderer.setUvRotateSouth( 3 );
					renderer.setUvRotateEast( 3 );
					renderer.setUvRotateWest( 3 );
				}
			}

			else if( ( displayBits & TileSpatialPylon.DISPLAY_Z ) == TileSpatialPylon.DISPLAY_Z )
			{
				ori = EnumFacing.NORTH;
				if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					renderer.setUvRotateSouth( 1 );
					renderer.setUvRotateNorth( 2 );
				}
				else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					renderer.setUvRotateNorth( 1 );
					renderer.setUvRotateSouth( 2 );
					renderer.setUvRotateTop( 3 );
					renderer.setUvRotateBottom( 3 );
				}
				else
				{
					renderer.setUvRotateNorth( 1 );
					renderer.setUvRotateSouth( 2 );
				}
			}

			final BlockRenderInfo bri = imb.getRendererInstance();
			bri.setTemporaryRenderIcon( null );
			bri.setTemporaryRenderIcons( this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.UP, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.DOWN, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.SOUTH, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.NORTH, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.EAST, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.WEST, renderer ) );

			final boolean r = renderer.renderStandardBlock( imb, pos );

			if( ( displayBits & TileSpatialPylon.DISPLAY_POWERED_ENABLED ) == TileSpatialPylon.DISPLAY_POWERED_ENABLED )
			{
				final int bn = 15;
				renderer.setBrightness( bn << 20 | bn << 4 );
				renderer.setColorOpaque_I( 0xffffff );

				for( final EnumFacing d : EnumFacing.VALUES )
				{
					this.renderFace( pos, imb, this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, d, renderer ), renderer, d );
				}
			}
			else
			{
				bri.setTemporaryRenderIcon( null );
				bri.setTemporaryRenderIcons( this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.UP, renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.DOWN, renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.SOUTH, renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.NORTH, renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.EAST, renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.WEST, renderer ) );

				renderer.renderStandardBlock( imb, pos );
			}

			bri.setTemporaryRenderIcon( null );
			renderer.setUvRotateEast( renderer.setUvRotateWest( renderer.setUvRotateNorth( renderer.setUvRotateSouth( renderer.setUvRotateTop( renderer.setUvRotateBottom( 0 ) ) ) ) ) );

			return r;
		}

		renderer.setOverrideBlockTexture( renderer.getIcon( world.getBlockState( pos ) )[0] );// imb.getIcon( 0, 0 );
		boolean result = renderer.renderStandardBlock( imb, pos );

		renderer.setOverrideBlockTexture( ExtraBlockTextures.BlockSpatialPylon_dim.getIcon() );
		result = renderer.renderStandardBlock( imb, pos );

		renderer.setOverrideBlockTexture( null );
		return result;
	}

	private IAESprite getBlockTextureFromSideOutside( final BlockSpatialPylon blk, final TileSpatialPylon sp, final int displayBits, final EnumFacing ori, final EnumFacing dir, final ModelGenerator renderer )
	{

		if( ori == dir || ori.getOpposite() == dir )
		{
			return blk.getRendererInstance().getTexture( AEPartLocation.fromFacing( dir ) );
		}

		if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_MIDDLE )
		{
			return ExtraBlockTextures.BlockSpatialPylonC.getIcon();
		}
		else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
		{
			return ExtraBlockTextures.BlockSpatialPylonE.getIcon();
		}
		else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
		{
			return ExtraBlockTextures.BlockSpatialPylonE.getIcon();
		}

		return renderer.getIcon( blk.getDefaultState() )[0];// blk.getIcon( 0, 0 );
	}

	private IAESprite getBlockTextureFromSideInside( final BlockSpatialPylon blk, final TileSpatialPylon sp, final int displayBits, final EnumFacing ori, final EnumFacing dir, final ModelGenerator renderer )
	{
		final boolean good = ( displayBits & TileSpatialPylon.DISPLAY_ENABLED ) == TileSpatialPylon.DISPLAY_ENABLED;

		if( ori == dir || ori.getOpposite() == dir )
		{
			return good ? ExtraBlockTextures.BlockSpatialPylon_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylon_red.getIcon();
		}

		if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_MIDDLE )
		{
			return good ? ExtraBlockTextures.BlockSpatialPylonC_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylonC_red.getIcon();
		}
		else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
		{
			return good ? ExtraBlockTextures.BlockSpatialPylonE_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylonE_red.getIcon();
		}
		else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
		{
			return good ? ExtraBlockTextures.BlockSpatialPylonE_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylonE_red.getIcon();
		}

		return renderer.getIcon( blk.getDefaultState() )[0];// blk.getIcon( 0, 0 );
	}
}
