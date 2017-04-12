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


import appeng.api.parts.ISimplifiedBundle;
import appeng.core.AELog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;


@SideOnly( Side.CLIENT )
public class RenderBlocksWorkaround extends RenderBlocks
{

	private final int[] lightHashTmp = new int[27];
	private boolean calculations = true;
	private EnumSet<ForgeDirection> renderFaces = EnumSet.allOf( ForgeDirection.class );
	private EnumSet<ForgeDirection> faces = EnumSet.allOf( ForgeDirection.class );
	private boolean isFacade = false;
	private boolean useTextures = true;
	private float opacity = 1.0f;
	private Field fBrightness = null;
	private Field fColor = null;
	private LightingCache lightState = new LightingCache();

	private int getCurrentColor()
	{
		try
		{
			if( this.fColor == null )
			{
				try
				{
					this.fColor = Tessellator.class.getDeclaredField( "color" );
				}
				catch( final Throwable t )
				{
					this.fColor = Tessellator.class.getDeclaredField( "field_78402_m" );
				}
				this.fColor.setAccessible( true );
			}
			return (Integer) this.fColor.get( Tessellator.instance );
		}
		catch( final Throwable t )
		{
			return 0;
		}
	}

	private int getCurrentBrightness()
	{
		try
		{
			if( this.fBrightness == null )
			{
				try
				{
					this.fBrightness = Tessellator.class.getDeclaredField( "brightness" );
				}
				catch( final Throwable t )
				{
					this.fBrightness = Tessellator.class.getDeclaredField( "field_78401_l" );
				}
				this.fBrightness.setAccessible( true );
			}
			return (Integer) this.fBrightness.get( Tessellator.instance );
		}
		catch( final Throwable t )
		{
			return 0;
		}
	}

	void setTexture( final IIcon ico )
	{
		this.lightState.rXPos = this.lightState.rXNeg = this.lightState.rYPos = this.lightState.rYNeg = this.lightState.rZPos = this.lightState.rZNeg = ico;
	}

	public void setTexture( final IIcon rYNeg, final IIcon rYPos, final IIcon rZNeg, final IIcon rZPos, final IIcon rXNeg, final IIcon rXPos )
	{
		this.lightState.rXPos = rXPos;
		this.lightState.rXNeg = rXNeg;
		this.lightState.rYPos = rYPos;
		this.lightState.rYNeg = rYNeg;
		this.lightState.rZPos = rZPos;
		this.lightState.rZNeg = rZNeg;
	}

	private boolean renderStandardBlockNoCalculations( final Block b, final int x, final int y, final int z )
	{
		Tessellator.instance.setBrightness( this.lightState.bXPos );
		this.restoreAO( this.lightState.aoXPos, this.lightState.foXPos );
		this.renderFaceXPos( b, x, y, z, this.isUseTextures() ? this.lightState.rXPos : this.getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.EAST.ordinal() ) );

		Tessellator.instance.setBrightness( this.lightState.bXNeg );
		this.restoreAO( this.lightState.aoXNeg, this.lightState.foXNeg );
		this.renderFaceXNeg( b, x, y, z, this.isUseTextures() ? this.lightState.rXNeg : this.getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.WEST.ordinal() ) );

		Tessellator.instance.setBrightness( this.lightState.bYPos );
		this.restoreAO( this.lightState.aoYPos, this.lightState.foYPos );
		this.renderFaceYPos( b, x, y, z, this.isUseTextures() ? this.lightState.rYPos : this.getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.UP.ordinal() ) );

		Tessellator.instance.setBrightness( this.lightState.bYNeg );
		this.restoreAO( this.lightState.aoYNeg, this.lightState.foYNeg );
		this.renderFaceYNeg( b, x, y, z, this.isUseTextures() ? this.lightState.rYNeg : this.getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.DOWN.ordinal() ) );

		Tessellator.instance.setBrightness( this.lightState.bZPos );
		this.restoreAO( this.lightState.aoZPos, this.lightState.foZPos );
		this.renderFaceZPos( b, x, y, z, this.isUseTextures() ? this.lightState.rZPos : this.getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.SOUTH.ordinal() ) );

		Tessellator.instance.setBrightness( this.lightState.bZNeg );
		this.restoreAO( this.lightState.aoZNeg, this.lightState.foZNeg );
		this.renderFaceZNeg( b, x, y, z, this.isUseTextures() ? this.lightState.rZNeg : this.getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.NORTH.ordinal() ) );

		return true;
	}

	private void restoreAO( final int[] z, final float[] c )
	{
		this.brightnessBottomLeft = z[0];
		this.brightnessBottomRight = z[1];
		this.brightnessTopLeft = z[2];
		this.brightnessTopRight = z[3];
		Tessellator.instance.setColorRGBA_I( z[4], (int) ( this.getOpacity() * 255 ) );

		this.colorRedTopLeft = c[0];
		this.colorGreenTopLeft = c[1];
		this.colorBlueTopLeft = c[2];
		this.colorRedBottomLeft = c[3];
		this.colorGreenBottomLeft = c[4];
		this.colorBlueBottomLeft = c[5];
		this.colorRedBottomRight = c[6];
		this.colorGreenBottomRight = c[7];
		this.colorBlueBottomRight = c[8];
		this.colorRedTopRight = c[9];
		this.colorGreenTopRight = c[10];
		this.colorBlueTopRight = c[11];
	}

	private void saveAO( final int[] z, final float[] c )
	{
		z[0] = this.brightnessBottomLeft;
		z[1] = this.brightnessBottomRight;
		z[2] = this.brightnessTopLeft;
		z[3] = this.brightnessTopRight;
		z[4] = this.getCurrentColor();

		c[0] = this.colorRedTopLeft;
		c[1] = this.colorGreenTopLeft;
		c[2] = this.colorBlueTopLeft;
		c[3] = this.colorRedBottomLeft;
		c[4] = this.colorGreenBottomLeft;
		c[5] = this.colorBlueBottomLeft;
		c[6] = this.colorRedBottomRight;
		c[7] = this.colorGreenBottomRight;
		c[8] = this.colorBlueBottomRight;
		c[9] = this.colorRedTopRight;
		c[10] = this.colorGreenTopRight;
		c[11] = this.colorBlueTopRight;
	}

	@Override
	public boolean renderStandardBlock( final Block blk, final int x, final int y, final int z )
	{
		try
		{
			if( this.isCalculations() )
			{
				this.lightState.lightHash = this.getLightingHash( blk, this.blockAccess, x, y, z );
				return super.renderStandardBlock( blk, x, y, z );
			}
			else
			{
				this.enableAO = this.lightState.isAO;
				final boolean out = this.renderStandardBlockNoCalculations( blk, x, y, z );
				this.enableAO = false;
				return out;
			}
		}
		catch( final Throwable t )
		{
			AELog.debug( t );
			// meh
		}
		return false;
	}

	@Override
	public void renderFaceYNeg( final Block par1Block, final double par2, final double par4, final double par6, final IIcon par8Icon )
	{
		if( this.getFaces().contains( ForgeDirection.DOWN ) )
		{
			if( !this.getRenderFaces().contains( ForgeDirection.DOWN ) )
			{
				return;
			}

			if( this.isFacade() )
			{
				final Tessellator tessellator = Tessellator.instance;

				final double d3 = par8Icon.getInterpolatedU( this.renderMinX * 16.0D );
				final double d4 = par8Icon.getInterpolatedU( this.renderMaxX * 16.0D );
				final double d5 = par8Icon.getInterpolatedV( this.renderMinZ * 16.0D );
				final double d6 = par8Icon.getInterpolatedV( this.renderMaxZ * 16.0D );

				final double d11 = par2 + this.renderMinX;
				final double d12 = par2 + this.renderMaxX;
				final double d13 = par4 + this.renderMinY;
				final double d14 = par6 + this.renderMinZ;
				final double d15 = par6 + this.renderMaxZ;

				if( this.enableAO )
				{
					this.partialLightingColoring( 1.0 - this.renderMinX, this.renderMaxZ );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
					this.partialLightingColoring( 1.0 - this.renderMinX, this.renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					this.partialLightingColoring( 1.0 - this.renderMaxX, this.renderMinZ );
					tessellator.addVertexWithUV( d12, d13, d14, d4, d5 );
					this.partialLightingColoring( 1.0 - this.renderMaxX, this.renderMaxZ );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
				}
				else
				{
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					tessellator.addVertexWithUV( d12, d13, d14, d4, d5 );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
				}
			}
			else
			{
				super.renderFaceYNeg( par1Block, par2, par4, par6, par8Icon );
			}
		}
		else
		{
			this.lightState.isAO = this.enableAO;
			this.lightState.rYNeg = par8Icon;
			this.saveAO( this.lightState.aoYNeg, this.lightState.foYNeg );
			this.lightState.bYNeg = this.getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceYPos( final Block par1Block, final double par2, final double par4, final double par6, final IIcon par8Icon )
	{
		if( this.getFaces().contains( ForgeDirection.UP ) )
		{
			if( !this.getRenderFaces().contains( ForgeDirection.UP ) )
			{
				return;
			}

			if( this.isFacade() )
			{
				final Tessellator tessellator = Tessellator.instance;

				final double d3 = par8Icon.getInterpolatedU( this.renderMinX * 16.0D );
				final double d4 = par8Icon.getInterpolatedU( this.renderMaxX * 16.0D );
				final double d5 = par8Icon.getInterpolatedV( this.renderMinZ * 16.0D );
				final double d6 = par8Icon.getInterpolatedV( this.renderMaxZ * 16.0D );

				final double d11 = par2 + this.renderMinX;
				final double d12 = par2 + this.renderMaxX;
				final double d13 = par4 + this.renderMaxY;
				final double d14 = par6 + this.renderMinZ;
				final double d15 = par6 + this.renderMaxZ;

				if( this.enableAO )
				{
					this.partialLightingColoring( this.renderMaxX, this.renderMaxZ );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					this.partialLightingColoring( this.renderMaxX, this.renderMinZ );
					tessellator.addVertexWithUV( d12, d13, d14, d4, d5 );
					this.partialLightingColoring( this.renderMinX, this.renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					this.partialLightingColoring( this.renderMinX, this.renderMaxZ );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
				}
				else
				{
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					tessellator.addVertexWithUV( d12, d13, d14, d4, d5 );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
				}
			}
			else
			{
				super.renderFaceYPos( par1Block, par2, par4, par6, par8Icon );
			}
		}
		else
		{
			this.lightState.isAO = this.enableAO;
			this.lightState.rYPos = par8Icon;
			this.saveAO( this.lightState.aoYPos, this.lightState.foYPos );
			this.lightState.bYPos = this.getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceZNeg( final Block par1Block, final double par2, final double par4, final double par6, final IIcon par8Icon )
	{
		if( this.getFaces().contains( ForgeDirection.NORTH ) )
		{
			if( !this.getRenderFaces().contains( ForgeDirection.NORTH ) )
			{
				return;
			}

			if( this.isFacade() )
			{
				final Tessellator tessellator = Tessellator.instance;

				final double d3 = par8Icon.getInterpolatedU( 16.0D - this.renderMinX * 16.0D );
				final double d4 = par8Icon.getInterpolatedU( 16.0D - this.renderMaxX * 16.0D );
				final double d5 = par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				final double d6 = par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				final double d11 = par2 + this.renderMinX;
				final double d12 = par2 + this.renderMaxX;
				final double d13 = par4 + this.renderMinY;
				final double d14 = par4 + this.renderMaxY;
				final double d15 = par6 + this.renderMinZ;

				if( this.enableAO )
				{
					this.partialLightingColoring( this.renderMaxY, 1.0 - this.renderMinX );
					tessellator.addVertexWithUV( d11, d14, d15, d3, d5 );
					this.partialLightingColoring( this.renderMaxY, 1.0 - this.renderMaxX );
					tessellator.addVertexWithUV( d12, d14, d15, d4, d5 );
					this.partialLightingColoring( this.renderMinY, 1.0 - this.renderMaxX );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					this.partialLightingColoring( this.renderMinY, 1.0 - this.renderMinX );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
				}
				else
				{
					tessellator.addVertexWithUV( d11, d14, d15, d3, d5 );
					tessellator.addVertexWithUV( d12, d14, d15, d4, d5 );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
				}
			}
			else
			{
				super.renderFaceZNeg( par1Block, par2, par4, par6, par8Icon );
			}
		}
		else
		{
			this.lightState.isAO = this.enableAO;
			this.lightState.rZNeg = par8Icon;
			this.saveAO( this.lightState.aoZNeg, this.lightState.foZNeg );
			this.lightState.bZNeg = this.getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceZPos( final Block par1Block, final double par2, final double par4, final double par6, final IIcon par8Icon )
	{
		if( this.getFaces().contains( ForgeDirection.SOUTH ) )
		{
			if( !this.getRenderFaces().contains( ForgeDirection.SOUTH ) )
			{
				return;
			}

			if( this.isFacade() )
			{
				final Tessellator tessellator = Tessellator.instance;

				final double d3 = par8Icon.getInterpolatedU( this.renderMinX * 16.0D );
				final double d4 = par8Icon.getInterpolatedU( this.renderMaxX * 16.0D );
				final double d5 = par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				final double d6 = par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				final double d11 = par2 + this.renderMinX;
				final double d12 = par2 + this.renderMaxX;
				final double d13 = par4 + this.renderMinY;
				final double d14 = par4 + this.renderMaxY;
				final double d15 = par6 + this.renderMaxZ;

				if( this.enableAO )
				{
					this.partialLightingColoring( 1.0 - this.renderMinX, this.renderMaxY );
					tessellator.addVertexWithUV( d11, d14, d15, d3, d5 );
					this.partialLightingColoring( 1.0 - this.renderMinX, this.renderMinY );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
					this.partialLightingColoring( 1.0 - this.renderMaxX, this.renderMinY );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					this.partialLightingColoring( 1.0 - this.renderMaxX, this.renderMaxY );
					tessellator.addVertexWithUV( d12, d14, d15, d4, d5 );
				}
				else
				{
					tessellator.addVertexWithUV( d11, d14, d15, d3, d5 );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					tessellator.addVertexWithUV( d12, d14, d15, d4, d5 );
				}
			}
			else
			{
				super.renderFaceZPos( par1Block, par2, par4, par6, par8Icon );
			}
		}
		else
		{
			this.lightState.isAO = this.enableAO;
			this.lightState.rZPos = par8Icon;
			this.saveAO( this.lightState.aoZPos, this.lightState.foZPos );
			this.lightState.bZPos = this.getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceXNeg( final Block par1Block, final double par2, final double par4, final double par6, final IIcon par8Icon )
	{
		if( this.getFaces().contains( ForgeDirection.WEST ) )
		{
			if( !this.getRenderFaces().contains( ForgeDirection.WEST ) )
			{
				return;
			}

			if( this.isFacade() )
			{
				final Tessellator tessellator = Tessellator.instance;

				final double d3 = par8Icon.getInterpolatedU( this.renderMinZ * 16.0D );
				final double d4 = par8Icon.getInterpolatedU( this.renderMaxZ * 16.0D );
				final double d5 = par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				final double d6 = par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				final double d11 = par2 + this.renderMinX;
				final double d12 = par4 + this.renderMinY;
				final double d13 = par4 + this.renderMaxY;
				final double d14 = par6 + this.renderMinZ;
				final double d15 = par6 + this.renderMaxZ;

				if( this.enableAO )
				{
					this.partialLightingColoring( this.renderMaxY, this.renderMaxZ );
					tessellator.addVertexWithUV( d11, d13, d15, d4, d5 );
					this.partialLightingColoring( this.renderMaxY, this.renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					this.partialLightingColoring( this.renderMinY, this.renderMinZ );
					tessellator.addVertexWithUV( d11, d12, d14, d3, d6 );
					this.partialLightingColoring( this.renderMinY, this.renderMaxZ );
					tessellator.addVertexWithUV( d11, d12, d15, d4, d6 );
				}
				else
				{
					tessellator.addVertexWithUV( d11, d13, d15, d4, d5 );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					tessellator.addVertexWithUV( d11, d12, d14, d3, d6 );
					tessellator.addVertexWithUV( d11, d12, d15, d4, d6 );
				}
			}
			else
			{
				super.renderFaceXNeg( par1Block, par2, par4, par6, par8Icon );
			}
		}
		else
		{
			this.lightState.isAO = this.enableAO;
			this.lightState.rXNeg = par8Icon;
			this.saveAO( this.lightState.aoXNeg, this.lightState.foXNeg );
			this.lightState.bXNeg = this.getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceXPos( final Block par1Block, final double par2, final double par4, final double par6, final IIcon par8Icon )
	{
		if( this.getFaces().contains( ForgeDirection.EAST ) )
		{
			if( !this.getRenderFaces().contains( ForgeDirection.EAST ) )
			{
				return;
			}

			if( this.isFacade() )
			{
				final Tessellator tessellator = Tessellator.instance;

				final double d3 = par8Icon.getInterpolatedU( 16.0D - this.renderMinZ * 16.0D );
				final double d4 = par8Icon.getInterpolatedU( 16.0D - this.renderMaxZ * 16.0D );
				final double d5 = par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				final double d6 = par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				final double d11 = par2 + this.renderMaxX;
				final double d12 = par4 + this.renderMinY;
				final double d13 = par4 + this.renderMaxY;
				final double d14 = par6 + this.renderMinZ;
				final double d15 = par6 + this.renderMaxZ;

				if( this.enableAO )
				{
					this.partialLightingColoring( 1.0 - this.renderMinY, this.renderMaxZ );
					tessellator.addVertexWithUV( d11, d12, d15, d4, d6 );
					this.partialLightingColoring( 1.0 - this.renderMinY, this.renderMinZ );
					tessellator.addVertexWithUV( d11, d12, d14, d3, d6 );
					this.partialLightingColoring( 1.0 - this.renderMaxY, this.renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					this.partialLightingColoring( 1.0 - this.renderMaxY, this.renderMaxZ );
					tessellator.addVertexWithUV( d11, d13, d15, d4, d5 );
				}
				else
				{
					tessellator.addVertexWithUV( d11, d12, d15, d4, d6 );
					tessellator.addVertexWithUV( d11, d12, d14, d3, d6 );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					tessellator.addVertexWithUV( d11, d13, d15, d4, d5 );
				}
			}
			else
			{
				super.renderFaceXPos( par1Block, par2, par4, par6, par8Icon );
			}
		}
		else
		{
			this.lightState.isAO = this.enableAO;
			this.lightState.rXPos = par8Icon;
			this.saveAO( this.lightState.aoXPos, this.lightState.foXPos );
			this.lightState.bXPos = this.getCurrentBrightness();
		}
	}

	private void partialLightingColoring( final double u, final double v )
	{
		final double rA = this.colorRedTopLeft * u + ( 1.0 - u ) * this.colorRedTopRight;
		final double rB = this.colorRedBottomLeft * u + ( 1.0 - u ) * this.colorRedBottomRight;
		final float r = (float) ( rA * v + rB * ( 1.0 - v ) );

		final double gA = this.colorGreenTopLeft * u + ( 1.0 - u ) * this.colorGreenTopRight;
		final double gB = this.colorGreenBottomLeft * u + ( 1.0 - u ) * this.colorGreenBottomRight;
		final float g = (float) ( gA * v + gB * ( 1.0 - v ) );

		final double bA = this.colorBlueTopLeft * u + ( 1.0 - u ) * this.colorBlueTopRight;
		final double bB = this.colorBlueBottomLeft * u + ( 1.0 - u ) * this.colorBlueBottomRight;
		final float b = (float) ( bA * v + bB * ( 1.0 - v ) );

		final double highA = ( this.brightnessTopLeft >> 16 & 255 ) * u + ( 1.0 - u ) * ( this.brightnessTopRight >> 16 & 255 );
		final double highB = ( this.brightnessBottomLeft >> 16 & 255 ) * u + ( 1.0 - u ) * ( this.brightnessBottomRight >> 16 & 255 );
		final int high = ( (int) ( highA * v + highB * ( 1.0 - v ) ) ) & 255;

		final double lowA = ( ( this.brightnessTopLeft & 255 ) ) * u + ( 1.0 - u ) * ( ( this.brightnessTopRight & 255 ) );
		final double lowB = ( ( this.brightnessBottomLeft & 255 ) ) * u + ( 1.0 - u ) * ( ( this.brightnessBottomRight & 255 ) );
		final int low = ( (int) ( lowA * v + lowB * ( 1.0 - v ) ) ) & 255;

		final int out = ( high << 16 ) | low;

		Tessellator.instance.setColorRGBA_F( r, g, b, this.getOpacity() );
		Tessellator.instance.setBrightness( out );
	}

	public boolean similarLighting( final Block blk, final IBlockAccess w, final int x, final int y, final int z, final ISimplifiedBundle sim )
	{
		final int lh = this.getLightingHash( blk, w, x, y, z );
		return ( (LightingCache) sim ).lightHash == lh;
	}

	private int getLightingHash( final Block blk, final IBlockAccess w, final int x, final int y, final int z )
	{
		int o = 0;

		for( int i = -1; i <= 1; i++ )
		{
			for( int j = -1; j <= 1; j++ )
			{
				for( int k = -1; k <= 1; k++ )
				{

					this.lightHashTmp[o] = blk.getMixedBrightnessForBlock( this.blockAccess, x + i, y + j, z + k );
					o++;
				}
			}
		}

		return Arrays.hashCode( this.lightHashTmp );
	}

	public void populate( final ISimplifiedBundle sim )
	{
		this.lightState = new LightingCache( (LightingCache) sim );
	}

	public ISimplifiedBundle getLightingCache()
	{
		return new LightingCache( this.lightState );
	}

	Set<ForgeDirection> getFaces()
	{
		return this.faces;
	}

	public void setFaces( final EnumSet<ForgeDirection> faces )
	{
		this.faces = faces;
	}

	private boolean isCalculations()
	{
		return this.calculations;
	}

	public void setCalculations( final boolean calculations )
	{
		this.calculations = calculations;
	}

	private boolean isUseTextures()
	{
		return this.useTextures;
	}

	void setUseTextures( final boolean useTextures )
	{
		this.useTextures = useTextures;
	}

	private boolean isFacade()
	{
		return this.isFacade;
	}

	public void setFacade( final boolean isFacade )
	{
		this.isFacade = isFacade;
	}

	private float getOpacity()
	{
		return this.opacity;
	}

	public void setOpacity( final float opacity )
	{
		this.opacity = opacity;
	}

	private EnumSet<ForgeDirection> getRenderFaces()
	{
		return this.renderFaces;
	}

	void setRenderFaces( final EnumSet<ForgeDirection> renderFaces )
	{
		this.renderFaces = renderFaces;
	}

	private static class LightingCache implements ISimplifiedBundle
	{
		public final int[] aoXPos;
		public final int[] aoXNeg;
		public final int[] aoYPos;
		public final int[] aoYNeg;
		public final int[] aoZPos;
		public final int[] aoZNeg;
		public final float[] foXPos;
		public final float[] foXNeg;
		public final float[] foYPos;
		public final float[] foYNeg;
		public final float[] foZPos;
		public final float[] foZNeg;
		public IIcon rXPos;
		public IIcon rXNeg;
		public IIcon rYPos;
		public IIcon rYNeg;
		public IIcon rZPos;
		public IIcon rZNeg;
		public boolean isAO;
		public int bXPos;
		public int bXNeg;
		public int bYPos;
		public int bYNeg;
		public int bZPos;
		public int bZNeg;
		public int lightHash;

		public LightingCache( final LightingCache secondCSrc )
		{
			this.rXPos = secondCSrc.rXPos;
			this.rXNeg = secondCSrc.rXNeg;
			this.rYPos = secondCSrc.rYPos;
			this.rYNeg = secondCSrc.rYNeg;
			this.rZPos = secondCSrc.rZPos;
			this.rZNeg = secondCSrc.rZNeg;

			this.isAO = secondCSrc.isAO;

			this.bXPos = secondCSrc.bXPos;
			this.bXNeg = secondCSrc.bXNeg;
			this.bYPos = secondCSrc.bYPos;
			this.bYNeg = secondCSrc.bYNeg;
			this.bZPos = secondCSrc.bZPos;
			this.bZNeg = secondCSrc.bZNeg;

			this.aoXPos = secondCSrc.aoXPos.clone();
			this.aoXNeg = secondCSrc.aoXNeg.clone();
			this.aoYPos = secondCSrc.aoYPos.clone();
			this.aoYNeg = secondCSrc.aoYNeg.clone();
			this.aoZPos = secondCSrc.aoZPos.clone();
			this.aoZNeg = secondCSrc.aoZNeg.clone();

			this.foXPos = secondCSrc.foXPos.clone();
			this.foXNeg = secondCSrc.foXNeg.clone();
			this.foYPos = secondCSrc.foYPos.clone();
			this.foYNeg = secondCSrc.foYNeg.clone();
			this.foZPos = secondCSrc.foZPos.clone();
			this.foZNeg = secondCSrc.foZNeg.clone();

			this.lightHash = secondCSrc.lightHash;
		}

		public LightingCache()
		{
			this.rXPos = null;
			this.rXNeg = null;
			this.rYPos = null;
			this.rYNeg = null;
			this.rZPos = null;
			this.rZNeg = null;

			this.isAO = false;

			this.bXPos = 0;
			this.bXNeg = 0;
			this.bYPos = 0;
			this.bYNeg = 0;
			this.bZPos = 0;
			this.bZNeg = 0;

			this.aoXPos = new int[5];
			this.aoXNeg = new int[5];
			this.aoYPos = new int[5];
			this.aoYNeg = new int[5];
			this.aoZPos = new int[5];
			this.aoZNeg = new int[5];

			this.foXPos = new float[12];
			this.foXNeg = new float[12];
			this.foYPos = new float[12];
			this.foYNeg = new float[12];
			this.foZPos = new float[12];
			this.foZNeg = new float[12];

			this.lightHash = 0;
		}
	}
}
