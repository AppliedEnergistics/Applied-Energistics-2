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

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.ISimplifiedBundle;
import appeng.block.AEBaseBlock;
import appeng.block.networking.BlockCableBus;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BusRenderHelper implements IPartRenderHelper
{

	final public static BusRenderHelper instance = new BusRenderHelper();

	double minX = 0;
	double minY = 0;
	double minZ = 0;
	double maxX = 16;
	double maxY = 16;
	double maxZ = 16;

	final AEBaseBlock blk = (AEBaseBlock) AEApi.instance().blocks().blockMultiPart.block();
	final BaseBlockRender bbr = new BaseBlockRender();

	private ForgeDirection ax = ForgeDirection.EAST;
	private ForgeDirection ay = ForgeDirection.UP;
	private ForgeDirection az = ForgeDirection.SOUTH;

	int color = 0xffffff;

	class BoundBoxCalculator implements IPartCollisionHelper
	{

		public boolean started = false;

		float minX;
		float minY;
		float minZ;

		float maxX;
		float maxY;
		float maxZ;

		@Override
		public void addBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
		{
			if ( this.started )
			{
				this.minX = Math.min( this.minX, (float) minX );
				this.minY = Math.min( this.minY, (float) minY );
				this.minZ = Math.min( this.minZ, (float) minZ );
				this.maxX = Math.max( this.maxX, (float) maxX );
				this.maxY = Math.max( this.maxY, (float) maxY );
				this.maxZ = Math.max( this.maxZ, (float) maxZ );
			}
			else
			{
				this.started = true;
				this.minX = (float) minX;
				this.minY = (float) minY;
				this.minZ = (float) minZ;
				this.maxX = (float) maxX;
				this.maxY = (float) maxY;
				this.maxZ = (float) maxZ;
			}
		}

		@Override
		public ForgeDirection getWorldX()
		{
			return BusRenderHelper.this.ax;
		}

		@Override
		public ForgeDirection getWorldY()
		{
			return BusRenderHelper.this.ay;
		}

		@Override
		public ForgeDirection getWorldZ()
		{
			return BusRenderHelper.this.az;
		}

		@Override
		public boolean isBBCollision()
		{
			return false;
		}

	}

	final BoundBoxCalculator bbc = new BoundBoxCalculator();

	int renderingForPass = 0;
	int currentPass = 0;
	int itemsRendered = 0;
	final boolean noAlphaPass = !AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass );

	public int getItemsRendered()
	{
		return this.itemsRendered;
	}

	public void setPass(int pass)
	{
		this.renderingForPass = 0;
		this.currentPass = pass;
		this.itemsRendered = 0;
	}

	@Override
	public void renderForPass(int pass)
	{
		this.renderingForPass = pass;
	}

	public boolean renderThis()
	{
		if ( this.renderingForPass == this.currentPass || this.noAlphaPass )
		{
			this.itemsRendered++;
			return true;
		}
		return false;
	}

	@Override
	public void normalRendering()
	{
		RenderBlocksWorkaround rbw = BusRenderer.instance.renderer;
		rbw.calculations = true;
		rbw.useTextures = true;
		rbw.enableAO = false;
	}

	@Override
	public ISimplifiedBundle useSimplifiedRendering(int x, int y, int z, IBoxProvider p, ISimplifiedBundle sim)
	{
		RenderBlocksWorkaround rbw = BusRenderer.instance.renderer;

		if ( sim != null && rbw.similarLighting( this.blk, rbw.blockAccess, x, y, z, sim ) )
		{
			rbw.populate( sim );
			rbw.faces = EnumSet.allOf( ForgeDirection.class );
			rbw.calculations = false;
			rbw.useTextures = false;

			return sim;
		}
		else
		{
			boolean allFaces = rbw.renderAllFaces;
			rbw.renderAllFaces = true;
			rbw.calculations = true;
			rbw.faces.clear();

			this.bbc.started = false;
			if ( p == null )
			{
				this.bbc.minX = this.bbc.minY = this.bbc.minZ = 0;
				this.bbc.maxX = this.bbc.maxY = this.bbc.maxZ = 16;
			}
			else
			{
				p.getBoxes( this.bbc );

				if ( this.bbc.minX < 1 )
					this.bbc.minX = 1;
				if ( this.bbc.minY < 1 )
					this.bbc.minY = 1;
				if ( this.bbc.minZ < 1 )
					this.bbc.minZ = 1;

				if ( this.bbc.maxX > 15 )
					this.bbc.maxX = 15;
				if ( this.bbc.maxY > 15 )
					this.bbc.maxY = 15;
				if ( this.bbc.maxZ > 15 )
					this.bbc.maxZ = 15;
			}

			this.setBounds( this.bbc.minX, this.bbc.minY, this.bbc.minZ, this.bbc.maxX, this.bbc.maxY, this.bbc.maxZ );

			this.bbr.renderBlockBounds( rbw, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );
			rbw.renderStandardBlock( this.blk, x, y, z );

			rbw.faces = EnumSet.allOf( ForgeDirection.class );
			rbw.renderAllFaces = allFaces;
			rbw.calculations = false;
			rbw.useTextures = false;

			return rbw.getLightingCache();
		}
	}

	@Override
	public void setBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ)
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public double getBound(ForgeDirection side)
	{
		switch (side)
		{
		default:
		case UNKNOWN:
			return 0.5;
		case DOWN:
			return this.minY;
		case EAST:
			return this.maxX;
		case NORTH:
			return this.minZ;
		case SOUTH:
			return this.maxZ;
		case UP:
			return this.maxY;
		case WEST:
			return this.minX;

		}
	}

	@Override
	public void setInvColor(int newColor)
	{
		this.color = newColor;
	}

	@Override
	public void setTexture(IIcon ico)
	{
		this.blk.getRendererInstance().setTemporaryRenderIcon( ico );
	}

	@Override
	public void setTexture(IIcon Down, IIcon Up, IIcon North, IIcon South, IIcon West, IIcon East)
	{
		IIcon list[] = new IIcon[6];

		list[0] = Down;
		list[1] = Up;
		list[2] = North;
		list[3] = South;
		list[4] = West;
		list[5] = East;

		this.blk.getRendererInstance().setTemporaryRenderIcons( list[this.mapRotation( ForgeDirection.UP ).ordinal()],
				list[this.mapRotation( ForgeDirection.DOWN ).ordinal()], list[this.mapRotation( ForgeDirection.SOUTH ).ordinal()],
				list[this.mapRotation( ForgeDirection.NORTH ).ordinal()], list[this.mapRotation( ForgeDirection.EAST ).ordinal()],
				list[this.mapRotation( ForgeDirection.WEST ).ordinal()] );
	}

	public ForgeDirection mapRotation(ForgeDirection dir)
	{
		ForgeDirection forward = this.az;
		ForgeDirection up = this.ay;
		ForgeDirection west = ForgeDirection.UNKNOWN;

		if ( forward == null || up == null )
			return dir;

		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		for (ForgeDirection dx : ForgeDirection.VALID_DIRECTIONS)
			if ( dx.offsetX == west_x && dx.offsetY == west_y && dx.offsetZ == west_z )
				west = dx;

		if ( dir.equals( forward ) )
			return ForgeDirection.SOUTH;
		if ( dir.equals( forward.getOpposite() ) )
			return ForgeDirection.NORTH;

		if ( dir.equals( up ) )
			return ForgeDirection.UP;
		if ( dir.equals( up.getOpposite() ) )
			return ForgeDirection.DOWN;

		if ( dir.equals( west ) )
			return ForgeDirection.WEST;
		if ( dir.equals( west.getOpposite() ) )
			return ForgeDirection.EAST;

		return ForgeDirection.UNKNOWN;
	}

	@Override
	public void renderInventoryBox(RenderBlocks renderer)
	{
		renderer.setRenderBounds( this.minX / 16.0, this.minY / 16.0, this.minZ / 16.0, this.maxX / 16.0, this.maxY / 16.0, this.maxZ / 16.0 );
		this.bbr.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), this.blk, null, Tessellator.instance, this.color, renderer );
	}

	@Override
	public void renderInventoryFace(IIcon IIcon, ForgeDirection face, RenderBlocks renderer)
	{
		renderer.setRenderBounds( this.minX / 16.0, this.minY / 16.0, this.minZ / 16.0, this.maxX / 16.0, this.maxY / 16.0, this.maxZ / 16.0 );
		this.setTexture( IIcon );
		this.bbr.renderInvBlock( EnumSet.of( face ), this.blk, null, Tessellator.instance, this.color, renderer );
	}

	@Override
	public void renderBlock(int x, int y, int z, RenderBlocks renderer)
	{
		if ( !this.renderThis() )
			return;

		AEBaseBlock blk = (AEBaseBlock) AEApi.instance().blocks().blockMultiPart.block();
		BlockRenderInfo info = blk.getRendererInstance();
		ForgeDirection forward = BusRenderHelper.instance.az;
		ForgeDirection up = BusRenderHelper.instance.ay;

		renderer.uvRotateBottom = info.getTexture( ForgeDirection.DOWN ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.DOWN, forward, up ) );
		renderer.uvRotateTop = info.getTexture( ForgeDirection.UP ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.UP, forward, up ) );

		renderer.uvRotateEast = info.getTexture( ForgeDirection.EAST ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.EAST, forward, up ) );
		renderer.uvRotateWest = info.getTexture( ForgeDirection.WEST ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.WEST, forward, up ) );

		renderer.uvRotateNorth = info.getTexture( ForgeDirection.NORTH ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.NORTH, forward, up ) );
		renderer.uvRotateSouth = info.getTexture( ForgeDirection.SOUTH ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.SOUTH, forward, up ) );

		this.bbr.renderBlockBounds( renderer, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );

		renderer.renderStandardBlock( blk, x, y, z );
	}

	@Override
	public Block getBlock()
	{
		return AEApi.instance().blocks().blockMultiPart.block();
	}

	public void setRenderColor(int color)
	{
		BlockCableBus blk = (BlockCableBus) AEApi.instance().blocks().blockMultiPart.block();
		blk.setRenderColor( color );
	}

	public void prepareBounds(RenderBlocks renderer)
	{
		this.bbr.renderBlockBounds( renderer, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );
	}

	@Override
	public void setFacesToRender(EnumSet<ForgeDirection> faces)
	{
		BusRenderer.instance.renderer.renderFaces = faces;
	}

	@Override
	public void renderBlockCurrentBounds(int x, int y, int z, RenderBlocks renderer)
	{
		if ( !this.renderThis() )
			return;

		renderer.renderStandardBlock( this.blk, x, y, z );
	}

	@Override
	public void renderFaceCutout(int x, int y, int z, IIcon ico, ForgeDirection face, float edgeThickness, RenderBlocks renderer)
	{
		if ( !this.renderThis() )
			return;

		switch (face)
		{
		case DOWN:
			face = this.ay.getOpposite();
			break;
		case EAST:
			face = this.ax;
			break;
		case NORTH:
			face = this.az.getOpposite();
			break;
		case SOUTH:
			face = this.az;
			break;
		case UP:
			face = this.ay;
			break;
		case WEST:
			face = this.ax.getOpposite();
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}

		this.bbr.renderCutoutFace( this.blk, ico, x, y, z, renderer, face, edgeThickness );
	}

	@Override
	public void renderFace(int x, int y, int z, IIcon ico, ForgeDirection face, RenderBlocks renderer)
	{
		if ( !this.renderThis() )
			return;

		this.prepareBounds( renderer );
		switch (face)
		{
		case DOWN:
			face = this.ay.getOpposite();
			break;
		case EAST:
			face = this.ax;
			break;
		case NORTH:
			face = this.az.getOpposite();
			break;
		case SOUTH:
			face = this.az;
			break;
		case UP:
			face = this.ay;
			break;
		case WEST:
			face = this.ax.getOpposite();
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}

		this.bbr.renderFace( x, y, z, this.blk, ico, renderer, face );
	}

	@Override
	public ForgeDirection getWorldX()
	{
		return this.ax;
	}

	@Override
	public ForgeDirection getWorldY()
	{
		return this.ay;
	}

	@Override
	public ForgeDirection getWorldZ()
	{
		return this.az;
	}

	public void setOrientation(ForgeDirection dx, ForgeDirection dy, ForgeDirection dz)
	{
		this.ax = dx == null ? ForgeDirection.EAST : dx;
		this.ay = dy == null ? ForgeDirection.UP : dy;
		this.az = dz == null ? ForgeDirection.SOUTH : dz;
	}

	public double[] getBounds()
	{
		return new double[] { this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ };
	}

	public void setBounds(double[] bounds)
	{
		if ( bounds == null || bounds.length != 6 )
			return;

		this.minX = bounds[0];
		this.minY = bounds[1];
		this.minZ = bounds[2];
		this.maxX = bounds[3];
		this.maxY = bounds[4];
		this.maxZ = bounds[5];
	}

}
