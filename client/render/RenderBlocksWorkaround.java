package appeng.client.render;

import java.lang.reflect.Field;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.core.AELog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBlocksWorkaround extends RenderBlocks
{

	public boolean calculations = true;
	public EnumSet<ForgeDirection> faces = EnumSet.allOf( ForgeDirection.class );

	private IIcon rXPos = null;
	private IIcon rXNeg = null;
	private IIcon rYPos = null;
	private IIcon rYNeg = null;
	private IIcon rZPos = null;
	private IIcon rZNeg = null;

	private boolean isAO = false;

	private int bXPos = 0;
	private int bXNeg = 0;
	private int bYPos = 0;
	private int bYNeg = 0;
	private int bZPos = 0;
	private int bZNeg = 0;

	private int aoXPos[] = new int[5];
	private int aoXNeg[] = new int[5];
	private int aoYPos[] = new int[5];
	private int aoYNeg[] = new int[5];
	private int aoZPos[] = new int[5];
	private int aoZNeg[] = new int[5];

	private float foXPos[] = new float[12];
	private float foXNeg[] = new float[12];
	private float foYPos[] = new float[12];
	private float foYNeg[] = new float[12];
	private float foZPos[] = new float[12];
	private float foZNeg[] = new float[12];

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
		rXPos = rXNeg = rYPos = rYNeg = rZPos = rZNeg = ico;
	}

	public void setTexture(IIcon rYNeg, IIcon rYPos, IIcon rZNeg, IIcon rZPos, IIcon rXNeg, IIcon rXPos)
	{
		this.rXPos = rXPos;
		this.rXNeg = rXNeg;
		this.rYPos = rYPos;
		this.rYNeg = rYNeg;
		this.rZPos = rZPos;
		this.rZNeg = rZNeg;
	}

	public boolean renderStandardBlockNoCalculations(Block b, int x, int y, int z)
	{
		Tessellator.instance.setBrightness( bXPos );
		restoreAO( aoXPos, foXPos );
		renderFaceXPos( b, x, y, z, useTextures ? rXPos : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.EAST.ordinal() ) );

		Tessellator.instance.setBrightness( bXNeg );
		restoreAO( aoXNeg, foXNeg );
		renderFaceXNeg( b, x, y, z, useTextures ? rXNeg : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.WEST.ordinal() ) );

		Tessellator.instance.setBrightness( bYPos );
		restoreAO( aoYPos, foYPos );
		renderFaceYPos( b, x, y, z, useTextures ? rYPos : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.UP.ordinal() ) );

		Tessellator.instance.setBrightness( bYNeg );
		restoreAO( aoYNeg, foYNeg );
		renderFaceYNeg( b, x, y, z, useTextures ? rYNeg : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.DOWN.ordinal() ) );

		Tessellator.instance.setBrightness( bZPos );
		restoreAO( aoZPos, foZPos );
		renderFaceZPos( b, x, y, z, useTextures ? rZPos : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.SOUTH.ordinal() ) );

		Tessellator.instance.setBrightness( bZNeg );
		restoreAO( aoZNeg, foZNeg );
		renderFaceZNeg( b, x, y, z, useTextures ? rZNeg : getBlockIcon( b, this.blockAccess, x, y, z, ForgeDirection.NORTH.ordinal() ) );

		return true;
	}

	private void restoreAO(int[] z, float[] c)
	{
		brightnessBottomLeft = z[0];
		brightnessBottomRight = z[1];
		brightnessTopLeft = z[2];
		brightnessTopRight = z[3];
		Tessellator.instance.setColorOpaque_I( z[4] );

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
	public boolean renderStandardBlock(Block par1Block, int par2, int par3, int par4)
	{
		try
		{
			if ( calculations )
				return super.renderStandardBlock( par1Block, par2, par3, par4 );
			else
			{
				enableAO = isAO;
				boolean out = renderStandardBlockNoCalculations( par1Block, par2, par3, par4 );
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
			isAO = enableAO;
			rXNeg = par8Icon;
			saveAO( aoXNeg, foXNeg );
			bXNeg = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceXPos(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.EAST ) )
		{
			if ( isFacade )
			{
				Tessellator tessellator = Tessellator.instance;

				double d3 = (double) par8Icon.getInterpolatedU( this.renderMinZ * 16.0D );
				double d4 = (double) par8Icon.getInterpolatedU( this.renderMaxZ * 16.0D );
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
			isAO = enableAO;
			rXPos = par8Icon;
			saveAO( aoXPos, foXPos );
			bXPos = getCurrentBrightness();
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

		Tessellator.instance.setColorOpaque_F( r, g, b );
		Tessellator.instance.setBrightness( out );
	}

	@Override
	public void renderFaceYNeg(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.DOWN ) )
		{
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
			isAO = enableAO;
			rYNeg = par8Icon;
			saveAO( aoYNeg, foYNeg );
			bYNeg = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceYPos(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.UP ) )
		{
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
					tessellator.addVertexWithUV( d12, d13, d15, d3, d6 );
					tessellator.addVertexWithUV( d12, d13, d14, d3, d5 );
					tessellator.addVertexWithUV( d11, d13, d14, d4, d5 );
					tessellator.addVertexWithUV( d11, d13, d15, d4, d6 );
				}
			}
			else
				super.renderFaceYPos( par1Block, par2, par4, par6, par8Icon );
		}
		else
		{
			isAO = enableAO;
			rYPos = par8Icon;
			saveAO( aoYPos, foYPos );
			bYPos = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceZNeg(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.NORTH ) )
		{
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
			isAO = enableAO;
			rZNeg = par8Icon;
			saveAO( aoZNeg, foZNeg );
			bZNeg = getCurrentBrightness();
		}
	}

	@Override
	public void renderFaceZPos(Block par1Block, double par2, double par4, double par6, IIcon par8Icon)
	{
		if ( faces.contains( ForgeDirection.SOUTH ) )
		{
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
			isAO = enableAO;
			rZPos = par8Icon;
			saveAO( aoZPos, foZPos );
			bZPos = getCurrentBrightness();
		}
	}
}
