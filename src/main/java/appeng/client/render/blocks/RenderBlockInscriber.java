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
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockInscriber;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.AELog;
import appeng.recipes.handlers.Inscribe.InscriberRecipe;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileInscriber;
import appeng.util.Platform;


public class RenderBlockInscriber extends BaseBlockRender
{

	public RenderBlockInscriber()
	{
		super( true, 30 );
	}

	@Override
	public void renderInventory( AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj )
	{
		Tessellator tess = Tessellator.instance;

		renderer.renderAllFaces = true;
		this.setInvRenderBounds( renderer, 6, 1, 0, 10, 15, 2 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		// sides...
		this.setInvRenderBounds( renderer, 3, 1, 0, 13, 15, 3 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 1, 0, 3, 15, 16 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 13, 1, 0, 16, 15, 16 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 1, 0, 1, 15, 2, 15 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 1, 14, 1, 15, 16, 15 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockInscriberInside.getIcon() );

		// press
		this.setInvRenderBounds( renderer, 3, 2, 3, 13, 3, 13 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 3, 13, 3, 13, 15, 13 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, tess, 0xffffff, renderer );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.renderAllFaces = false;
		// blk.getRendererInstance().setTemporaryRenderIcon( null );

	}

	@Override
	public boolean renderInWorld( AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer )
	{
		this.preRenderInWorld( block, world, x, y, z, renderer );

		BlockInscriber blk = (BlockInscriber) block;

		IOrientable te = this.getOrientable( block, world, x, y, z );
		if( te == null )
			return false;

		ForgeDirection fdy = te.getUp();
		ForgeDirection fdz = te.getForward();
		ForgeDirection fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.renderAllFaces = true;

		// sides...
		this.renderBlockBounds( renderer, 3, 1, 0, 13, 15, 3, fdx, fdy, fdz );
		boolean out = renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 0, 1, 0, 3, 15, 16, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 13, 1, 0, 16, 15, 16, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		// top bottom..
		this.renderBlockBounds( renderer, 1, 0, 1, 15, 4, 15, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 1, 12, 1, 15, 16, 15, fdx, fdy, fdz );
		out = renderer.renderStandardBlock( blk, x, y, z );

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.renderAllFaces = false;
		blk.getRendererInstance().setTemporaryRenderIcon( null );

		this.postRenderInWorld( renderer );
		return out;
	}

	@Override
	public void renderTile( AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f, RenderBlocks renderer )
	{
		TileInscriber inv = (TileInscriber) tile;

		GL11.glPushMatrix();
		this.applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDisable( GL12.GL_RESCALE_NORMAL );

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture( TextureMap.locationBlocksTexture );

		int br = tile.getWorldObj().getLightBrightnessForSkyBlocks( tile.xCoord, tile.yCoord, tile.zCoord, 0 );// << 20 | light << 4;
		int var11 = br % 65536;
		int var12 = br / 65536;
		OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11, var12 );

		float TwoPx = 2.0f / 16.0f;
		float middle = 0.5f;

		float press = 0.2f;
		float base = 0.4f;

		long absoluteProgress = 0;
		if( inv.smash )
		{
			long currentTime = System.currentTimeMillis();
			absoluteProgress = currentTime - inv.clientStart;
			if( absoluteProgress > 800 )
				inv.smash = false;
		}

		float relativeProgress = absoluteProgress % 800 / 400.0f;
		float progress = relativeProgress;

		if( progress > 1.0f )
			progress = 1.0f - ( progress - 1.0f );
		press -= progress / 5.0f;

		IIcon ic = ExtraBlockTextures.BlockInscriberInside.getIcon();
		tess.startDrawingQuads();

		middle += 0.02f;
		tess.addVertexWithUV( TwoPx, middle + press, TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + press, TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 13 ) );
		tess.addVertexWithUV( TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 13 ) );

		tess.addVertexWithUV( TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + base, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) );
		tess.addVertexWithUV( TwoPx, middle + base, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) );

		middle -= 2.0f * 0.02f;
		tess.addVertexWithUV( 1.0 - TwoPx, middle - press, TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( TwoPx, middle - press, TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 2 ) );
		tess.addVertexWithUV( TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 13 ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 13 ) );

		tess.addVertexWithUV( 1.0 - TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( TwoPx, middle - press, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 ) );
		tess.addVertexWithUV( TwoPx, middle - base, 1.0 - TwoPx, ic.getInterpolatedU( 14 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) );
		tess.addVertexWithUV( 1.0 - TwoPx, middle + -base, 1.0 - TwoPx, ic.getInterpolatedU( 2 ), ic.getInterpolatedV( 3 - 16 * ( press - base ) ) );

		tess.draw();

		GL11.glPopMatrix();

		int items = 0;
		if( inv.getStackInSlot( 0 ) != null )
			items++;
		if( inv.getStackInSlot( 1 ) != null )
			items++;
		if( inv.getStackInSlot( 2 ) != null )
			items++;

		if( relativeProgress > 1.0f || items == 0 )
		{
			ItemStack is = inv.getStackInSlot( 3 );

			if( is == null )
			{
				InscriberRecipe ir = inv.getTask();
				if( ir != null )
					is = ir.output.copy();
			}

			this.renderItem( is, 0.0f, block, tile, tess, x, y, z, f, renderer );
		}
		else
		{
			this.renderItem( inv.getStackInSlot( 0 ), press, block, tile, tess, x, y, z, f, renderer );
			this.renderItem( inv.getStackInSlot( 1 ), -press, block, tile, tess, x, y, z, f, renderer );
			this.renderItem( inv.getStackInSlot( 2 ), 0.0f, block, tile, tess, x, y, z, f, renderer );
		}
	}

	public void renderItem( ItemStack sis, float o, AEBaseBlock block, AEBaseTile tile, Tessellator tess, double x, double y, double z, float f, RenderBlocks renderer )
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

				Block blk = Block.getBlockFromItem( sis.getItem() );
				if( sis.getItemSpriteNumber() == 0 && block != null && RenderBlocks.renderItemIn3d( blk.getRenderType() ) )
				{
					GL11.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
					GL11.glRotatef( 15.0f, 0.0f, 1.0f, 0.0f );
					GL11.glRotatef( 30.0f, 0.0f, 1.0f, 0.0f );
				}

				GL11.glRotatef( 90.0f, 1, 0, 0 );

				int br = tile.getWorldObj().getLightBrightnessForSkyBlocks( tile.xCoord, tile.yCoord, tile.zCoord, 0 );// << 20 | light << 4;
				int var11 = br % 65536;
				int var12 = br / 65536;
				OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, var11, var12 );

				GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

				GL11.glDisable( GL11.GL_LIGHTING );
				GL11.glDisable( GL12.GL_RESCALE_NORMAL );
				tess.setColorOpaque_F( 1.0f, 1.0f, 1.0f );

				this.doRenderItem( sis, tile );
			}
			catch( Exception err )
			{
				AELog.error( err );
			}

			GL11.glPopMatrix();
		}
	}
}
