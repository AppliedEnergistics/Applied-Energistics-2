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


import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.crafting.TileCraftingTile;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class RenderBlockCraftingCPU<B extends BlockCraftingUnit, T extends TileCraftingTile> extends BaseBlockRender<B, T>
{

	protected RenderBlockCraftingCPU( final boolean useTESR, final int range )
	{
		super( useTESR, range );
	}

	public RenderBlockCraftingCPU()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( final B blk, final IBlockAccess w, final int x, final int y, final int z, RenderBlocks renderer )
	{
		final TileCraftingTile craftingTile = blk.getTileEntity( w, x, y, z );

		if( craftingTile == null )
		{
			return false;
		}

		final boolean formed = craftingTile.isFormed();
		final boolean emitsLight = craftingTile.isPowered();
		final int meta = w.getBlockMetadata( x, y, z ) & 3;
		final boolean isMonitor = blk.getClass() == BlockCraftingMonitor.class;
		final IIcon theIcon = blk.getIcon( ForgeDirection.SOUTH.ordinal(), meta | ( formed ? 8 : 0 ) );
		IIcon nonForward = theIcon;

		if( isMonitor )
		{
			for( final Block craftingBlock : AEApi.instance().definitions().blocks().craftingUnit().maybeBlock().asSet() )
			{
				nonForward = craftingBlock.getIcon( 0, meta | ( formed ? 8 : 0 ) );
			}
		}

		if( formed && renderer.overrideBlockTexture == null )
		{
			renderer = BusRenderer.INSTANCE.getRenderer();
			final BusRenderHelper i = BusRenderHelper.INSTANCE;
			BusRenderer.INSTANCE.getRenderer().setFacade( true );

			renderer.blockAccess = w;
			i.setPass( 0 );
			i.setOrientation( ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH );

			try
			{
				craftingTile.setLightCache( i.useSimplifiedRendering( x, y, z, null, craftingTile.getLightCache() ) );
			}
			catch( final Throwable ignored )
			{

			}

			final float highX = this.isConnected( w, x, y, z, ForgeDirection.EAST ) ? 16 : 13.01f;
			final float lowX = this.isConnected( w, x, y, z, ForgeDirection.WEST ) ? 0 : 2.99f;

			final float highY = this.isConnected( w, x, y, z, ForgeDirection.UP ) ? 16 : 13.01f;
			final float lowY = this.isConnected( w, x, y, z, ForgeDirection.DOWN ) ? 0 : 2.99f;

			final float highZ = this.isConnected( w, x, y, z, ForgeDirection.SOUTH ) ? 16 : 13.01f;
			final float lowZ = this.isConnected( w, x, y, z, ForgeDirection.NORTH ) ? 0 : 2.99f;

			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.SOUTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.SOUTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.SOUTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.WEST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.WEST, ForgeDirection.SOUTH );

			for( final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
			{
				i.setBounds( this.fso( side, lowX, ForgeDirection.WEST ), this.fso( side, lowY, ForgeDirection.DOWN ), this.fso( side, lowZ, ForgeDirection.NORTH ), this.fso( side, highX, ForgeDirection.EAST ), this.fso( side, highY, ForgeDirection.UP ), this.fso( side, highZ, ForgeDirection.SOUTH ) );
				i.prepareBounds( renderer );

				boolean localEmit = emitsLight;

				if( blk instanceof BlockCraftingMonitor && craftingTile.getForward() != side )
				{
					localEmit = false;
				}

				this.handleSide( blk, meta, x, y, z, i, renderer, craftingTile.getForward() == side ? theIcon : nonForward, localEmit, isMonitor, side, w );
			}

			BusRenderer.INSTANCE.getRenderer().setFacade( false );
			i.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
			i.normalRendering();

			return true;
		}
		else
		{
			final double a = 0.0 / 16.0;
			final double o = 16.0 / 16.0;
			renderer.setRenderBounds( a, a, a, o, o, o );

			return renderer.renderStandardBlock( blk, x, y, z );
		}
	}

	private boolean isConnected( final IBlockAccess w, final int x, final int y, final int z, final ForgeDirection side )
	{
		final int tileYPos = y + side.offsetY;

		if( 0 <= tileYPos && tileYPos <= 255 )
		{
			final TileEntity tile = w.getTileEntity( x + side.offsetX, tileYPos, z + side.offsetZ );

			return tile instanceof TileCraftingTile;
		}
		else
		{
			return false;
		}
	}

	private void renderCorner( final BusRenderHelper i, final RenderBlocks renderer, final IBlockAccess w, final int x, final int y, final int z, final ForgeDirection up, final ForgeDirection east, final ForgeDirection south )
	{
		if( this.isConnected( w, x, y, z, up ) || this.isConnected( w, x, y, z, east ) || this.isConnected( w, x, y, z, south ) )
		{
			return;
		}

		i.setBounds( this.gso( east, 3, ForgeDirection.WEST ), this.gso( up, 3, ForgeDirection.DOWN ), this.gso( south, 3, ForgeDirection.NORTH ), this.gso( east, 13, ForgeDirection.EAST ), this.gso( up, 13, ForgeDirection.UP ), this.gso( south, 13, ForgeDirection.SOUTH ) );
		i.prepareBounds( renderer );
		i.setTexture( ExtraBlockTextures.BlockCraftingUnitRing.getIcon() );
		i.renderBlockCurrentBounds( x, y, z, renderer );
	}

	private float fso( final ForgeDirection side, final float def, final ForgeDirection target )
	{
		if( side == target )
		{
			if( side.offsetX > 0 || side.offsetY > 0 || side.offsetZ > 0 )
			{
				return 16;
			}
			return 0;
		}
		return def;
	}

	private void handleSide( final B blk, final int meta, final int x, final int y, final int z, final BusRenderHelper i, final RenderBlocks renderer, final IIcon color, final boolean emitsLight, final boolean isMonitor, final ForgeDirection side, final IBlockAccess w )
	{
		if( this.isConnected( w, x, y, z, side ) )
		{
			return;
		}

		i.setFacesToRender( EnumSet.of( side ) );

		if( meta == 0 && blk.getClass() == BlockCraftingUnit.class )
		{
			i.setTexture( ExtraBlockTextures.BlockCraftingUnitFit.getIcon() );
			i.renderBlockCurrentBounds( x, y, z, renderer );
		}
		else
		{
			if( color == ExtraBlockTextures.BlockCraftingMonitorFit_Light.getIcon() )
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingMonitorOuter.getIcon() );
			}
			else
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingFitSolid.getIcon() );
			}

			i.renderBlockCurrentBounds( x, y, z, renderer );

			if( color != null )
			{
				i.setTexture( color );

				if( !emitsLight )
				{
					if( color == ExtraBlockTextures.BlockCraftingMonitorFit_Light.getIcon() )
					{
						final int b = w.getLightBrightnessForSkyBlocks( x + side.offsetX, y + side.offsetY, z + side.offsetZ, 0 );

						final TileCraftingMonitorTile sr = blk.getTileEntity( w, x, y, z );
						final AEColor col = sr.getColor();

						Tessellator.instance.setBrightness( b );
						Tessellator.instance.setColorOpaque_I( col.whiteVariant );
						i.renderFace( x, y, z, color, side, renderer );

						Tessellator.instance.setColorOpaque_I( col.mediumVariant );
						i.renderFace( x, y, z, ExtraBlockTextures.BlockCraftingMonitorFit_Medium.getIcon(), side, renderer );

						Tessellator.instance.setColorOpaque_I( col.blackVariant );
						i.renderFace( x, y, z, ExtraBlockTextures.BlockCraftingMonitorFit_Dark.getIcon(), side, renderer );
					}
					else
					{
						i.renderBlockCurrentBounds( x, y, z, renderer );
					}
				}
				else
				{
					if( isMonitor )
					{
						final TileCraftingMonitorTile sr = blk.getTileEntity( w, x, y, z );
						final AEColor col = sr.getColor();

						Tessellator.instance.setColorOpaque_I( col.whiteVariant );
						Tessellator.instance.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( x, y, z, color, side, renderer );

						Tessellator.instance.setColorOpaque_I( col.mediumVariant );
						Tessellator.instance.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( x, y, z, ExtraBlockTextures.BlockCraftingMonitorFit_Medium.getIcon(), side, renderer );

						Tessellator.instance.setColorOpaque_I( col.blackVariant );
						Tessellator.instance.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( x, y, z, ExtraBlockTextures.BlockCraftingMonitorFit_Dark.getIcon(), side, renderer );
					}
					else
					{
						Tessellator.instance.setColorOpaque_F( 1.0f, 1.0f, 1.0f );
						Tessellator.instance.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( x, y, z, color, side, renderer );
					}
				}
			}
		}

		for( final ForgeDirection a : ForgeDirection.VALID_DIRECTIONS )
		{
			if( a == side || a == side.getOpposite() )
			{
				continue;
			}

			if( ( side.offsetX != 0 || side.offsetZ != 0 ) && ( a == ForgeDirection.NORTH || a == ForgeDirection.EAST || a == ForgeDirection.WEST || a == ForgeDirection.SOUTH ) )
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			}
			else if( ( side.offsetY != 0 ) && ( a == ForgeDirection.EAST || a == ForgeDirection.WEST ) )
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			}
			else
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLong.getIcon() );
			}

			if( !( i.getBound( a ) < 0.001 || i.getBound( a ) > 15.999 ) )
			{
				final double width = 3.0 / 16.0;

				switch( a )
				{
					case DOWN:
						renderer.renderMinY = 0;
						renderer.renderMaxY = width;
						break;
					case EAST:
						renderer.renderMaxX = 1;
						renderer.renderMinX = 1.0 - width;
						renderer.uvRotateTop = 1;
						renderer.uvRotateBottom = 1;
						renderer.uvRotateWest = 1;
						renderer.uvRotateEast = 1;
						break;
					case NORTH:
						renderer.renderMinZ = 0;
						renderer.renderMaxZ = width;
						renderer.uvRotateWest = 1;
						renderer.uvRotateNorth = 1;
						renderer.uvRotateSouth = 1;
						break;
					case SOUTH:
						renderer.renderMaxZ = 1;
						renderer.renderMinZ = 1.0 - width;
						renderer.uvRotateNorth = 1;
						renderer.uvRotateSouth = 1;
						break;
					case UP:
						renderer.renderMaxY = 1;
						renderer.renderMinY = 1.0 - width;
						break;
					case WEST:
						renderer.renderMinX = 0;
						renderer.renderMaxX = width;
						renderer.uvRotateTop = 1;
						renderer.uvRotateBottom = 1;
						renderer.uvRotateWest = 1;
						renderer.uvRotateEast = 1;
						break;
					case UNKNOWN:
					default:
				}

				i.renderBlockCurrentBounds( x, y, z, renderer );
				i.prepareBounds( renderer );
			}
		}
	}

	private float gso( final ForgeDirection side, final float def, final ForgeDirection target )
	{
		if( side != target )
		{
			if( side.offsetX > 0 || side.offsetY > 0 || side.offsetZ > 0 )
			{
				return 16;
			}
			return 0;
		}
		return def;
	}
}
