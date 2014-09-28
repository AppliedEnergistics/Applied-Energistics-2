package appeng.client.render;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.ISimplifiedBundle;
import appeng.core.AELog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlocksWorkaround extends RenderBlocks
{

	public boolean calculations = true;
	public EnumSet<ForgeDirection> renderFaces = EnumSet.allOf( ForgeDirection.class );
	public EnumSet<ForgeDirection> faces = EnumSet.allOf( ForgeDirection.class );

	private class LightingCache implements ISimplifiedBundle
	{

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

		public int aoXPos[];
		public int aoXNeg[];
		public int aoYPos[];
		public int aoYNeg[];
		public int aoZPos[];
		public int aoZNeg[];

		public float foXPos[];
		public float foXNeg[];
		public float foYPos[];
		public float foYNeg[];
		public float foZPos[];
		public float foZNeg[];

		public int lightHash;

		public LightingCache(LightingCache secondCSrc) {
			rXPos = secondCSrc.rXPos;
			rXNeg = secondCSrc.rXNeg;
			rYPos = secondCSrc.rYPos;
			rYNeg = secondCSrc.rYNeg;
			rZPos = secondCSrc.rZPos;
			rZNeg = secondCSrc.rZNeg;

			isAO = secondCSrc.isAO;

			bXPos = secondCSrc.bXPos;
			bXNeg = secondCSrc.bXNeg;
			bYPos = secondCSrc.bYPos;
			bYNeg = secondCSrc.bYNeg;
			bZPos = secondCSrc.bZPos;
			bZNeg = secondCSrc.bZNeg;

			aoXPos = secondCSrc.aoXPos.clone();
			aoXNeg = secondCSrc.aoXNeg.clone();
			aoYPos = secondCSrc.aoYPos.clone();
			aoYNeg = secondCSrc.aoYNeg.clone();
			aoZPos = secondCSrc.aoZPos.clone();
			aoZNeg = secondCSrc.aoZNeg.clone();

			foXPos = secondCSrc.foXPos.clone();
			foXNeg = secondCSrc.foXNeg.clone();
			foYPos = secondCSrc.foYPos.clone();
			foYNeg = secondCSrc.foYNeg.clone();
			foZPos = secondCSrc.foZPos.clone();
			foZNeg = secondCSrc.foZNeg.clone();

			lightHash = secondCSrc.lightHash;
		}

		public LightingCache() {
			rXPos = null;
			rXNeg = null;
			rYPos = null;
			rYNeg = null;
			rZPos = null;
			rZNeg = null;

			isAO = false;

			bXPos = 0;
			bXNeg = 0;
			bYPos = 0;
			bYNeg = 0;
			bZPos = 0;
			bZNeg = 0;

			aoXPos = new int[5];
			aoXNeg = new int[5];
			aoYPos = new int[5];
			aoYNeg = new int[5];
			aoZPos = new int[5];
			aoZNeg = new int[5];

			foXPos = new float[12];
			foXNeg = new float[12];
			foYPos = new float[12];
			foYNeg = new float[12];
			foZPos = new float[12];
			foZNeg = new float[12];

			lightHash = 0;
		}

	}

	private LightingCache lightState = new LightingCache();

	public boolean isFacade = false;
	public boolean useTextures = true;

	Field fBrightness;
	Field fColor;

	public int getCurrentColor()
	{
		try
		{
			if ( fColor == null )
			{
				try
				{
					fColor = Tessellator.class.getDeclaredField( "color" );
				}
				catch (Throwable t)
				{
					fColor = Tessellator.class.getDeclaredField( "field_78402_m" );
				}
				fColor.setAccessible( true );
			}
			return (Integer) fColor.get( Tessellator.instance );
		}
		catch (Throwable t)
		{
			return 0;
		}
	}

	public int getCurrentBrightness()
	{
		try
		{
			if ( fBrightness == null )
			{
				try
				{
					fBrightness = Tessellator.class.getDeclaredField( "brightness" );
				}
				catch (Throwable t)
				{
					fBrightness = Tessellator.class.getDeclaredField( "field_78401_l" );
				}
				fBrightness.setAccessible( true );
			}
			return (Integer) fBrightness.get( Tessellator.instance );
		}
		catch (Throwable t)
		{
			return 0;
		}
	}

	public void setTexture(IIcon ico)
	{
		lightState.rXPos = lightState.rXNeg = lightState.rYPos = lightState.rYNeg = lightState.rZPos = lightState.rZNeg = ico;
	}

	public void setTexture(IIcon rYNeg, IIcon rYPos, IIcon rZNeg, IIcon rZPos, IIcon rXNeg, IIcon rXPos)
	{
		lightState.rXPos = rXPos;
		lightState.rXNeg = rXNeg;
		lightState.rYPos = rYPos;
		lightState.rYNeg = rYNeg;
		lightState.rZPos = rZPos;
		lightState.rZNeg = rZNeg;
	}

	public boolean renderStandardBlockNoCalculations(Block b, int x, int y, int z)
	{
		Tessellator.instance.setBrightness( lightState.bXPos );
		restoreAO( lightState.aoXPos, lightState.foXPos );
		renderFaceXPos( b, x, y, z, useTextures ? lightState.rXPos : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.EAST.ordinal() ) );

		Tessellator.instance.setBrightness( lightState.bXNeg );
		restoreAO( lightState.aoXNeg, lightState.foXNeg );
		renderFaceXNeg( b, x, y, z, useTextures ? lightState.rXNeg : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.WEST.ordinal() ) );

		Tessellator.instance.setBrightness( lightState.bYPos );
		restoreAO( lightState.aoYPos, lightState.foYPos );
		renderFaceYPos( b, x, y, z, useTextures ? lightState.rYPos : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.UP.ordinal() ) );

		Tessellator.instance.setBrightness( lightState.bYNeg );
		restoreAO( lightState.aoYNeg, lightState.foYNeg );
		renderFaceYNeg( b, x, y, z, useTextures ? lightState.rYNeg : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.DOWN.ordinal() ) );

		Tessellator.instance.setBrightness( lightState.bZPos );
		restoreAO( lightState.aoZPos, lightState.foZPos );
		renderFaceZPos( b, x, y, z, useTextures ? lightState.rZPos : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.SOUTH.ordinal() ) );

		Tessellator.instance.setBrightness( lightState.bZNeg );
		restoreAO( lightState.aoZNeg, lightState.foZNeg );
		renderFaceZNeg( b, x, y, z, useTextures ? lightState.rZNeg : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.NORTH.ordinal() ) );

		return true;
	}

	private void restoreAO(int[] z, float[] c)
	{
		brightnessBottomLeft = z[0];
		brightnessBottomRight = z[1];
		brightnessTopLeft = z[2];
		brightnessTopRight = z[3];
		Tessellator.instance.setColorRGBA_I( z[4], (int) (opacity * 255) );

		colorRedTopLeft = c[0];
		colorGreenTopLeft = c[1];
		colorBlueTopLeft = c[2];
		colorRedBottomLeft = c[3];
		colorGreenBottomLeft = c[4];
		colorBlueBottomLeft = c[5];
		colorRedBottomRight = c[6];
		colorGreenBottomRight = c[7];
		colorBlueBottomRight = c[8];
		colorRedTopRight = c[9];
		colorGreenTopRight = c[10];
		colorBlueTopRight = c[11];
	}

	private void saveAO(int[] z, float[] c)
	{
		z[0] = brightnessBottomLeft;
		z[1] = brightnessBottomRight;
		z[2] = brightnessTopLeft;
		z[3] = brightnessTopRight;
		z[4] = getCurrentColor();

		c[0] = colorRedTopLeft;
		c[1] = colorGreenTopLeft;
		c[2] = colorBlueTopLeft;
		c[3] = colorRedBottomLeft;
		c[4] = colorGreenBottomLeft;
		c[5] = colorBlueBottomLeft;
		c[6] = colorRedBottomRight;
		c[7] = colorGreenBottomRight;
		c[8] = colorBlueBottomRight;
		c[9] = colorRedTopRight;
		c[10] = colorGreenTopRight;
		c[11] = colorBlueTopRight;
	}

	@Override
	public boolean renderStandardBlock(Block blk, int x, int y, int z)
	{
		try
		{
			if ( calculations )
			{
				lightState.lightHash = getLightingHash( blk, this.blockAccess, x, y, z );
				return super.renderStandardBlock( blk, x, y, z );
			}
			else
			{
				enableAO = lightState.isAO;
				boolean out = renderStandardBlockNoCalculations( blk, x, y, z );
				enableAO = false;
				return out;
			}
		}
		catch (Throwable t)
		{
			AELog.error( t );
			// meh
		}
		return false;
	}

	@Override
	public void renderFaceXNeg(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.WEST ) )
		{
			if ( !renderFaces.contains( ForgeDirection.WEST ) )
				return;

			if ( isFacade )
			{
				Tessellator tessellator = Tessellator.instance;

				double d3 = (double) par8Icon.getInterpolatedU( this.renderMinZ * 16.0D );
				double d4 = (double) par8Icon.getInterpolatedU( this.renderMaxZ * 16.0D );
				double d5 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				double d6 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				double d11 = par2 + this.renderMinX;
				double d12 = par4 + this.renderMinY;
				double d13 = par4 + this.renderMaxY;
				double d14 = par6 + this.renderMinZ;
				double d15 = par6 + this.renderMaxZ;

				if ( this.enableAO )
				{
					partialLightingColoring( renderMaxY, renderMaxZ );
					tessellator.addVertexWithUV( d11, d13, d15, d4, d5 );
					partialLightingColoring( renderMaxY, renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					partialLightingColoring( renderMinY, renderMinZ );
					tessellator.addVertexWithUV( d11, d12, d14, d3, d6 );
					partialLightingColoring( renderMinY, renderMaxZ );
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
				super.renderFaceXNeg( par1Block, par2, par4, par6, par8Icon );
		}
		else
		{
			lightState.isAO = enableAO;
			lightState.rXNeg = par8Icon;
			saveAO( lightState.aoXNeg, lightState.foXNeg );
			lightState.bXNeg = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceXPos(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.EAST ) )
		{
			if ( !renderFaces.contains( ForgeDirection.EAST ) )
				return;

			if ( isFacade )
			{
				Tessellator tessellator = Tessellator.instance;

				double d3 = (double) par8Icon.getInterpolatedU( 16.0D - this.renderMinZ * 16.0D );
				double d4 = (double) par8Icon.getInterpolatedU( 16.0D - this.renderMaxZ * 16.0D );
				double d5 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				double d6 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				double d11 = par2 + this.renderMaxX;
				double d12 = par4 + this.renderMinY;
				double d13 = par4 + this.renderMaxY;
				double d14 = par6 + this.renderMinZ;
				double d15 = par6 + this.renderMaxZ;

				if ( this.enableAO )
				{
					partialLightingColoring( 1.0 - renderMinY, renderMaxZ );
					tessellator.addVertexWithUV( d11, d12, d15, d4, d6 );
					partialLightingColoring( 1.0 - renderMinY, renderMinZ );
					tessellator.addVertexWithUV( d11, d12, d14, d3, d6 );
					partialLightingColoring( 1.0 - renderMaxY, renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					partialLightingColoring( 1.0 - renderMaxY, renderMaxZ );
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
				super.renderFaceXPos( par1Block, par2, par4, par6, par8Icon );
		}
		else
		{
			lightState.isAO = enableAO;
			lightState.rXPos = par8Icon;
			saveAO( lightState.aoXPos, lightState.foXPos );
			lightState.bXPos = getCurrentBrightness();
		}
	}

	private void partialLightingColoring(double u, double v)
	{
		double rA = colorRedTopLeft * u + (1.0 - u) * colorRedTopRight;
		double rB = colorRedBottomLeft * u + (1.0 - u) * colorRedBottomRight;
		float r = (float) (rA * v + rB * (1.0 - v));

		double gA = colorGreenTopLeft * u + (1.0 - u) * colorGreenTopRight;
		double gB = colorGreenBottomLeft * u + (1.0 - u) * colorGreenBottomRight;
		float g = (float) (gA * v + gB * (1.0 - v));

		double bA = colorBlueTopLeft * u + (1.0 - u) * colorBlueTopRight;
		double bB = colorBlueBottomLeft * u + (1.0 - u) * colorBlueBottomRight;
		float b = (float) (bA * v + bB * (1.0 - v));

		double highA = (brightnessTopLeft >> 16 & 255) * u + (1.0 - u) * (brightnessTopRight >> 16 & 255);
		double highB = (brightnessBottomLeft >> 16 & 255) * u + (1.0 - u) * (brightnessBottomRight >> 16 & 255);
		int high = ((int) (highA * v + highB * (1.0 - v))) & 255;

		double lowA = ((brightnessTopLeft & 255)) * u + (1.0 - u) * ((brightnessTopRight & 255));
		double lowB = ((brightnessBottomLeft & 255)) * u + (1.0 - u) * ((brightnessBottomRight & 255));
		int low = ((int) (lowA * v + lowB * (1.0 - v))) & 255;

		int out = (high << 16) | low;

		Tessellator.instance.setColorRGBA_F( r, g, b, opacity );
		Tessellator.instance.setBrightness( out );
	}

	@Override
	public void renderFaceYNeg(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.DOWN ) )
		{
			if ( !renderFaces.contains( ForgeDirection.DOWN ) )
				return;

			if ( isFacade )
			{
				Tessellator tessellator = Tessellator.instance;

				double d3 = (double) par8Icon.getInterpolatedU( this.renderMinX * 16.0D );
				double d4 = (double) par8Icon.getInterpolatedU( this.renderMaxX * 16.0D );
				double d5 = (double) par8Icon.getInterpolatedV( this.renderMinZ * 16.0D );
				double d6 = (double) par8Icon.getInterpolatedV( this.renderMaxZ * 16.0D );

				double d11 = par2 + this.renderMinX;
				double d12 = par2 + this.renderMaxX;
				double d13 = par4 + this.renderMinY;
				double d14 = par6 + this.renderMinZ;
				double d15 = par6 + this.renderMaxZ;

				if ( this.enableAO )
				{
					partialLightingColoring( 1.0 - renderMinX, renderMaxZ );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
					partialLightingColoring( 1.0 - renderMinX, renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					partialLightingColoring( 1.0 - renderMaxX, renderMinZ );
					tessellator.addVertexWithUV( d12, d13, d14, d4, d5 );
					partialLightingColoring( 1.0 - renderMaxX, renderMaxZ );
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
				super.renderFaceYNeg( par1Block, par2, par4, par6, par8Icon );
		}
		else
		{
			lightState.isAO = enableAO;
			lightState.rYNeg = par8Icon;
			saveAO( lightState.aoYNeg, lightState.foYNeg );
			lightState.bYNeg = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceYPos(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.UP ) )
		{
			if ( !renderFaces.contains( ForgeDirection.UP ) )
				return;

			if ( isFacade )
			{
				Tessellator tessellator = Tessellator.instance;

				double d3 = (double) par8Icon.getInterpolatedU( this.renderMinX * 16.0D );
				double d4 = (double) par8Icon.getInterpolatedU( this.renderMaxX * 16.0D );
				double d5 = (double) par8Icon.getInterpolatedV( this.renderMinZ * 16.0D );
				double d6 = (double) par8Icon.getInterpolatedV( this.renderMaxZ * 16.0D );

				double d11 = par2 + this.renderMinX;
				double d12 = par2 + this.renderMaxX;
				double d13 = par4 + this.renderMaxY;
				double d14 = par6 + this.renderMinZ;
				double d15 = par6 + this.renderMaxZ;

				if ( this.enableAO )
				{
					partialLightingColoring( this.renderMaxX, renderMaxZ );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					partialLightingColoring( this.renderMaxX, renderMinZ );
					tessellator.addVertexWithUV( d12, d13, d14, d4, d5 );
					partialLightingColoring( this.renderMinX, renderMinZ );
					tessellator.addVertexWithUV( d11, d13, d14, d3, d5 );
					partialLightingColoring( this.renderMinX, renderMaxZ );
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
				super.renderFaceYPos( par1Block, par2, par4, par6, par8Icon );
		}
		else
		{
			lightState.isAO = enableAO;
			lightState.rYPos = par8Icon;
			saveAO( lightState.aoYPos, lightState.foYPos );
			lightState.bYPos = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceZNeg(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.NORTH ) )
		{
			if ( !renderFaces.contains( ForgeDirection.NORTH ) )
				return;

			if ( isFacade )
			{
				Tessellator tessellator = Tessellator.instance;

				double d3 = (double) par8Icon.getInterpolatedU( 16.0D - this.renderMinX * 16.0D );
				double d4 = (double) par8Icon.getInterpolatedU( 16.0D - this.renderMaxX * 16.0D );
				double d5 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				double d6 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				double d11 = par2 + this.renderMinX;
				double d12 = par2 + this.renderMaxX;
				double d13 = par4 + this.renderMinY;
				double d14 = par4 + this.renderMaxY;
				double d15 = par6 + this.renderMinZ;

				if ( this.enableAO )
				{
					partialLightingColoring( renderMaxY, 1.0 - renderMinX );
					tessellator.addVertexWithUV( d11, d14, d15, d3, d5 );
					partialLightingColoring( renderMaxY, 1.0 - renderMaxX );
					tessellator.addVertexWithUV( d12, d14, d15, d4, d5 );
					partialLightingColoring( renderMinY, 1.0 - renderMaxX );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					partialLightingColoring( renderMinY, 1.0 - renderMinX );
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
				super.renderFaceZNeg( par1Block, par2, par4, par6, par8Icon );
		}
		else
		{
			lightState.isAO = enableAO;
			lightState.rZNeg = par8Icon;
			saveAO( lightState.aoZNeg, lightState.foZNeg );
			lightState.bZNeg = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceZPos(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.SOUTH ) )
		{
			if ( !renderFaces.contains( ForgeDirection.SOUTH ) )
				return;

			if ( isFacade )
			{
				Tessellator tessellator = Tessellator.instance;

				double d3 = (double) par8Icon.getInterpolatedU( this.renderMinX * 16.0D );
				double d4 = (double) par8Icon.getInterpolatedU( this.renderMaxX * 16.0D );
				double d5 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMaxY * 16.0D );
				double d6 = (double) par8Icon.getInterpolatedV( 16.0D - this.renderMinY * 16.0D );

				double d11 = par2 + this.renderMinX;
				double d12 = par2 + this.renderMaxX;
				double d13 = par4 + this.renderMinY;
				double d14 = par4 + this.renderMaxY;
				double d15 = par6 + this.renderMaxZ;

				if ( this.enableAO )
				{
					partialLightingColoring( 1.0 - renderMinX, renderMaxY );
					tessellator.addVertexWithUV( d11, d14, d15, d3, d5 );
					partialLightingColoring( 1.0 - renderMinX, renderMinY );
					tessellator.addVertexWithUV( d11, d13, d15, d3, d6 );
					partialLightingColoring( 1.0 - renderMaxX, renderMinY );
					tessellator.addVertexWithUV( d12, d13, d15, d4, d6 );
					partialLightingColoring( 1.0 - renderMaxX, renderMaxY );
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
				super.renderFaceZPos( par1Block, par2, par4, par6, par8Icon );
		}
		else
		{
			lightState.isAO = enableAO;
			lightState.rZPos = par8Icon;
			saveAO( lightState.aoZPos, lightState.foZPos );
			lightState.bZPos = getCurrentBrightness();
		}
	}

	public boolean similarLighting(Block blk, IBlockAccess w, int x, int y, int z, ISimplifiedBundle sim)
	{
		int lh = getLightingHash( blk, w, x, y, z );
		return ((LightingCache) sim).lightHash == lh;
	}

	int lightHashTmp[] = new int[27];
	public float opacity = 1.0f;

	private int getLightingHash(Block blk, IBlockAccess w, int x, int y, int z)
	{
		int o = 0;

		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				for (int k = -1; k <= 1; k++)
				{

					lightHashTmp[o++] = blk.getMixedBrightnessForBlock( this.blockAccess, x + i, y + j, z + k );
				}

		return Arrays.hashCode( lightHashTmp );
	}

	public void populate(ISimplifiedBundle sim)
	{
		lightState = new LightingCache( (LightingCache) sim );
	}

	public ISimplifiedBundle getLightingCache()
	{
		return new LightingCache( lightState );
	}
}
