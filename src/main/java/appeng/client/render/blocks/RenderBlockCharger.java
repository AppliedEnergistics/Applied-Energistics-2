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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.api.util.ModelGenerator;
import appeng.block.misc.BlockCharger;
import appeng.client.ItemRenderType;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AELog;
import appeng.tile.misc.TileCharger;
import appeng.util.Platform;


public class RenderBlockCharger extends BaseBlockRender<BlockCharger, TileCharger>
{

	public RenderBlockCharger()
	{
		super( true, 30 );
	}

	@Override
	public void renderInventory( final BlockCharger blk, final ItemStack is, final ModelGenerator renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.setRenderAllFaces( true );
		this.setInvRenderBounds( renderer, 6, 1, 0, 10, 15, 2 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcons( ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null, null );

		this.setInvRenderBounds( renderer, 2, 0, 2, 14, 3, 14 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 3, 3, 3, 13, 4, 13 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		blk.getRendererInstance().setTemporaryRenderIcons( null, ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null );

		this.setInvRenderBounds( renderer, 2, 13, 2, 14, 16, 14 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 3, 12, 3, 13, 13, 13 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		renderer.setRenderAllFaces( false );
		blk.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public boolean renderInWorld( final BlockCharger block, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		this.preRenderInWorld( block, world, pos, renderer );

		final IOrientable te = this.getOrientable( block, world, pos );

		final EnumFacing fdy = te.getUp();
		final EnumFacing fdz = te.getForward();
		final EnumFacing fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.setRenderAllFaces( true );
		this.renderBlockBounds( renderer, 6, 1, 0, 10, 15, 2, fdx, fdy, fdz );
		boolean out = renderer.renderStandardBlock( block, pos );

		block.getRendererInstance().setTemporaryRenderIcons( ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null, null );

		this.renderBlockBounds( renderer, 2, 0, 2, 14, 3, 14, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		this.renderBlockBounds( renderer, 3, 3, 3, 13, 4, 13, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		block.getRendererInstance().setTemporaryRenderIcon( null );

		block.getRendererInstance().setTemporaryRenderIcons( null, ExtraBlockTextures.BlockChargerInside.getIcon(), null, null, null, null );

		this.renderBlockBounds( renderer, 2, 13, 2, 14, 16, 14, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		this.renderBlockBounds( renderer, 3, 12, 3, 13, 13, 13, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		renderer.setRenderAllFaces( false );
		block.getRendererInstance().setTemporaryRenderIcon( null );

		this.postRenderInWorld( renderer );
		return out;
	}

	@Override
	public void renderTile( final BlockCharger block, final TileCharger tile, final WorldRenderer tess, final double x, final double y, final double z, final float f, final ModelGenerator renderer )
	{
		final ItemStack sis = tile.getStackInSlot( 0 );

		if( sis != null )
		{
			GL11.glPushMatrix();
			this.applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

			try
			{
				GL11.glTranslatef( 0.5f, 0.35f, 0.5f );
				GL11.glScalef( 1.0f / 1.1f, 1.0f / 1.1f, 1.0f / 1.1f );
				GL11.glScalef( 1.0f, 1.0f, 1.0f );

				final Block blk = Block.getBlockFromItem( sis.getItem() );
				/*
				 * if( sis.getItemSpriteNumber() == 0 && block != null && IRenderHelper.renderItemIn3d(
				 * blk.getRenderType() ) )
				 * {
				 * GL11.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
				 * GL11.glRotatef( 15.0f, 0.0f, 1.0f, 0.0f );
				 * GL11.glRotatef( 30.0f, 0.0f, 1.0f, 0.0f );
				 * }
				 */

				// << 20 | light << 4;
				final int br = tile.getWorld().getCombinedLight( tile.getPos(), 0 );
				final int var11 = br % 65536;
				final int var12 = br / 65536;

				OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11, var12 );

				GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL12.GL_RESCALE_NORMAL );

				this.doRenderItem( sis, tile );
			}
			catch( final Exception err )
			{
				AELog.debug( err );
			}

			GL11.glPopMatrix();
		}
	}
}
