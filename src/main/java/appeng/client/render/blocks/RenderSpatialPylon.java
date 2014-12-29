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

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.spatial.TileSpatialPylon;

public class RenderSpatialPylon extends BaseBlockRender
{

	public RenderSpatialPylon() {
		super( false, 0 );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		renderer.overrideBlockTexture = ExtraBlockTextures.BlockSpatialPylon_dim.getIcon();
		super.renderInventory( block, is, renderer, type, obj );
		renderer.overrideBlockTexture = null;
		super.renderInventory( block, is, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock imb, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		renderer.setRenderBounds( 0, 0, 0, 1, 1, 1 );

		TileSpatialPylon sp = imb.getTileEntity( world, x, y, z );

		int displayBits = sp.getDisplayBits();
		ForgeDirection ori = ForgeDirection.UNKNOWN;

		if ( displayBits != 0 )
		{
			if ( (displayBits & sp.DISPLAY_Z) == sp.DISPLAY_X )
			{
				ori = ForgeDirection.EAST;
				if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MAX )
				{
					renderer.uvRotateEast = 1;
					renderer.uvRotateWest = 2;
					renderer.uvRotateTop = 2;
					renderer.uvRotateBottom = 1;
				}
				else if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MIN )
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

			else if ( (displayBits & sp.DISPLAY_Z) == sp.DISPLAY_Y )
			{
				ori = ForgeDirection.UP;
				if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MAX )
				{
					renderer.uvRotateNorth = 3;
					renderer.uvRotateSouth = 3;
					renderer.uvRotateEast = 3;
					renderer.uvRotateWest = 3;
				}
			}

			else if ( (displayBits & sp.DISPLAY_Z) == sp.DISPLAY_Z )
			{
				ori = ForgeDirection.NORTH;
				if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MAX )
				{
					renderer.uvRotateSouth = 1;
					renderer.uvRotateNorth = 2;
				}
				else if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MIN )
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
			bri.setTemporaryRenderIcons( this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, ForgeDirection.UP ),
					this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, ForgeDirection.DOWN ),
					this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, ForgeDirection.SOUTH ),
					this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, ForgeDirection.NORTH ),
					this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, ForgeDirection.EAST ),
					this.getBlockTextureFromSideOutside( imb, sp, displayBits, ori, ForgeDirection.WEST ) );

			boolean r = renderer.renderStandardBlock( imb, x, y, z );

			if ( (displayBits & sp.DISPLAY_POWERED_ENABLED) == sp.DISPLAY_POWERED_ENABLED )
			{
				int bn = 15;
				Tessellator.instance.setBrightness( bn << 20 | bn << 4 );
				Tessellator.instance.setColorOpaque_I( 0xffffff );

				for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
					this.renderFace( x, y, z, imb, this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, d ), renderer, d );
			}
			else
			{
				bri.setTemporaryRenderIcon( null );
				bri.setTemporaryRenderIcons( this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, ForgeDirection.UP ),
						this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, ForgeDirection.DOWN ),
						this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, ForgeDirection.SOUTH ),
						this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, ForgeDirection.NORTH ),
						this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, ForgeDirection.EAST ),
						this.getBlockTextureFromSideInside( imb, sp, displayBits, ori, ForgeDirection.WEST ) );

				renderer.renderStandardBlock( imb, x, y, z );
			}

			bri.setTemporaryRenderIcon( null );
			renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateBottom = 0;

			return r;
		}

		renderer.overrideBlockTexture = imb.getIcon( 0, 0 );
		boolean result = renderer.renderStandardBlock( imb, x, y, z );

		renderer.overrideBlockTexture = ExtraBlockTextures.BlockSpatialPylon_dim.getIcon();
		result = renderer.renderStandardBlock( imb, x, y, z );

		renderer.overrideBlockTexture = null;
		return result;
	}

	private IIcon getBlockTextureFromSideOutside(AEBaseBlock blk, TileSpatialPylon sp, int displayBits, ForgeDirection ori, ForgeDirection dir)
	{

		if ( ori.equals( dir ) || ori.getOpposite().equals( dir ) )
			return blk.getRendererInstance().getTexture( dir );

		if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_MIDDLE )
			return ExtraBlockTextures.BlockSpatialPylonC.getIcon();

		else if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MIN )
			return ExtraBlockTextures.BlockSpatialPylonE.getIcon();

		else if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MAX )
			return ExtraBlockTextures.BlockSpatialPylonE.getIcon();

		return blk.getIcon( 0, 0 );
	}

	private IIcon getBlockTextureFromSideInside(AEBaseBlock blk, TileSpatialPylon sp, int displayBits, ForgeDirection ori, ForgeDirection dir)
	{
		boolean good = (displayBits & sp.DISPLAY_ENABLED) == sp.DISPLAY_ENABLED;

		if ( ori.equals( dir ) || ori.getOpposite().equals( dir ) )
			return good ? ExtraBlockTextures.BlockSpatialPylon_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylon_red.getIcon();

		if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_MIDDLE )
			return good ? ExtraBlockTextures.BlockSpatialPylonC_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylonC_red.getIcon();

		else if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MIN )
			return good ? ExtraBlockTextures.BlockSpatialPylonE_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylonE_red.getIcon();

		else if ( (displayBits & sp.DISPLAY_MIDDLE) == sp.DISPLAY_END_MAX )
			return good ? ExtraBlockTextures.BlockSpatialPylonE_dim.getIcon() : ExtraBlockTextures.BlockSpatialPylonE_red.getIcon();

		return blk.getIcon( 0, 0 );
	}
}
