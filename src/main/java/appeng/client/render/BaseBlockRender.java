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


import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.EnumSet;


@SideOnly( Side.CLIENT )
public class BaseBlockRender<B extends AEBaseBlock, T extends AEBaseTile>
{
	private static final int ORIENTATION_BITS = 7;
	private static final int FLIP_H_BIT = 8;
	private static final int FLIP_V_BIT = 16;
	private static final byte[][][] ORIENTATION_MAP = new byte[6][6][6];

	private final boolean hasTESR;
	private final double renderDistance;
	private final FloatBuffer rotMat = BufferUtils.createFloatBuffer( 16 );

	public BaseBlockRender()
	{
		this( false, 20 );
	}

	public BaseBlockRender( final boolean enableTESR, final double renderDistance )
	{
		this.hasTESR = enableTESR;
		this.renderDistance = renderDistance;
		setOriMap();
	}

	private static void setOriMap()
	{
		// pointed up...
		ORIENTATION_MAP[0][3][1] = 0;
		ORIENTATION_MAP[1][3][1] = 0;
		ORIENTATION_MAP[2][3][1] = 0;
		ORIENTATION_MAP[3][3][1] = 0;
		ORIENTATION_MAP[4][3][1] = 0;
		ORIENTATION_MAP[5][3][1] = 0;

		ORIENTATION_MAP[0][5][1] = 1;
		ORIENTATION_MAP[1][5][1] = 2;
		ORIENTATION_MAP[2][5][1] = 0;
		ORIENTATION_MAP[3][5][1] = 0;
		ORIENTATION_MAP[4][5][1] = 0;
		ORIENTATION_MAP[5][5][1] = 0;

		ORIENTATION_MAP[0][2][1] = 3;
		ORIENTATION_MAP[1][2][1] = 3;
		ORIENTATION_MAP[2][2][1] = 0;
		ORIENTATION_MAP[3][2][1] = 0;
		ORIENTATION_MAP[4][2][1] = 0;
		ORIENTATION_MAP[5][2][1] = 0;

		ORIENTATION_MAP[0][4][1] = 2;
		ORIENTATION_MAP[1][4][1] = 1;
		ORIENTATION_MAP[2][4][1] = 0;
		ORIENTATION_MAP[3][4][1] = 0;
		ORIENTATION_MAP[4][4][1] = 0;
		ORIENTATION_MAP[5][4][1] = 0;

		// upside down
		ORIENTATION_MAP[0][3][0] = FLIP_H_BIT;
		ORIENTATION_MAP[1][3][0] = FLIP_H_BIT;
		ORIENTATION_MAP[2][3][0] = 3;
		ORIENTATION_MAP[3][3][0] = 3;
		ORIENTATION_MAP[4][3][0] = 3;
		ORIENTATION_MAP[5][3][0] = 3;

		ORIENTATION_MAP[0][4][0] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[1][4][0] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[2][4][0] = 3;
		ORIENTATION_MAP[3][4][0] = 3;
		ORIENTATION_MAP[4][4][0] = 3;
		ORIENTATION_MAP[5][4][0] = 3;

		ORIENTATION_MAP[0][5][0] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[1][5][0] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[2][5][0] = 3;
		ORIENTATION_MAP[3][5][0] = 3;
		ORIENTATION_MAP[4][5][0] = 3;
		ORIENTATION_MAP[5][5][0] = 3;

		ORIENTATION_MAP[0][2][0] = 3 | FLIP_H_BIT;
		ORIENTATION_MAP[1][2][0] = 3 | FLIP_H_BIT;
		ORIENTATION_MAP[2][2][0] = 3;
		ORIENTATION_MAP[3][2][0] = 3;
		ORIENTATION_MAP[4][2][0] = 3;
		ORIENTATION_MAP[5][2][0] = 3;

		// side 1
		ORIENTATION_MAP[0][3][5] = 1 | FLIP_V_BIT;
		ORIENTATION_MAP[1][3][5] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[2][3][5] = 1;
		ORIENTATION_MAP[3][3][5] = 1;
		ORIENTATION_MAP[4][3][5] = 1;
		ORIENTATION_MAP[5][3][5] = 1 | FLIP_V_BIT;

		ORIENTATION_MAP[0][1][5] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[1][1][5] = 1;
		ORIENTATION_MAP[2][1][5] = 3 | FLIP_V_BIT;
		ORIENTATION_MAP[3][1][5] = 3;
		ORIENTATION_MAP[4][1][5] = 1 | FLIP_V_BIT;
		ORIENTATION_MAP[5][1][5] = 1;

		ORIENTATION_MAP[0][2][5] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[1][2][5] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[2][2][5] = 1;
		ORIENTATION_MAP[3][2][5] = 2 | FLIP_V_BIT;
		ORIENTATION_MAP[4][2][5] = 1 | FLIP_V_BIT;
		ORIENTATION_MAP[5][2][5] = 1;

		ORIENTATION_MAP[0][0][5] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[1][0][5] = 1;
		ORIENTATION_MAP[2][0][5] = 0;
		ORIENTATION_MAP[3][0][5] = FLIP_V_BIT;
		ORIENTATION_MAP[4][0][5] = 1;
		ORIENTATION_MAP[5][0][5] = 1 | FLIP_V_BIT;

		// side 2
		ORIENTATION_MAP[0][1][2] = FLIP_H_BIT;
		ORIENTATION_MAP[1][1][2] = 0;
		ORIENTATION_MAP[2][1][2] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[3][1][2] = 1;
		ORIENTATION_MAP[4][1][2] = 3;
		ORIENTATION_MAP[5][1][2] = 3 | FLIP_H_BIT;

		ORIENTATION_MAP[0][4][2] = FLIP_H_BIT;
		ORIENTATION_MAP[1][4][2] = FLIP_H_BIT;
		ORIENTATION_MAP[2][4][2] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[3][4][2] = 1;
		ORIENTATION_MAP[4][4][2] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[5][4][2] = 2;

		ORIENTATION_MAP[0][0][2] = FLIP_V_BIT;
		ORIENTATION_MAP[1][0][2] = 0;
		ORIENTATION_MAP[2][0][2] = 2;
		ORIENTATION_MAP[3][0][2] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[4][0][2] = 3 | FLIP_H_BIT;
		ORIENTATION_MAP[5][0][2] = 0;

		ORIENTATION_MAP[0][5][2] = FLIP_H_BIT;
		ORIENTATION_MAP[1][5][2] = FLIP_H_BIT;
		ORIENTATION_MAP[2][5][2] = 2;
		ORIENTATION_MAP[3][5][2] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[4][5][2] = 2;
		ORIENTATION_MAP[5][5][2] = 1 | FLIP_H_BIT;

		// side 3
		ORIENTATION_MAP[0][0][3] = 3 | FLIP_H_BIT;
		ORIENTATION_MAP[1][0][3] = 3;
		ORIENTATION_MAP[2][0][3] = 1;
		ORIENTATION_MAP[3][0][3] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[4][0][3] = 0;
		ORIENTATION_MAP[5][0][3] = FLIP_H_BIT;

		ORIENTATION_MAP[0][4][3] = 3;
		ORIENTATION_MAP[1][4][3] = 3;
		ORIENTATION_MAP[2][4][3] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[3][4][3] = 2;
		ORIENTATION_MAP[4][4][3] = 1;
		ORIENTATION_MAP[5][4][3] = 2 | FLIP_H_BIT;

		ORIENTATION_MAP[0][1][3] = 3 | FLIP_V_BIT;
		ORIENTATION_MAP[1][1][3] = 3;
		ORIENTATION_MAP[2][1][3] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[3][1][3] = 2;
		ORIENTATION_MAP[4][1][3] = 3 | FLIP_H_BIT;
		ORIENTATION_MAP[5][1][3] = 0;

		ORIENTATION_MAP[0][5][3] = 3;
		ORIENTATION_MAP[1][5][3] = 3;
		ORIENTATION_MAP[2][5][3] = 1;
		ORIENTATION_MAP[3][5][3] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[4][5][3] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[5][5][3] = 1;

		// side 4
		ORIENTATION_MAP[0][3][4] = 1;
		ORIENTATION_MAP[1][3][4] = 2;
		ORIENTATION_MAP[2][3][4] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[3][3][4] = 1;
		ORIENTATION_MAP[4][3][4] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[5][3][4] = 1;

		ORIENTATION_MAP[0][0][4] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[1][0][4] = 2;
		ORIENTATION_MAP[2][0][4] = 0;
		ORIENTATION_MAP[3][0][4] = FLIP_H_BIT;
		ORIENTATION_MAP[4][0][4] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[5][0][4] = 1;

		ORIENTATION_MAP[0][1][4] = 1 | FLIP_H_BIT;
		ORIENTATION_MAP[1][1][4] = 2;
		ORIENTATION_MAP[2][1][4] = 3 | FLIP_H_BIT;
		ORIENTATION_MAP[3][1][4] = 3;
		ORIENTATION_MAP[4][1][4] = 2;
		ORIENTATION_MAP[5][1][4] = 1 | FLIP_H_BIT;

		ORIENTATION_MAP[0][2][4] = 1;
		ORIENTATION_MAP[1][2][4] = 2;
		ORIENTATION_MAP[2][2][4] = 1;
		ORIENTATION_MAP[3][2][4] = 2 | FLIP_H_BIT;
		ORIENTATION_MAP[4][2][4] = 2;
		ORIENTATION_MAP[5][2][4] = 1 | FLIP_H_BIT;
	}

	public boolean hasTESR()
	{
		return this.hasTESR;
	}

	protected int adjustBrightness( final int v, final double d )
	{
		int r = 0xff & ( v >> 16 );
		int g = 0xff & ( v >> 8 );
		int b = 0xff & ( v );

		r *= d;
		g *= d;
		b *= d;

		r = Math.min( 255, Math.max( 0, r ) );
		g = Math.min( 255, Math.max( 0, g ) );
		b = Math.min( 255, Math.max( 0, b ) );

		return ( r << 16 ) | ( g << 8 ) | b;
	}

	double getTesrRenderDistance()
	{
		return this.renderDistance;
	}

	public void renderInventory( final B block, final ItemStack item, final RenderBlocks renderer, final ItemRenderType type, final Object[] data )
	{
		final Tessellator tess = Tessellator.instance;
		final BlockRenderInfo info = block.getRendererInstance();

		if( info.isValid() )
		{
			if( block.hasSubtypes() )
			{
				block.setRenderStateByMeta( item.getItemDamage() );
			}

			renderer.uvRotateBottom = info.getTexture( ForgeDirection.DOWN ).setFlip( getOrientation( ForgeDirection.DOWN, ForgeDirection.SOUTH, ForgeDirection.UP ) );
			renderer.uvRotateTop = info.getTexture( ForgeDirection.UP ).setFlip( getOrientation( ForgeDirection.UP, ForgeDirection.SOUTH, ForgeDirection.UP ) );

			renderer.uvRotateEast = info.getTexture( ForgeDirection.EAST ).setFlip( getOrientation( ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.UP ) );
			renderer.uvRotateWest = info.getTexture( ForgeDirection.WEST ).setFlip( getOrientation( ForgeDirection.WEST, ForgeDirection.SOUTH, ForgeDirection.UP ) );

			renderer.uvRotateNorth = info.getTexture( ForgeDirection.NORTH ).setFlip( getOrientation( ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.UP ) );
			renderer.uvRotateSouth = info.getTexture( ForgeDirection.SOUTH ).setFlip( getOrientation( ForgeDirection.SOUTH, ForgeDirection.SOUTH, ForgeDirection.UP ) );
		}

		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), block, item, tess, 0xffffff, renderer );

		if( block.hasSubtypes() )
		{
			info.setTemporaryRenderIcon( null );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
	}

	static int getOrientation( final ForgeDirection in, final ForgeDirection forward, final ForgeDirection up )
	{
		if( in == null || in == ForgeDirection.UNKNOWN // 1
				|| forward == null || forward == ForgeDirection.UNKNOWN // 2
				|| up == null || up == ForgeDirection.UNKNOWN )
		{
			return 0;
		}

		final int a = in.ordinal();
		final int b = forward.ordinal();
		final int c = up.ordinal();

		return ORIENTATION_MAP[a][b][c];
	}

	public void renderInvBlock( final EnumSet<ForgeDirection> sides, final B block, final ItemStack item, final Tessellator tess, final int color, final RenderBlocks renderer )
	{
		if( block == null )
		{
			return;
		}

		int meta = 0;

		if( block.hasSubtypes() && item != null )
		{
			meta = item.getItemDamage();
		}

		if( sides.contains( ForgeDirection.DOWN ) )
		{
			tess.startDrawingQuads();
			tess.setNormal( 0.0F, -1.0F, 0.0F );
			tess.setColorOpaque_I( color );
			renderer.renderFaceYNeg( block, 0.0D, 0.0D, 0.0D, this.firstNotNull( renderer.overrideBlockTexture, block.getRendererInstance().getTexture( ForgeDirection.DOWN ), block.getIcon( ForgeDirection.DOWN.ordinal(), meta ) ) );
			tess.draw();
		}

		if( sides.contains( ForgeDirection.UP ) )
		{
			tess.startDrawingQuads();
			tess.setNormal( 0.0F, 1.0F, 0.0F );
			tess.setColorOpaque_I( color );
			renderer.renderFaceYPos( block, 0.0D, 0.0D, 0.0D, this.firstNotNull( renderer.overrideBlockTexture, block.getRendererInstance().getTexture( ForgeDirection.UP ), block.getIcon( ForgeDirection.UP.ordinal(), meta ) ) );
			tess.draw();
		}

		if( sides.contains( ForgeDirection.NORTH ) )
		{
			tess.startDrawingQuads();
			tess.setNormal( 0.0F, 0.0F, -1.0F );
			tess.setColorOpaque_I( color );
			renderer.renderFaceZNeg( block, 0.0D, 0.0D, 0.0D, this.firstNotNull( renderer.overrideBlockTexture, block.getRendererInstance().getTexture( ForgeDirection.NORTH ), block.getIcon( ForgeDirection.NORTH.ordinal(), meta ) ) );
			tess.draw();
		}

		if( sides.contains( ForgeDirection.SOUTH ) )
		{
			tess.startDrawingQuads();
			tess.setNormal( 0.0F, 0.0F, 1.0F );
			tess.setColorOpaque_I( color );
			renderer.renderFaceZPos( block, 0.0D, 0.0D, 0.0D, this.firstNotNull( renderer.overrideBlockTexture, block.getRendererInstance().getTexture( ForgeDirection.SOUTH ), block.getIcon( ForgeDirection.SOUTH.ordinal(), meta ) ) );
			tess.draw();
		}

		if( sides.contains( ForgeDirection.WEST ) )
		{
			tess.startDrawingQuads();
			tess.setNormal( -1.0F, 0.0F, 0.0F );
			tess.setColorOpaque_I( color );
			renderer.renderFaceXNeg( block, 0.0D, 0.0D, 0.0D, this.firstNotNull( renderer.overrideBlockTexture, block.getRendererInstance().getTexture( ForgeDirection.WEST ), block.getIcon( ForgeDirection.WEST.ordinal(), meta ) ) );
			tess.draw();
		}

		if( sides.contains( ForgeDirection.EAST ) )
		{
			tess.startDrawingQuads();
			tess.setNormal( 1.0F, 0.0F, 0.0F );
			tess.setColorOpaque_I( color );
			renderer.renderFaceXPos( block, 0.0D, 0.0D, 0.0D, this.firstNotNull( renderer.overrideBlockTexture, block.getRendererInstance().getTexture( ForgeDirection.EAST ), block.getIcon( ForgeDirection.EAST.ordinal(), meta ) ) );
			tess.draw();
		}
	}

	private IIcon firstNotNull( final IIcon... s )
	{
		for( final IIcon o : s )
		{
			if( o != null )
			{
				return o;
			}
		}

		return ExtraBlockTextures.getMissing();
	}

	public boolean renderInWorld( final B block, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		this.preRenderInWorld( block, world, x, y, z, renderer );

		final boolean o = renderer.renderStandardBlock( block, x, y, z );

		this.postRenderInWorld( renderer );

		return o;
	}

	public void preRenderInWorld( final B block, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{

		final BlockRenderInfo info = block.getRendererInstance();
		final IOrientable te = this.getOrientable( block, world, x, y, z );

		if( te != null )
		{
			final ForgeDirection forward = te.getForward();
			final ForgeDirection up = te.getUp();

			renderer.uvRotateBottom = info.getTexture( ForgeDirection.DOWN ).setFlip( getOrientation( ForgeDirection.DOWN, forward, up ) );
			renderer.uvRotateTop = info.getTexture( ForgeDirection.UP ).setFlip( getOrientation( ForgeDirection.UP, forward, up ) );

			renderer.uvRotateEast = info.getTexture( ForgeDirection.EAST ).setFlip( getOrientation( ForgeDirection.EAST, forward, up ) );
			renderer.uvRotateWest = info.getTexture( ForgeDirection.WEST ).setFlip( getOrientation( ForgeDirection.WEST, forward, up ) );

			renderer.uvRotateNorth = info.getTexture( ForgeDirection.NORTH ).setFlip( getOrientation( ForgeDirection.NORTH, forward, up ) );
			renderer.uvRotateSouth = info.getTexture( ForgeDirection.SOUTH ).setFlip( getOrientation( ForgeDirection.SOUTH, forward, up ) );
		}
	}

	public void postRenderInWorld( final RenderBlocks renderer )
	{
		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
	}

	@Nullable
	public IOrientable getOrientable( final B block, final IBlockAccess w, final int x, final int y, final int z )
	{
		return block.getOrientable( w, x, y, z );
	}

	protected void setInvRenderBounds( final RenderBlocks renderer, final int i, final int j, final int k, final int l, final int m, final int n )
	{
		renderer.setRenderBounds( i / 16.0, j / 16.0, k / 16.0, l / 16.0, m / 16.0, n / 16.0 );
	}

	protected void renderBlockBounds( final RenderBlocks renderer, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, final ForgeDirection x, final ForgeDirection y, final ForgeDirection z )
	{
		minX /= 16.0;
		minY /= 16.0;
		minZ /= 16.0;
		maxX /= 16.0;
		maxY /= 16.0;
		maxZ /= 16.0;

		double aX = minX * x.offsetX + minY * y.offsetX + minZ * z.offsetX;
		double aY = minX * x.offsetY + minY * y.offsetY + minZ * z.offsetY;
		double aZ = minX * x.offsetZ + minY * y.offsetZ + minZ * z.offsetZ;

		double bX = maxX * x.offsetX + maxY * y.offsetX + maxZ * z.offsetX;
		double bY = maxX * x.offsetY + maxY * y.offsetY + maxZ * z.offsetY;
		double bZ = maxX * x.offsetZ + maxY * y.offsetZ + maxZ * z.offsetZ;

		if( x.offsetX + y.offsetX + z.offsetX < 0 )
		{
			aX += 1;
			bX += 1;
		}

		if( x.offsetY + y.offsetY + z.offsetY < 0 )
		{
			aY += 1;
			bY += 1;
		}

		if( x.offsetZ + y.offsetZ + z.offsetZ < 0 )
		{
			aZ += 1;
			bZ += 1;
		}

		renderer.renderMinX = Math.min( aX, bX );
		renderer.renderMinY = Math.min( aY, bY );
		renderer.renderMinZ = Math.min( aZ, bZ );
		renderer.renderMaxX = Math.max( aX, bX );
		renderer.renderMaxY = Math.max( aY, bY );
		renderer.renderMaxZ = Math.max( aZ, bZ );
	}

	@SideOnly( Side.CLIENT )
	protected void renderCutoutFace( final B block, final IIcon ico, final int x, final int y, final int z, final RenderBlocks renderer, final ForgeDirection orientation, final float edgeThickness )
	{
		final Tessellator tess = Tessellator.instance;

		double offsetX = 0.0;
		double offsetY = 0.0;
		double offsetZ = 0.0;
		double layerAX = 0.0;
		double layerAZ = 0.0;
		double layerBY = 0.0;
		double layerBZ = 0.0;

		boolean flip = false;
		switch( orientation )
		{
			case NORTH:

				layerAX = 1.0;
				layerBY = 1.0;
				flip = true;

				break;
			case SOUTH:

				layerAX = 1.0;
				layerBY = 1.0;
				offsetZ = 1.0;

				break;
			case EAST:

				flip = true;
				layerAZ = 1.0;
				layerBY = 1.0;
				offsetX = 1.0;

				break;
			case WEST:

				layerAZ = 1.0;
				layerBY = 1.0;

				break;
			case UP:

				flip = true;
				layerAX = 1.0;
				layerBZ = 1.0;
				offsetY = 1.0;

				break;
			case DOWN:

				layerAX = 1.0;
				layerBZ = 1.0;

				break;
			default:
				break;
		}

		offsetX += x;
		offsetY += y;
		offsetZ += z;

		final double layerBX = 0.0;
		final double layerAY = 0.0;
		this.renderFace( tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				0, 1.0,
				// v -> v
				0, edgeThickness, ico, flip );

		this.renderFace( tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				0.0, edgeThickness,
				// v -> v
				edgeThickness, 1.0 - edgeThickness, ico, flip );

		this.renderFace( tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				1.0 - edgeThickness, 1.0,
				// v -> v
				edgeThickness, 1.0 - edgeThickness, ico, flip );

		this.renderFace( tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				0, 1.0,
				// v -> v
				1.0 - edgeThickness, 1.0, ico, flip );
	}

	@SideOnly( Side.CLIENT )
	private void renderFace( final Tessellator tess, final double offsetX, final double offsetY, final double offsetZ, final double ax, final double ay, final double az, final double bx, final double by, final double bz, final double ua, final double ub, final double va, final double vb, final IIcon ico, final boolean flip )
	{
		if( flip )
		{
			tess.addVertexWithUV( offsetX + ax * ua + bx * va, offsetY + ay * ua + by * va, offsetZ + az * ua + bz * va, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
			tess.addVertexWithUV( offsetX + ax * ua + bx * vb, offsetY + ay * ua + by * vb, offsetZ + az * ua + bz * vb, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
			tess.addVertexWithUV( offsetX + ax * ub + bx * vb, offsetY + ay * ub + by * vb, offsetZ + az * ub + bz * vb, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
			tess.addVertexWithUV( offsetX + ax * ub + bx * va, offsetY + ay * ub + by * va, offsetZ + az * ub + bz * va, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
		}
		else
		{
			tess.addVertexWithUV( offsetX + ax * ua + bx * va, offsetY + ay * ua + by * va, offsetZ + az * ua + bz * va, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
			tess.addVertexWithUV( offsetX + ax * ub + bx * va, offsetY + ay * ub + by * va, offsetZ + az * ub + bz * va, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
			tess.addVertexWithUV( offsetX + ax * ub + bx * vb, offsetY + ay * ub + by * vb, offsetZ + az * ub + bz * vb, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
			tess.addVertexWithUV( offsetX + ax * ua + bx * vb, offsetY + ay * ua + by * vb, offsetZ + az * ua + bz * vb, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
		}
	}

	@SideOnly( Side.CLIENT )
	protected void renderFace( final int x, final int y, final int z, final B block, final IIcon ico, final RenderBlocks renderer, final ForgeDirection orientation )
	{
		switch( orientation )
		{
			case NORTH:
				renderer.renderFaceZNeg( block, x, y, z, ico );
				break;
			case SOUTH:
				renderer.renderFaceZPos( block, x, y, z, ico );
				break;
			case EAST:
				renderer.renderFaceXPos( block, x, y, z, ico );
				break;
			case WEST:
				renderer.renderFaceXNeg( block, x, y, z, ico );
				break;
			case UP:
				renderer.renderFaceYPos( block, x, y, z, ico );
				break;
			case DOWN:
				renderer.renderFaceYNeg( block, x, y, z, ico );
				break;
			default:
				break;
		}
	}

	public void selectFace( final RenderBlocks renderer, final ForgeDirection west, final ForgeDirection up, final ForgeDirection forward, final int u1, final int u2, int v1, int v2 )
	{
		v1 = 16 - v1;
		v2 = 16 - v2;

		final double minX = ( forward.offsetX > 0 ? 1 : 0 ) + this.mapFaceUV( west.offsetX, u1 ) + this.mapFaceUV( up.offsetX, v1 );
		final double minY = ( forward.offsetY > 0 ? 1 : 0 ) + this.mapFaceUV( west.offsetY, u1 ) + this.mapFaceUV( up.offsetY, v1 );
		final double minZ = ( forward.offsetZ > 0 ? 1 : 0 ) + this.mapFaceUV( west.offsetZ, u1 ) + this.mapFaceUV( up.offsetZ, v1 );

		final double maxX = ( forward.offsetX > 0 ? 1 : 0 ) + this.mapFaceUV( west.offsetX, u2 ) + this.mapFaceUV( up.offsetX, v2 );
		final double maxY = ( forward.offsetY > 0 ? 1 : 0 ) + this.mapFaceUV( west.offsetY, u2 ) + this.mapFaceUV( up.offsetY, v2 );
		final double maxZ = ( forward.offsetZ > 0 ? 1 : 0 ) + this.mapFaceUV( west.offsetZ, u2 ) + this.mapFaceUV( up.offsetZ, v2 );

		renderer.renderMinX = Math.max( 0.0, Math.min( minX, maxX ) - ( forward.offsetX != 0 ? 0 : 0.001 ) );
		renderer.renderMaxX = Math.min( 1.0, Math.max( minX, maxX ) + ( forward.offsetX != 0 ? 0 : 0.001 ) );

		renderer.renderMinY = Math.max( 0.0, Math.min( minY, maxY ) - ( forward.offsetY != 0 ? 0 : 0.001 ) );
		renderer.renderMaxY = Math.min( 1.0, Math.max( minY, maxY ) + ( forward.offsetY != 0 ? 0 : 0.001 ) );

		renderer.renderMinZ = Math.max( 0.0, Math.min( minZ, maxZ ) - ( forward.offsetZ != 0 ? 0 : 0.001 ) );
		renderer.renderMaxZ = Math.min( 1.0, Math.max( minZ, maxZ ) + ( forward.offsetZ != 0 ? 0 : 0.001 ) );
	}

	private double mapFaceUV( final int offset, final int uv )
	{
		if( offset == 0 )
		{
			return 0;
		}

		if( offset > 0 )
		{
			return uv / 16.0;
		}

		return ( 16.0 - uv ) / 16.0;
	}

	public void renderTile( final B block, final T tile, final Tessellator tess, final double x, final double y, final double z, final float f, final RenderBlocks renderer )
	{
		renderer.uvRotateBottom = renderer.uvRotateTop = renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = 0;

		final ForgeDirection up = ForgeDirection.UP;
		final ForgeDirection forward = ForgeDirection.SOUTH;
		this.applyTESRRotation( x, y, z, forward, up );

		Minecraft.getMinecraft().getTextureManager().bindTexture( TextureMap.locationBlocksTexture );
		RenderHelper.disableStandardItemLighting();

		if( Minecraft.isAmbientOcclusionEnabled() )
		{
			GL11.glShadeModel( GL11.GL_SMOOTH );
		}
		else
		{
			GL11.glShadeModel( GL11.GL_FLAT );
		}

		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );

		tess.setTranslation( -tile.xCoord, -tile.yCoord, -tile.zCoord );
		tess.startDrawingQuads();

		// note that this is a terrible approach...
		renderer.setRenderBoundsFromBlock( block );
		renderer.renderStandardBlock( block, tile.xCoord, tile.yCoord, tile.zCoord );

		tess.draw();
		tess.setTranslation( 0, 0, 0 );
		RenderHelper.enableStandardItemLighting();

		renderer.uvRotateBottom = renderer.uvRotateTop = renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = 0;
	}

	protected void applyTESRRotation( final double x, final double y, final double z, ForgeDirection forward, ForgeDirection up )
	{
		if( forward != null && up != null )
		{
			if( forward == ForgeDirection.UNKNOWN )
			{
				forward = ForgeDirection.SOUTH;
			}

			if( up == ForgeDirection.UNKNOWN )
			{
				up = ForgeDirection.UP;
			}

			final ForgeDirection west = Platform.crossProduct( forward, up );

			this.rotMat.put( 0, west.offsetX );
			this.rotMat.put( 1, west.offsetY );
			this.rotMat.put( 2, west.offsetZ );
			this.rotMat.put( 3, 0 );

			this.rotMat.put( 4, up.offsetX );
			this.rotMat.put( 5, up.offsetY );
			this.rotMat.put( 6, up.offsetZ );
			this.rotMat.put( 7, 0 );

			this.rotMat.put( 8, forward.offsetX );
			this.rotMat.put( 9, forward.offsetY );
			this.rotMat.put( 10, forward.offsetZ );
			this.rotMat.put( 11, 0 );

			this.rotMat.put( 12, 0 );
			this.rotMat.put( 13, 0 );
			this.rotMat.put( 14, 0 );
			this.rotMat.put( 15, 1 );
			GL11.glTranslated( x + 0.5, y + 0.5, z + 0.5 );
			GL11.glMultMatrix( this.rotMat );
			GL11.glTranslated( -0.5, -0.5, -0.5 );
		}
		else
		{
			GL11.glTranslated( x, y, z );
		}
	}

	public void doRenderItem( final ItemStack itemstack, final TileEntity par1EntityItemFrame )
	{
		if( itemstack != null )
		{
			final EntityItem entityitem = new EntityItem( par1EntityItemFrame.getWorldObj(), 0.0D, 0.0D, 0.0D, itemstack );

			entityitem.getEntityItem().stackSize = 1;

			// set all this stuff and then do shit? meh?
			entityitem.hoverStart = 0;
			entityitem.age = 0;
			entityitem.rotationYaw = 0;

			GL11.glPushMatrix();
			GL11.glTranslatef( 0, -0.14F, 0 );

			RenderItem.renderInFrame = true;
			RenderManager.instance.renderEntityWithPosYaw( entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F );
			RenderItem.renderInFrame = false;

			GL11.glPopMatrix();
		}
	}
}
