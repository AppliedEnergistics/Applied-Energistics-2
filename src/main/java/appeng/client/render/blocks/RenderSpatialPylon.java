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
	public void renderInventory( BlockSpatialPylon block, ItemStack is, ModelGenerator renderer, ItemRenderType type, Object[] obj )
	{
		renderer.overrideBlockTexture = ExtraBlockTextures.BlockSpatialPylon_dim.getIcon();
		super.renderInventory( block, is, renderer, type, obj );
		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( BlockSpatialPylon imb, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		TileSpatialPylon sp = imb.getTileEntity( world, pos  );

		int displayBits = ( sp == null ) ? 0 : sp.getDisplayBits();
		EnumFacing ori = null;//AEPartLocation.INTERNAL;

		if( displayBits != 0 )
		{
			if( ( displayBits & TileSpatialPylon.DISPLAY_Z ) == TileSpatialPylon.DISPLAY_X )
			{
				ori = EnumFacing.EAST;
				if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					renderer.uvRotateEast = 1;
					renderer.uvRotateWest = 2;
					renderer.uvRotateTop = 2;
					renderer.uvRotateBottom = 1;
				}
				else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					renderer.uvRotateEast = 2;
					renderer.uvRotateWest = 1;
					renderer.uvRotateTop = 1;
					renderer.uvRotateBottom = 2;
				}
				else
				{
					renderer.uvRotateEast = 1;
					renderer.uvRotateWest = 1;
					renderer.uvRotateTop = 1;
					renderer.uvRotateBottom = 1;
				}
			}

			else if( ( displayBits & TileSpatialPylon.DISPLAY_Z ) == TileSpatialPylon.DISPLAY_Y )
			{
				ori = EnumFacing.UP;
				if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					renderer.uvRotateNorth = 3;
					renderer.uvRotateSouth = 3;
					renderer.uvRotateEast = 3;
					renderer.uvRotateWest = 3;
				}
			}

			else if( ( displayBits & TileSpatialPylon.DISPLAY_Z ) == TileSpatialPylon.DISPLAY_Z )
			{
				ori = EnumFacing.NORTH;
				if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MAX )
				{
					renderer.uvRotateSouth = 1;
					renderer.uvRotateNorth = 2;
				}
				else if( ( displayBits & TileSpatialPylon.DISPLAY_MIDDLE ) == TileSpatialPylon.DISPLAY_END_MIN )
				{
					renderer.uvRotateNorth = 1;
					renderer.uvRotateSouth = 2;
					renderer.uvRotateTop = 3;
					renderer.uvRotateBottom = 3;
				}
				else
				{
					renderer.uvRotateNorth = 1;
					renderer.uvRotateSouth = 2;
				}
			}

			BlockRenderInfo bri = imb.getRendererInstance();
			bri.setTemporaryRenderIcon( null );
			bri.setTemporaryRenderIcons( this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.UP, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.DOWN, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.SOUTH, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.NORTH, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.EAST, renderer ), this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, EnumFacing.WEST , renderer) );

			boolean r = renderer.renderStandardBlock( imb, pos );

			if( ( displayBits & TileSpatialPylon.DISPLAY_POWERED_ENABLED ) == TileSpatialPylon.DISPLAY_POWERED_ENABLED )
			{
				int bn = 15;
				renderer.setBrightness( bn << 20 | bn << 4 );
				renderer.setColorOpaque_I( 0xffffff );

				for( EnumFacing d : EnumFacing.VALUES )
				{
					this.renderFace( pos, imb, this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, d,renderer ), renderer, d );
				}
			}
			else
			{
				bri.setTemporaryRenderIcon( null );
				bri.setTemporaryRenderIcons( this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.UP,renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.DOWN,renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.SOUTH,renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.NORTH,renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.EAST,renderer ), this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, EnumFacing.WEST,renderer ) );

				renderer.renderStandardBlock( imb, pos );
			}

			bri.setTemporaryRenderIcon( null );
			renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateBottom = 0;

			return r;
		}

		renderer.overrideBlockTexture = renderer.getIcon( world.getBlockState( pos ) )[0];//imb.getIcon( 0, 0 );
		boolean result = renderer.renderStandardBlock( imb, pos );

		renderer.overrideBlockTexture = ExtraBlockTextures.BlockSpatialPylon_dim.getIcon();
		result = renderer.renderStandardBlock( imb, pos );

		renderer.overrideBlockTexture = null;
		return result;
	}

	private IAESprite getBlockTextureFromSideOutside( BlockSpatialPylon blk, TileSpatialPylon sp, int displayBits, EnumFacing ori, EnumFacing dir, ModelGenerator renderer )
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

	private IAESprite getBlockTextureFromSideInside( BlockSpatialPylon blk, TileSpatialPylon sp, int displayBits, EnumFacing ori, EnumFacing dir, ModelGenerator renderer )
	{
		boolean good = ( displayBits & TileSpatialPylon.DISPLAY_ENABLED ) == TileSpatialPylon.DISPLAY_ENABLED;

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

		return renderer.getIcon( blk.getDefaultState() )[0];//blk.getIcon( 0, 0 );
	}
}
