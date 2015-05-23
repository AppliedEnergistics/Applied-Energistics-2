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


import java.util.EnumSet;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.ISimplifiedBundle;
import appeng.block.AEBaseBlock;
import appeng.block.networking.BlockCableBus;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;


@SideOnly( Side.CLIENT )
public final class BusRenderHelper implements IPartRenderHelper
{
	public static final BusRenderHelper INSTANCE = new BusRenderHelper();
	private static final int HEX_WHITE = 0xffffff;

	private final BoundBoxCalculator bbc;
	private final boolean noAlphaPass;
	private final BaseBlockRender bbr;
	private final Optional<Block> maybeBlock;
	private final Optional<AEBaseBlock> maybeBaseBlock;
	private int renderingForPass;
	private int currentPass;
	private int itemsRendered;
	private double minX;
	private double minY;
	private double minZ;
	private double maxX;
	private double maxY;
	private double maxZ;
	private ForgeDirection ax;
	private ForgeDirection ay;
	private ForgeDirection az;
	private int color;

	public BusRenderHelper()
	{
		this.bbc = new BoundBoxCalculator( this );
		this.noAlphaPass = !AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass );
		this.bbr = new BaseBlockRender();
		this.renderingForPass = 0;
		this.currentPass = 0;
		this.itemsRendered = 0;
		this.minX = 0;
		this.minY = 0;
		this.minZ = 0;
		this.maxX = 16;
		this.maxY = 16;
		this.maxZ = 16;
		this.ax = ForgeDirection.EAST;
		this.az = ForgeDirection.SOUTH;
		this.ay = ForgeDirection.UP;
		this.color = HEX_WHITE;
		this.maybeBlock = AEApi.instance().definitions().blocks().multiPart().maybeBlock();
		this.maybeBaseBlock = this.maybeBlock.transform( new BaseBlockTransformFunction() );
	}

	public int getItemsRendered()
	{
		return this.itemsRendered;
	}

	public void setPass( int pass )
	{
		this.renderingForPass = 0;
		this.currentPass = pass;
		this.itemsRendered = 0;
	}

	public double getBound( ForgeDirection side )
	{
		switch( side )
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

	public void setRenderColor( int color )
	{
		for( Block block : AEApi.instance().definitions().blocks().multiPart().maybeBlock().asSet() )
		{
			final BlockCableBus cableBus = (BlockCableBus) block;
			cableBus.setRenderColor( color );
		}
	}

	public void setOrientation( ForgeDirection dx, ForgeDirection dy, ForgeDirection dz )
	{
		this.ax = dx == null ? ForgeDirection.EAST : dx;
		this.ay = dy == null ? ForgeDirection.UP : dy;
		this.az = dz == null ? ForgeDirection.SOUTH : dz;
	}

	public double[] getBounds()
	{
		return new double[] { this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ };
	}

	public void setBounds( double[] bounds )
	{
		if( bounds == null || bounds.length != 6 )
		{
			return;
		}

		this.minX = bounds[0];
		this.minY = bounds[1];
		this.minZ = bounds[2];
		this.maxX = bounds[3];
		this.maxY = bounds[4];
		this.maxZ = bounds[5];
	}

	private static class BoundBoxCalculator implements IPartCollisionHelper
	{
		private final BusRenderHelper helper;
		private boolean started = false;

		private float minX;
		private float minY;
		private float minZ;

		private float maxX;
		private float maxY;
		private float maxZ;

		public BoundBoxCalculator( BusRenderHelper helper )
		{
			this.helper = helper;
		}

		@Override
		public void addBox( double minX, double minY, double minZ, double maxX, double maxY, double maxZ )
		{
			if( this.started )
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
			return this.helper.ax;
		}

		@Override
		public ForgeDirection getWorldY()
		{
			return this.helper.ay;
		}

		@Override
		public ForgeDirection getWorldZ()
		{
			return this.helper.az;
		}

		@Override
		public boolean isBBCollision()
		{
			return false;
		}
	}


	private static final class BaseBlockTransformFunction implements Function<Block, AEBaseBlock>
	{
		@Nullable
		@Override
		public AEBaseBlock apply( Block input )
		{
			if( input instanceof AEBaseBlock )
			{
				return ( (AEBaseBlock) input );
			}

			return null;
		}
	}

	@Override
	public void renderForPass( int pass )
	{
		this.renderingForPass = pass;
	}

	public boolean renderThis()
	{
		if( this.renderingForPass == this.currentPass || this.noAlphaPass )
		{
			this.itemsRendered++;
			return true;
		}
		return false;
	}

	@Override
	public void normalRendering()
	{
		RenderBlocksWorkaround rbw = BusRenderer.INSTANCE.renderer;
		rbw.calculations = true;
		rbw.useTextures = true;
		rbw.enableAO = false;
	}

	@Override
	public ISimplifiedBundle useSimplifiedRendering( int x, int y, int z, IBoxProvider p, ISimplifiedBundle sim )
	{
		RenderBlocksWorkaround rbw = BusRenderer.INSTANCE.renderer;

		if( sim != null && this.maybeBlock.isPresent() && rbw.similarLighting( this.maybeBlock.get(), rbw.blockAccess, x, y, z, sim ) )
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
			if( p == null )
			{
				this.bbc.minX = this.bbc.minY = this.bbc.minZ = 0;
				this.bbc.maxX = this.bbc.maxY = this.bbc.maxZ = 16;
			}
			else
			{
				p.getBoxes( this.bbc );

				if( this.bbc.minX < 1 )
				{
					this.bbc.minX = 1;
				}
				if( this.bbc.minY < 1 )
				{
					this.bbc.minY = 1;
				}
				if( this.bbc.minZ < 1 )
				{
					this.bbc.minZ = 1;
				}

				if( this.bbc.maxX > 15 )
				{
					this.bbc.maxX = 15;
				}
				if( this.bbc.maxY > 15 )
				{
					this.bbc.maxY = 15;
				}
				if( this.bbc.maxZ > 15 )
				{
					this.bbc.maxZ = 15;
				}
			}

			this.setBounds( this.bbc.minX, this.bbc.minY, this.bbc.minZ, this.bbc.maxX, this.bbc.maxY, this.bbc.maxZ );

			this.bbr.renderBlockBounds( rbw, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );

			for( Block block : this.maybeBlock.asSet() )
			{
				rbw.renderStandardBlock( block, x, y, z );
			}

			rbw.faces = EnumSet.allOf( ForgeDirection.class );
			rbw.renderAllFaces = allFaces;
			rbw.calculations = false;
			rbw.useTextures = false;

			return rbw.getLightingCache();
		}
	}

	@Override
	public void setBounds( float minX, float minY, float minZ, float maxX, float maxY, float maxZ )
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	@Override
	public void setInvColor( int newColor )
	{
		this.color = newColor;
	}

	@Override
	public void setTexture( IIcon ico )
	{
		for( AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			baseBlock.getRendererInstance().setTemporaryRenderIcon( ico );
		}
	}

	@Override
	public void setTexture( IIcon down, IIcon up, IIcon north, IIcon south, IIcon west, IIcon east )
	{
		IIcon[] list = new IIcon[6];

		list[0] = down;
		list[1] = up;
		list[2] = north;
		list[3] = south;
		list[4] = west;
		list[5] = east;

		for( AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			baseBlock.getRendererInstance().setTemporaryRenderIcons( list[this.mapRotation( ForgeDirection.UP ).ordinal()], list[this.mapRotation( ForgeDirection.DOWN ).ordinal()], list[this.mapRotation( ForgeDirection.SOUTH ).ordinal()], list[this.mapRotation( ForgeDirection.NORTH ).ordinal()], list[this.mapRotation( ForgeDirection.EAST ).ordinal()], list[this.mapRotation( ForgeDirection.WEST ).ordinal()] );
		}
	}

	public ForgeDirection mapRotation( ForgeDirection dir )
	{
		ForgeDirection forward = this.az;
		ForgeDirection up = this.ay;
		ForgeDirection west = ForgeDirection.UNKNOWN;

		if( forward == null || up == null )
		{
			return dir;
		}

		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		for( ForgeDirection dx : ForgeDirection.VALID_DIRECTIONS )
		{
			if( dx.offsetX == west_x && dx.offsetY == west_y && dx.offsetZ == west_z )
			{
				west = dx;
			}
		}

		if( dir == forward )
		{
			return ForgeDirection.SOUTH;
		}
		if( dir == forward.getOpposite() )
		{
			return ForgeDirection.NORTH;
		}

		if( dir == up )
		{
			return ForgeDirection.UP;
		}
		if( dir == up.getOpposite() )
		{
			return ForgeDirection.DOWN;
		}

		if( dir == west )
		{
			return ForgeDirection.WEST;
		}
		if( dir == west.getOpposite() )
		{
			return ForgeDirection.EAST;
		}

		return ForgeDirection.UNKNOWN;
	}

	@Override
	public void renderInventoryBox( RenderBlocks renderer )
	{
		renderer.setRenderBounds( this.minX / 16.0, this.minY / 16.0, this.minZ / 16.0, this.maxX / 16.0, this.maxY / 16.0, this.maxZ / 16.0 );

		for( AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			this.bbr.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), baseBlock, null, Tessellator.instance, this.color, renderer );
		}
	}

	@Override
	public void renderInventoryFace( IIcon icon, ForgeDirection face, RenderBlocks renderer )
	{
		renderer.setRenderBounds( this.minX / 16.0, this.minY / 16.0, this.minZ / 16.0, this.maxX / 16.0, this.maxY / 16.0, this.maxZ / 16.0 );
		this.setTexture( icon );

		for( AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			this.bbr.renderInvBlock( EnumSet.of( face ), baseBlock, null, Tessellator.instance, this.color, renderer );
		}
	}

	@Override
	public void renderBlock( int x, int y, int z, RenderBlocks renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		for( Block multiPart : AEApi.instance().definitions().blocks().multiPart().maybeBlock().asSet() )
		{
			final AEBaseBlock block = (AEBaseBlock) multiPart;

			BlockRenderInfo info = block.getRendererInstance();
			ForgeDirection forward = BusRenderHelper.INSTANCE.az;
			ForgeDirection up = BusRenderHelper.INSTANCE.ay;

			renderer.uvRotateBottom = info.getTexture( ForgeDirection.DOWN ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.DOWN, forward, up ) );
			renderer.uvRotateTop = info.getTexture( ForgeDirection.UP ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.UP, forward, up ) );

			renderer.uvRotateEast = info.getTexture( ForgeDirection.EAST ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.EAST, forward, up ) );
			renderer.uvRotateWest = info.getTexture( ForgeDirection.WEST ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.WEST, forward, up ) );

			renderer.uvRotateNorth = info.getTexture( ForgeDirection.NORTH ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.NORTH, forward, up ) );
			renderer.uvRotateSouth = info.getTexture( ForgeDirection.SOUTH ).setFlip( BaseBlockRender.getOrientation( ForgeDirection.SOUTH, forward, up ) );

			this.bbr.renderBlockBounds( renderer, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );

			renderer.renderStandardBlock( block, x, y, z );
		}
	}

	@Override
	public Block getBlock()
	{
		for( Block block : AEApi.instance().definitions().blocks().multiPart().maybeBlock().asSet() )
		{
			return block;
		}

		throw new MissingDefinition( "Tried to access the multi part block without it being defined." );
	}

	public void prepareBounds( RenderBlocks renderer )
	{
		this.bbr.renderBlockBounds( renderer, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );
	}

	@Override
	public void setFacesToRender( EnumSet<ForgeDirection> faces )
	{
		BusRenderer.INSTANCE.renderer.renderFaces = faces;
	}

	@Override
	public void renderBlockCurrentBounds( int x, int y, int z, RenderBlocks renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		for( Block block : this.maybeBlock.asSet() )
		{
			renderer.renderStandardBlock( block, x, y, z );
		}
	}

	@Override
	public void renderFaceCutout( int x, int y, int z, IIcon ico, ForgeDirection face, float edgeThickness, RenderBlocks renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		switch( face )
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

		for( Block block : this.maybeBlock.asSet() )
		{
			this.bbr.renderCutoutFace( block, ico, x, y, z, renderer, face, edgeThickness );
		}
	}

	@Override
	public void renderFace( int x, int y, int z, IIcon ico, ForgeDirection face, RenderBlocks renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		this.prepareBounds( renderer );
		switch( face )
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

		for( Block block : this.maybeBlock.asSet() )
		{
			this.bbr.renderFace( x, y, z, block, ico, renderer, face );
		}
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
}
