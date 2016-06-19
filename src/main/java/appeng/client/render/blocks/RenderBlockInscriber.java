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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.features.IInscriberRecipe;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IAESprite;
import appeng.api.util.IOrientable;
import appeng.api.util.ModelGenerator;
import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockInscriber;
import appeng.client.ItemRenderType;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AELog;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class RenderBlockInscriber extends BaseBlockRender<BlockInscriber, TileInscriber>
{

	public RenderBlockInscriber()
	{
		super( true, 30 );
	}

	@Override
	public void renderInventory( final BlockInscriber blk, final ItemStack is, final ModelGenerator renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.setRenderAllFaces( true );
		this.setInvRenderBounds( renderer, 6, 1, 0, 10, 15, 2 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		// sides...
		this.setInvRenderBounds( renderer, 3, 1, 0, 13, 15, 3 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 1, 0, 3, 15, 16 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 13, 1, 0, 16, 15, 16 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 1, 0, 1, 15, 2, 15 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 1, 14, 1, 15, 16, 15 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockInscriberInside.getIcon() );

		// press
		this.setInvRenderBounds( renderer, 3, 2, 3, 13, 3, 13 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 3, 13, 3, 13, 15, 13 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.setRenderAllFaces( false );
		// blk.getRendererInstance().setTemporaryRenderIcon( null );

	}

	@Override
	public boolean renderInWorld( final BlockInscriber block, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		this.preRenderInWorld( block, world, pos, renderer );

		final IOrientable te = this.getOrientable( block, world, pos );
		if( te == null )
		{
			return false;
		}

		final EnumFacing fdy = te.getUp();
		final EnumFacing fdz = te.getForward();
		final EnumFacing fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.setRenderAllFaces( true );

		// sides...
		this.renderBlockBounds( renderer, 3, 1, 0, 13, 15, 3, fdx, fdy, fdz );
		boolean out = renderer.renderStandardBlock( block, pos );

		this.renderBlockBounds( renderer, 0, 1, 0, 3, 15, 16, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		this.renderBlockBounds( renderer, 13, 1, 0, 16, 15, 16, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		// top bottom..
		this.renderBlockBounds( renderer, 1, 0, 1, 15, 4, 15, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		this.renderBlockBounds( renderer, 1, 12, 1, 15, 16, 15, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( block, pos );

		block.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.setRenderAllFaces( false );
		block.getRendererInstance().setTemporaryRenderIcon( null );

		this.postRenderInWorld( renderer );
		return out;
	}

	@Override
	public void renderTile( final BlockInscriber block, final TileInscriber tile, final VertexBuffer tess, final double x, final double y, final double z, final float f, final ModelGenerator renderer )
	{
		GL11.glPushMatrix();
		this.applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		GL11.glCullFace( GL11.GL_FRONT );

		final Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture( TextureMap.LOCATION_BLOCKS_TEXTURE );

		// << 20 | light << 4;
		final int br = tile.getWorld().getCombinedLight( tile.getPos(), 0 );
		final int var11 = br % 65536;
		final int var12 = br / 65536;

		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11, var12 );

		long absoluteProgress = 0;

		if( tile.isSmash() )
		{
			final long currentTime = System.currentTimeMillis();
			absoluteProgress = currentTime - tile.getClientStart();
			if( absoluteProgress > 800 )
			{
				tile.setSmash( false );
			}
		}

		final float relativeProgress = absoluteProgress % 800 / 400.0f;
		float progress = relativeProgress;

		if( progress > 1.0f )
		{
			progress = 1.0f - ( progress - 1.0f );
		}
		float press = 0.2f;
		press -= progress / 5.0f;

		final IAESprite ic = ExtraBlockTextures.BlockInscriberInside.getIcon();
		tess.begin( GL11.GL_QUADS, DefaultVertexFormats.ITEM );

		float middle = 0.5f;
		middle += 0.02f;
		final float TwoPx = 2.0f / 16.0f;
		tess.pos( TwoPx, middle + press, TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 2 ) ).endVertex();
		tess.pos( 1.0 - TwoPx, middle + press, TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 2 ) ).endVertex();
		tess.pos( 1.0 - TwoPx, middle + press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 13 ) ).endVertex();
		tess.pos( TwoPx, middle + press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 13 ) ).endVertex();

		tess.pos( TwoPx, middle + press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 ) ).endVertex();
		tess.pos( 1.0 - TwoPx, middle + press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 ) ).endVertex();
		final float base = 0.4f;
		tess.pos( 1.0 - TwoPx, middle + base, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();
		tess.pos( TwoPx, middle + base, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();

		middle -= 2.0f * 0.02f;
		tess.pos( 1.0 - TwoPx, middle - press, TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 2 ) ).endVertex();
		tess.pos( TwoPx, middle - press, TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 2 ) ).endVertex();
		tess.pos( TwoPx, middle - press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 13 ) ).endVertex();
		tess.pos( 1.0 - TwoPx, middle - press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 13 ) ).endVertex();

		tess.pos( 1.0 - TwoPx, middle - press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 ) ).endVertex();
		tess.pos( TwoPx, middle - press, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 ) ).endVertex();
		tess.pos( TwoPx, middle - base, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();
		tess.pos( 1.0 - TwoPx, middle + -base, 1.0 - TwoPx ).tex( ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) ).endVertex();

		Tessellator.getInstance().draw();

		GL11.glCullFace( GL11.GL_BACK );
		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		GL11.glPopMatrix();

		int items = 0;
		if( tile.getStackInSlot( 0 ) != null )
		{
			items++;
		}
		if( tile.getStackInSlot( 1 ) != null )
		{
			items++;
		}
		if( tile.getStackInSlot( 2 ) != null )
		{
			items++;
		}

		if( relativeProgress > 1.0f || items == 0 )
		{
			ItemStack is = tile.getStackInSlot( 3 );

			if( is == null )
			{
				final IInscriberRecipe ir = tile.getTask();
				if( ir != null )
				{
					is = ir.getOutput().copy();
				}
			}

			this.renderItem( is, 0.0f, block, tile, tess, x, y, z, f, renderer );
		}
		else
		{
			this.renderItem( tile.getStackInSlot( 0 ), press, block, tile, tess, x, y, z, f, renderer );
			this.renderItem( tile.getStackInSlot( 1 ), -press, block, tile, tess, x, y, z, f, renderer );
			this.renderItem( tile.getStackInSlot( 2 ), 0.0f, block, tile, tess, x, y, z, f, renderer );
		}
	}

	private void renderItem( ItemStack sis, final float o, final AEBaseBlock block, final AEBaseTile tile, final VertexBuffer tess, final double x, final double y, final double z, final float f, final ModelGenerator renderer )
	{
		if( sis != null )
		{
			sis = sis.copy();
			GL11.glPushMatrix();
			this.applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

			try
			{
				GL11.glTranslatef( 0.5f, 0.5f + o, 0.5f );
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

				GL11.glRotatef( 90.0f, 1, 0, 0 );

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
