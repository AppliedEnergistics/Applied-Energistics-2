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

package appeng.client.render;


import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.client.IRenderHandler;


//TODO https://github.com/MinecraftForge/MinecraftForge/pull/6537
public class SpatialSkyRender implements IRenderHandler
{

	private static final SpatialSkyRender INSTANCE = new SpatialSkyRender();

	private final Random random = new Random();
	private final int dspList;
	private long cycle = 0;

	public SpatialSkyRender()
	{
		this.dspList = GL11.glGenLists( 1 );
	}

	//FIXME, Do not use this until the above PR is merged.
	//	public static IRenderHandler getInstance()
	//	{
	//		return INSTANCE;
	//	}

	@Override
	public void render( final int ticks, final float partialTicks, final ClientWorld world, final Minecraft mc )
	{

		final long now = System.currentTimeMillis();
		if( now - this.cycle > 2000 )
		{
			this.cycle = now;
			GL11.glNewList( this.dspList, GL11.GL_COMPILE );
			this.renderTwinkles();
			GL11.glEndList();
		}

		float fade = now - this.cycle;
		fade /= 1000;
		fade = 0.15f * ( 1.0f - Math.abs( ( fade - 1.0f ) * ( fade - 1.0f ) ) );

		GlStateManager.disableFog();
		GlStateManager.disableAlphaTest();
		RenderSystem.disableBlend();
		GlStateManager.depthMask( false );
		RenderSystem.color4f( 0.0f, 0.0f, 0.0f, 1.0f );
		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder VertexBuffer = tessellator.getBuffer();

		// This renders a skybox around the player at a far, fixed distance from them.
		// The skybox is pitch black and untextured
		for( int i = 0; i < 6; ++i )
		{
			GlStateManager.pushMatrix();

			if( i == 1 )
			{
				GlStateManager.rotatef( 90.0F, 1.0F, 0.0F, 0.0F );
			}

			if( i == 2 )
			{
				GlStateManager.rotatef( -90.0F, 1.0F, 0.0F, 0.0F );
			}

			if( i == 3 )
			{
				GlStateManager.rotatef( 180.0F, 1.0F, 0.0F, 0.0F );
			}

			if( i == 4 )
			{
				GlStateManager.rotatef( 90.0F, 0.0F, 0.0F, 1.0F );
			}

			if( i == 5 )
			{
				GlStateManager.rotatef( -90.0F, 0.0F, 0.0F, 1.0F );
			}

			GlStateManager.disableTexture();
			VertexBuffer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION );
			VertexBuffer.pos( -100.0D, -100.0D, -100.0D ).endVertex();
			VertexBuffer.pos( -100.0D, -100.0D, 100.0D ).endVertex();
			VertexBuffer.pos( 100.0D, -100.0D, 100.0D ).endVertex();
			VertexBuffer.pos( 100.0D, -100.0D, -100.0D ).endVertex();
			tessellator.draw();
			RenderSystem.enableTexture();
			GlStateManager.popMatrix();
		}

		GlStateManager.depthMask( true );

		if( fade > 0.0f )
		{
			GlStateManager.disableFog();
			GlStateManager.disableAlphaTest();
			RenderSystem.enableBlend();
			GlStateManager.disableTexture();
			GlStateManager.depthMask( false );
			GlStateManager.blendFuncSeparate( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0 );

			RenderHelper.disableStandardItemLighting();

			RenderSystem.color4f( fade, fade, fade, 1.0f );
			GL11.glCallList( this.dspList );
		}

		GlStateManager.depthMask( true );
		RenderSystem.enableBlend();
		GlStateManager.enableAlphaTest();
		RenderSystem.enableTexture();
		GlStateManager.enableFog();

		RenderSystem.color4f( 1.0f, 1.0f, 1.0f, 1.0f );
	}

	private void renderTwinkles()
	{
		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder VertexBuffer = tessellator.getBuffer();
		VertexBuffer.begin( GL11.GL_QUADS, DefaultVertexFormats.POSITION );

		for( int i = 0; i < 50; ++i )
		{
			double iX = this.random.nextFloat() * 2.0F - 1.0F;
			double iY = this.random.nextFloat() * 2.0F - 1.0F;
			double iZ = this.random.nextFloat() * 2.0F - 1.0F;
			final double d3 = 0.05F + this.random.nextFloat() * 0.1F;
			double dist = iX * iX + iY * iY + iZ * iZ;

			if( dist < 1.0D && dist > 0.01D )
			{
				dist = 1.0D / Math.sqrt( dist );
				iX *= dist;
				iY *= dist;
				iZ *= dist;
				final double x = iX * 100.0D;
				final double y = iY * 100.0D;
				final double z = iZ * 100.0D;
				final double d8 = Math.atan2( iX, iZ );
				final double d9 = Math.sin( d8 );
				final double d10 = Math.cos( d8 );
				final double d11 = Math.atan2( Math.sqrt( iX * iX + iZ * iZ ), iY );
				final double d12 = Math.sin( d11 );
				final double d13 = Math.cos( d11 );
				final double d14 = this.random.nextDouble() * Math.PI * 2.0D;
				final double d15 = Math.sin( d14 );
				final double d16 = Math.cos( d14 );

				for( int j = 0; j < 4; ++j )
				{
					final double d17 = 0.0D;
					final double d18 = ( ( j & 2 ) - 1 ) * d3;
					final double d19 = ( ( j + 1 & 2 ) - 1 ) * d3;
					final double d20 = d18 * d16 - d19 * d15;
					final double d21 = d19 * d16 + d18 * d15;
					final double d22 = d20 * d12 + d17 * d13;
					final double d23 = d17 * d12 - d20 * d13;
					final double d24 = d23 * d9 - d21 * d10;
					final double d25 = d21 * d9 + d23 * d10;
					VertexBuffer.pos( x + d24, y + d22, z + d25 ).endVertex();
				}
			}
		}

		tessellator.draw();
	}
}
