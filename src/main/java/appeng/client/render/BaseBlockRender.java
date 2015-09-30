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


import java.nio.FloatBuffer;
import java.util.EnumSet;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.core.AppEng;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


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

	private static int dynRender = 0;
	private final ModelResourceLocation modelPath = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, "DynamicRender"+dynRender++ ), "inventory" );

	public BaseBlockRender()
	{
		this( false, 20 );
	}

	public BaseBlockRender( boolean enableTESR, double renderDistance )
	{
		this.hasTESR = enableTESR;
		this.renderDistance = renderDistance;
		setOriMap();
	}

	public static void setOriMap()
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

	protected int adjustBrightness( int v, double d )
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

	public double getTesrRenderDistance()
	{
		return this.renderDistance;
	}

	public void renderInventory( B block, ItemStack item, ModelGenerator renderer, ItemRenderType type, Object[] data )
	{
		BlockRenderInfo info = block.getRendererInstance();
		if( info.isValid() )
		{
			if( block.hasSubtypes() )
			{
				block.setRenderStateByMeta( item.getItemDamage() );
			}

			renderer.uvRotateBottom = info.getTexture( AEPartLocation.DOWN ).setFlip( getOrientation( EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.UP ) );
			renderer.uvRotateTop = info.getTexture( AEPartLocation.UP ).setFlip( getOrientation( EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.UP ) );

			renderer.uvRotateEast = info.getTexture( AEPartLocation.EAST ).setFlip( getOrientation( EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.UP ) );
			renderer.uvRotateWest = info.getTexture( AEPartLocation.WEST ).setFlip( getOrientation( EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.UP ) );

			renderer.uvRotateNorth = info.getTexture( AEPartLocation.NORTH ).setFlip( getOrientation( EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.UP ) );
			renderer.uvRotateSouth = info.getTexture( AEPartLocation.SOUTH ).setFlip( getOrientation( EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.UP ) );
		}

		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), block, item, 0xffffff, renderer );

		if( block.hasSubtypes() )
		{
			info.setTemporaryRenderIcon( null );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
	}

	public static int getOrientation( EnumFacing in, EnumFacing forward, EnumFacing up )
	{
		if( in == null // 1
				|| forward == null // 2
				|| up == null )
		{
			return 0;
		}

		int a = in.ordinal();
		int b = forward.ordinal();
		int c = up.ordinal();

		return ORIENTATION_MAP[a][b][c];
	}

	public void renderInvBlock( EnumSet<AEPartLocation> sides, B block, ItemStack item,  int color, ModelGenerator tess )
	{
		if( block != null && block.hasSubtypes() && item != null )
		{
			int meta = item.getItemDamage();
		}

		IAESprite[] icons = tess.getIcon( item == null ? block.getDefaultState() : block.getStateFromMeta( item.getMetadata() ) );
		BlockPos zero = new BlockPos(0,0,0);
		
		if( sides.contains( AEPartLocation.DOWN ) )
		{
			tess.setNormal( 0.0F, -1.0F, 0.0F );
			tess.setColorOpaque_I( color );
			tess.renderFaceYNeg( block, zero, this.firstNotNull( tess.overrideBlockTexture, block.getRendererInstance().getTexture( AEPartLocation.DOWN ), icons[ AEPartLocation.DOWN.ordinal() ] ) );
		}

		if( sides.contains( AEPartLocation.UP ) )
		{
			tess.setNormal( 0.0F, 1.0F, 0.0F );
			tess.setColorOpaque_I( color );
			tess.renderFaceYPos( block, zero, this.firstNotNull( tess.overrideBlockTexture, block.getRendererInstance().getTexture( AEPartLocation.UP ), icons[ AEPartLocation.UP.ordinal() ] ) );
		}

		if( sides.contains( AEPartLocation.NORTH ) )
		{
			tess.setNormal( 0.0F, 0.0F, -1.0F );
			tess.setColorOpaque_I( color );
			tess.renderFaceZNeg( block, zero, this.firstNotNull( tess.overrideBlockTexture, block.getRendererInstance().getTexture( AEPartLocation.NORTH ), icons[ AEPartLocation.NORTH.ordinal() ] ) );
		}

		if( sides.contains( AEPartLocation.SOUTH ) )
		{
			tess.setNormal( 0.0F, 0.0F, 1.0F );
			tess.setColorOpaque_I( color );
			tess.renderFaceZPos( block, zero, this.firstNotNull( tess.overrideBlockTexture, block.getRendererInstance().getTexture( AEPartLocation.SOUTH ), icons[ AEPartLocation.SOUTH.ordinal() ] ) );
		}

		if( sides.contains( AEPartLocation.WEST ) )
		{
			tess.setNormal( -1.0F, 0.0F, 0.0F );
			tess.setColorOpaque_I( color );
			tess.renderFaceXNeg( block, zero, this.firstNotNull( tess.overrideBlockTexture, block.getRendererInstance().getTexture( AEPartLocation.WEST ), icons[ AEPartLocation.WEST.ordinal() ] ) );
		}

		if( sides.contains( AEPartLocation.EAST ) )
		{
			tess.setNormal( 1.0F, 0.0F, 0.0F );
			tess.setColorOpaque_I( color );
			tess.renderFaceXPos( block, zero, this.firstNotNull( tess.overrideBlockTexture, block.getRendererInstance().getTexture( AEPartLocation.EAST ), icons[ AEPartLocation.EAST.ordinal() ]) );
		}
	}

	public IAESprite firstNotNull( IAESprite... s )
	{
		for( IAESprite o : s )
		{
			if( o != null )
			{
				return o;
			}
		}
		return ExtraBlockTextures.getMissing();
	}

	public boolean renderInWorld( B block, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{
		this.preRenderInWorld( block, world, pos, renderer );

		boolean o = renderer.renderStandardBlock( block, pos );

		this.postRenderInWorld( renderer );
		return o;
	}

	public void preRenderInWorld( B block, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{

		BlockRenderInfo info = block.getRendererInstance();
		IOrientable te = this.getOrientable( block, world, pos );
		if( te != null )
		{
			EnumFacing forward = te.getForward();
			EnumFacing up = te.getUp();

			renderer.uvRotateBottom = info.getTexture( AEPartLocation.DOWN ).setFlip( getOrientation( EnumFacing.DOWN, forward, up ) );
			renderer.uvRotateTop = info.getTexture( AEPartLocation.UP ).setFlip( getOrientation( EnumFacing.UP, forward, up ) );

			renderer.uvRotateEast = info.getTexture( AEPartLocation.EAST ).setFlip( getOrientation( EnumFacing.EAST, forward, up ) );
			renderer.uvRotateWest = info.getTexture( AEPartLocation.WEST ).setFlip( getOrientation( EnumFacing.WEST, forward, up ) );

			renderer.uvRotateNorth = info.getTexture( AEPartLocation.NORTH ).setFlip( getOrientation( EnumFacing.NORTH, forward, up ) );
			renderer.uvRotateSouth = info.getTexture( AEPartLocation.SOUTH ).setFlip( getOrientation( EnumFacing.SOUTH, forward, up ) );
		}
	}

	public void postRenderInWorld( ModelGenerator renderer )
	{
		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
	}

	@Nullable
	public IOrientable getOrientable( B block, IBlockAccess w, BlockPos pos )
	{
		return block.getOrientable( w, pos );
	}

	protected void setInvRenderBounds( ModelGenerator renderer, int i, int j, int k, int l, int m, int n )
	{
		renderer.setRenderBounds( i / 16.0, j / 16.0, k / 16.0, l / 16.0, m / 16.0, n / 16.0 );
	}

	protected void renderBlockBounds( ModelGenerator renderer,

			double minX, double minY, double minZ,

			double maxX, double maxY, double maxZ,

			EnumFacing x, EnumFacing y, EnumFacing z )
	{
		minX /= 16.0;
		minY /= 16.0;
		minZ /= 16.0;
		maxX /= 16.0;
		maxY /= 16.0;
		maxZ /= 16.0;

		double aX = minX * x.getFrontOffsetX() + minY * y.getFrontOffsetX() + minZ * z.getFrontOffsetX();
		double aY = minX * x.getFrontOffsetY() + minY * y.getFrontOffsetY() + minZ * z.getFrontOffsetY();
		double aZ = minX * x.getFrontOffsetZ() + minY * y.getFrontOffsetZ() + minZ * z.getFrontOffsetZ();

		double bX = maxX * x.getFrontOffsetX() + maxY * y.getFrontOffsetX() + maxZ * z.getFrontOffsetX();
		double bY = maxX * x.getFrontOffsetY() + maxY * y.getFrontOffsetY() + maxZ * z.getFrontOffsetY();
		double bZ = maxX * x.getFrontOffsetZ() + maxY * y.getFrontOffsetZ() + maxZ * z.getFrontOffsetZ();

		if( x.getFrontOffsetX() + y.getFrontOffsetX() + z.getFrontOffsetX() < 0 )
		{
			aX += 1;
			bX += 1;
		}

		if( x.getFrontOffsetY() + y.getFrontOffsetY() + z.getFrontOffsetY() < 0 )
		{
			aY += 1;
			bY += 1;
		}

		if( x.getFrontOffsetZ() + y.getFrontOffsetZ() + z.getFrontOffsetZ() < 0 )
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
	protected void renderCutoutFace( B block, IAESprite ico, BlockPos pos, ModelGenerator tess, EnumFacing orientation, float edgeThickness )
	{
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

		offsetX += pos.getX();
		offsetY += pos.getY();
		offsetZ += pos.getZ();

		double layerBX = 0.0;
		double layerAY = 0.0;
		this.renderFace(orientation, tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				0, 1.0,
				// v -> v
				0, edgeThickness, ico, flip );

		this.renderFace( orientation,tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				0.0, edgeThickness,
				// v -> v
				edgeThickness, 1.0 - edgeThickness, ico, flip );

		this.renderFace( orientation,tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				1.0 - edgeThickness, 1.0,
				// v -> v
				edgeThickness, 1.0 - edgeThickness, ico, flip );

		this.renderFace( orientation,tess, offsetX, offsetY, offsetZ, layerAX, layerAY, layerAZ, layerBX, layerBY, layerBZ,
				// u -> u
				0, 1.0,
				// v -> v
				1.0 - edgeThickness, 1.0, ico, flip );
	}

	@SideOnly( Side.CLIENT )
	private void renderFace(  EnumFacing face, ModelGenerator tess, double offsetX, double offsetY, double offsetZ, double ax, double ay, double az, double bx, double by, double bz, double ua, double ub, double va, double vb, IAESprite ico, boolean flip )
	{
		if( flip )
		{
			tess.addVertexWithUV( face,offsetX + ax * ua + bx * va, offsetY + ay * ua + by * va, offsetZ + az * ua + bz * va, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
			tess.addVertexWithUV( face,offsetX + ax * ua + bx * vb, offsetY + ay * ua + by * vb, offsetZ + az * ua + bz * vb, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
			tess.addVertexWithUV( face,offsetX + ax * ub + bx * vb, offsetY + ay * ub + by * vb, offsetZ + az * ub + bz * vb, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
			tess.addVertexWithUV( face,offsetX + ax * ub + bx * va, offsetY + ay * ub + by * va, offsetZ + az * ub + bz * va, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
		}
		else
		{
			tess.addVertexWithUV( face, offsetX + ax * ua + bx * va, offsetY + ay * ua + by * va, offsetZ + az * ua + bz * va, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
			tess.addVertexWithUV( face, offsetX + ax * ub + bx * va, offsetY + ay * ub + by * va, offsetZ + az * ub + bz * va, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( va * 16.0 ) );
			tess.addVertexWithUV( face, offsetX + ax * ub + bx * vb, offsetY + ay * ub + by * vb, offsetZ + az * ub + bz * vb, ico.getInterpolatedU( ub * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
			tess.addVertexWithUV( face, offsetX + ax * ua + bx * vb, offsetY + ay * ua + by * vb, offsetZ + az * ua + bz * vb, ico.getInterpolatedU( ua * 16.0 ), ico.getInterpolatedV( vb * 16.0 ) );
		}
	}

	@SideOnly( Side.CLIENT )
	protected void renderFace( BlockPos pos, B block, IAESprite ico, ModelGenerator renderer, EnumFacing orientation )
	{
		switch( orientation )
		{
			case NORTH:
				renderer.renderFaceZNeg( block, pos, ico );
				break;
			case SOUTH:
				renderer.renderFaceZPos( block, pos, ico );
				break;
			case EAST:
				renderer.renderFaceXPos( block, pos, ico );
				break;
			case WEST:
				renderer.renderFaceXNeg( block, pos, ico );
				break;
			case UP:
				renderer.renderFaceYPos( block, pos, ico );
				break;
			case DOWN:
				renderer.renderFaceYNeg( block, pos, ico );
				break;
			default:
				break;
		}
	}

	public void selectFace( ModelGenerator renderer, EnumFacing west, EnumFacing up, EnumFacing forward, int u1, int u2, int v1, int v2 )
	{
		v1 = 16 - v1;
		v2 = 16 - v2;

		double minX = ( forward.getFrontOffsetX() > 0 ? 1 : 0 ) + this.mapFaceUV( west.getFrontOffsetX(), u1 ) + this.mapFaceUV( up.getFrontOffsetX(), v1 );
		double minY = ( forward.getFrontOffsetY() > 0 ? 1 : 0 ) + this.mapFaceUV( west.getFrontOffsetY(), u1 ) + this.mapFaceUV( up.getFrontOffsetY(), v1 );
		double minZ = ( forward.getFrontOffsetZ() > 0 ? 1 : 0 ) + this.mapFaceUV( west.getFrontOffsetZ(), u1 ) + this.mapFaceUV( up.getFrontOffsetZ(), v1 );

		double maxX = ( forward.getFrontOffsetX() > 0 ? 1 : 0 ) + this.mapFaceUV( west.getFrontOffsetX(), u2 ) + this.mapFaceUV( up.getFrontOffsetX(), v2 );
		double maxY = ( forward.getFrontOffsetY() > 0 ? 1 : 0 ) + this.mapFaceUV( west.getFrontOffsetY(), u2 ) + this.mapFaceUV( up.getFrontOffsetY(), v2 );
		double maxZ = ( forward.getFrontOffsetZ() > 0 ? 1 : 0 ) + this.mapFaceUV( west.getFrontOffsetZ(), u2 ) + this.mapFaceUV( up.getFrontOffsetZ(), v2 );

		renderer.renderMinX = Math.max( 0.0, Math.min( minX, maxX ) - ( forward.getFrontOffsetX() != 0 ? 0 : 0.001 ) );
		renderer.renderMaxX = Math.min( 1.0, Math.max( minX, maxX ) + ( forward.getFrontOffsetX() != 0 ? 0 : 0.001 ) );

		renderer.renderMinY = Math.max( 0.0, Math.min( minY, maxY ) - ( forward.getFrontOffsetY() != 0 ? 0 : 0.001 ) );
		renderer.renderMaxY = Math.min( 1.0, Math.max( minY, maxY ) + ( forward.getFrontOffsetY() != 0 ? 0 : 0.001 ) );

		renderer.renderMinZ = Math.max( 0.0, Math.min( minZ, maxZ ) - ( forward.getFrontOffsetZ() != 0 ? 0 : 0.001 ) );
		renderer.renderMaxZ = Math.min( 1.0, Math.max( minZ, maxZ ) + ( forward.getFrontOffsetZ() != 0 ? 0 : 0.001 ) );
	}

	private double mapFaceUV( int offset, int uv )
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

	public void renderTile( B block, T tile, WorldRenderer tess, double x, double y, double z, float f, ModelGenerator renderer )
	{

		renderer.uvRotateBottom = renderer.uvRotateTop = renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = 0;

		AEPartLocation up = AEPartLocation.UP;
		AEPartLocation forward = AEPartLocation.SOUTH;
		this.applyTESRRotation( x, y, z, forward.getFacing(), up.getFacing() );

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

		BlockPos pos = tile.getPos();
		renderer.setTranslation( -pos.getX(), -pos.getY(), -pos.getZ()  );

		// note that this is a terrible approach...
		renderer.setRenderBoundsFromBlock( block );
		renderer.renderStandardBlock( block, pos );

		renderer.setTranslation( 0, 0, 0 );
		RenderHelper.enableStandardItemLighting();

		renderer.uvRotateBottom = renderer.uvRotateTop = renderer.uvRotateEast = renderer.uvRotateWest = renderer.uvRotateNorth = renderer.uvRotateSouth = 0;
	}

	protected void applyTESRRotation( double x, double y, double z, EnumFacing forward, EnumFacing up )
	{
		if( forward != null && up != null )
		{
			EnumFacing west = Platform.crossProduct( forward, up );

			this.rotMat.put( 0, west.getFrontOffsetX() );
			this.rotMat.put( 1, west.getFrontOffsetY() );
			this.rotMat.put( 2, west.getFrontOffsetZ() );
			this.rotMat.put( 3, 0 );

			this.rotMat.put( 4, up.getFrontOffsetX() );
			this.rotMat.put( 5, up.getFrontOffsetY() );
			this.rotMat.put( 6, up.getFrontOffsetZ() );
			this.rotMat.put( 7, 0 );

			this.rotMat.put( 8, forward.getFrontOffsetX() );
			this.rotMat.put( 9, forward.getFrontOffsetY() );
			this.rotMat.put( 10, forward.getFrontOffsetZ() );
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

	public void doRenderItem( ItemStack itemstack, TileEntity par1EntityItemFrame )
	{
		if( itemstack != null )
		{
			EntityItem entityitem = new EntityItem( par1EntityItemFrame.getWorld(), 0.0D, 0.0D, 0.0D, itemstack );
			entityitem.getEntityItem().stackSize = 1;

			// set all this stuff and then do shit? meh?
			entityitem.hoverStart = 0;
			entityitem.setNoDespawn();
			entityitem.rotationYaw = 0;

			GL11.glPushMatrix();
			GL11.glTranslatef( 0, -0.14F, 0 );

			//RenderItem.renderInFrame = true;
			Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw( entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F );
			//RenderItem.renderInFrame = false;

			GL11.glPopMatrix();
		}
	}

	public ModelResourceLocation getResourcePath()
	{
		return modelPath;
	}
}
