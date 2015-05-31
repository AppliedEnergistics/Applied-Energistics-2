/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.IRenderHandler;


public final class SpatialSkyRender extends IRenderHandler
{

	private static final SpatialSkyRender INSTANCE = new SpatialSkyRender();

	private final Random random = new Random();
	private final int dspList;
	private long cycle = 0;

	public SpatialSkyRender()
	{
		this.dspList = GLAllocation.generateDisplayLists( 1 );
	}

	public static IRenderHandler getInstance()
	{
		return INSTANCE;
	}

	@Override
	public final void render( float partialTicks, WorldClient world, Minecraft mc )
	{
		long now = System.currentTimeMillis();
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

		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );

		GL11.glDisable( GL11.GL_FOG );
		GL11.glDisable( GL11.GL_ALPHA_TEST );
		GL11.glDisable( GL11.GL_BLEND );
		GL11.glDepthMask( false );
		GL11.glColor4f( 0.0f, 0.0f, 0.0f, 1.0f );
		Tessellator tessellator = Tessellator.instance;

		for( int i = 0; i < 6; ++i )
		{
			GL11.glPushMatrix();

			if( i == 1 )
			{
				GL11.glRotatef( 90.0F, 1.0F, 0.0F, 0.0F );
			}

			if( i == 2 )
			{
				GL11.glRotatef( -90.0F, 1.0F, 0.0F, 0.0F );
			}

			if( i == 3 )
			{
				GL11.glRotatef( 180.0F, 1.0F, 0.0F, 0.0F );
			}

			if( i == 4 )
			{
				GL11.glRotatef( 90.0F, 0.0F, 0.0F, 1.0F );
			}

			if( i == 5 )
			{
				GL11.glRotatef( -90.0F, 0.0F, 0.0F, 1.0F );
			}

			tessellator.startDrawingQuads();
			tessellator.setColorOpaque_I( 0 );
			tessellator.addVertexWithUV( -100.0D, -100.0D, -100.0D, 0.0D, 0.0D );
			tessellator.addVertexWithUV( -100.0D, -100.0D, 100.0D, 0.0D, 16.0D );
			tessellator.addVertexWithUV( 100.0D, -100.0D, 100.0D, 16.0D, 16.0D );
			tessellator.addVertexWithUV( 100.0D, -100.0D, -100.0D, 16.0D, 0.0D );
			tessellator.draw();
			GL11.glPopMatrix();
		}

		GL11.glDepthMask( true );

		if( fade > 0.0f )
		{
			GL11.glDisable( GL11.GL_FOG );
			GL11.glDisable( GL11.GL_ALPHA_TEST );
			GL11.glEnable( GL11.GL_BLEND );
			GL11.glDepthMask( false );
			GL11.glEnable( GL11.GL_FOG );
			GL11.glDisable( GL11.GL_FOG );
			GL11.glDisable( GL11.GL_ALPHA_TEST );
			GL11.glDisable( GL11.GL_TEXTURE_2D );
			OpenGlHelper.glBlendFunc( 770, 771, 1, 0 );
			RenderHelper.disableStandardItemLighting();
			GL11.glDepthMask( false );

			GL11.glColor4f( fade, fade, fade, 1.0f );
			GL11.glCallList( this.dspList );
		}

		GL11.glPopAttrib();

		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
	}

	private void renderTwinkles()
	{
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();

		for( int i = 0; i < 50; ++i )
		{
			double iX = this.random.nextFloat() * 2.0F - 1.0F;
			double iY = this.random.nextFloat() * 2.0F - 1.0F;
			double iZ = this.random.nextFloat() * 2.0F - 1.0F;
			double d3 = 0.05F + this.random.nextFloat() * 0.1F;
			double dist = iX * iX + iY * iY + iZ * iZ;

			if( dist < 1.0D && dist > 0.01D )
			{
				dist = 1.0D / Math.sqrt( dist );
				iX *= dist;
				iY *= dist;
				iZ *= dist;
				double x = iX * 100.0D;
				double y = iY * 100.0D;
				double z = iZ * 100.0D;
				double d8 = Math.atan2( iX, iZ );
				double d9 = Math.sin( d8 );
				double d10 = Math.cos( d8 );
				double d11 = Math.atan2( Math.sqrt( iX * iX + iZ * iZ ), iY );
				double d12 = Math.sin( d11 );
				double d13 = Math.cos( d11 );
				double d14 = this.random.nextDouble() * Math.PI * 2.0D;
				double d15 = Math.sin( d14 );
				double d16 = Math.cos( d14 );

				for( int j = 0; j < 4; ++j )
				{
					double d17 = 0.0D;
					double d18 = ( ( j & 2 ) - 1 ) * d3;
					double d19 = ( ( j + 1 & 2 ) - 1 ) * d3;
					double d20 = d18 * d16 - d19 * d15;
					double d21 = d19 * d16 + d18 * d15;
					double d22 = d20 * d12 + d17 * d13;
					double d23 = d17 * d12 - d20 * d13;
					double d24 = d23 * d9 - d21 * d10;
					double d25 = d21 * d9 + d23 * d10;
					tessellator.addVertex( x + d24, y + d22, z + d25 );
				}
			}
		}

		tessellator.draw();
	}
}
