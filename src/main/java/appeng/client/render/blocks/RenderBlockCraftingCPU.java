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


import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.crafting.TileCraftingTile;


public class RenderBlockCraftingCPU extends BaseBlockRender
{

	protected RenderBlockCraftingCPU( boolean useTESR, int range )
	{
		super( useTESR, range );
	}

	public RenderBlockCraftingCPU()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( AEBaseBlock blk, IBlockAccess w, int x, int y, int z, RenderBlocks renderer )
	{
		IIcon theIcon = null;
		boolean formed = false;
		boolean emitsLight = false;

		TileCraftingTile ct = blk.getTileEntity( w, x, y, z );
		if( ct != null && ct.isFormed() )
		{
			formed = true;
			emitsLight = ct.isPowered();
		}
		int meta = w.getBlockMetadata( x, y, z ) & 3;

		boolean isMonitor = blk.getClass() == BlockCraftingMonitor.class;
		theIcon = blk.getIcon( ForgeDirection.SOUTH.ordinal(), meta | ( formed ? 8 : 0 ) );

		IIcon nonForward = theIcon;
		if( isMonitor )
			nonForward = AEApi.instance().blocks().blockCraftingUnit.block().getIcon( 0, meta | ( formed ? 8 : 0 ) );

		if( formed && renderer.overrideBlockTexture == null )
		{
			renderer = BusRenderer.INSTANCE.renderer;
			BusRenderHelper i = BusRenderHelper.INSTANCE;
			BusRenderer.INSTANCE.renderer.isFacade = true;

			renderer.blockAccess = w;
			i.setPass( 0 );
			i.setOrientation( ForgeDirection.EAST, ForgeDirection.UP, ForgeDirection.SOUTH );

			try
			{
				ct.lightCache = i.useSimplifiedRendering( x, y, z, null, ct.lightCache );
			}
			catch( Throwable ignored )
			{

			}

			float highX = this.isConnected( w, x, y, z, ForgeDirection.EAST ) ? 16 : 13.01f;
			float lowX = this.isConnected( w, x, y, z, ForgeDirection.WEST ) ? 0 : 2.99f;

			float highY = this.isConnected( w, x, y, z, ForgeDirection.UP ) ? 16 : 13.01f;
			float lowY = this.isConnected( w, x, y, z, ForgeDirection.DOWN ) ? 0 : 2.99f;

			float highZ = this.isConnected( w, x, y, z, ForgeDirection.SOUTH ) ? 16 : 13.01f;
			float lowZ = this.isConnected( w, x, y, z, ForgeDirection.NORTH ) ? 0 : 2.99f;

			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.EAST, ForgeDirection.SOUTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.UP, ForgeDirection.WEST, ForgeDirection.SOUTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.EAST, ForgeDirection.SOUTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.WEST, ForgeDirection.NORTH );
			this.renderCorner( i, renderer, w, x, y, z, ForgeDirection.DOWN, ForgeDirection.WEST, ForgeDirection.SOUTH );

			for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
			{
				i.setBounds( this.fso( side, lowX, ForgeDirection.WEST ), this.fso( side, lowY, ForgeDirection.DOWN ), this.fso( side, lowZ, ForgeDirection.NORTH ), this.fso( side, highX, ForgeDirection.EAST ), this.fso( side, highY, ForgeDirection.UP ), this.fso( side, highZ, ForgeDirection.SOUTH ) );
				i.prepareBounds( renderer );

				boolean LocalEmit = emitsLight;
				if( blk instanceof BlockCraftingMonitor && ct.getForward() != side )
					LocalEmit = false;

				this.handleSide( blk, meta, x, y, z, i, renderer, ct.getForward() == side ? theIcon : nonForward, LocalEmit, isMonitor, side, w );
			}

			BusRenderer.INSTANCE.renderer.isFacade = false;
			i.setFacesToRender( EnumSet.allOf( ForgeDirection.class ) );
			i.normalRendering();

			return true;
		}
		else
		{
			double a = 0.0 / 16.0;
			double o = 16.0 / 16.0;
			renderer.setRenderBounds( a, a, a, o, o, o );

			return renderer.renderStandardBlock( blk, x, y, z );
		}
	}

	private boolean isConnected( IBlockAccess w, int x, int y, int z, ForgeDirection side )
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

	private void renderCorner( BusRenderHelper i, RenderBlocks renderer, IBlockAccess w, int x, int y, int z, ForgeDirection up, ForgeDirection east, ForgeDirection south )
	{
		if( this.isConnected( w, x, y, z, up ) )
			return;
		if( this.isConnected( w, x, y, z, east ) )
			return;
		if( this.isConnected( w, x, y, z, south ) )
			return;

		i.setBounds( this.gso( east, 3, ForgeDirection.WEST ), this.gso( up, 3, ForgeDirection.DOWN ), this.gso( south, 3, ForgeDirection.NORTH ), this.gso( east, 13, ForgeDirection.EAST ), this.gso( up, 13, ForgeDirection.UP ), this.gso( south, 13, ForgeDirection.SOUTH ) );
		i.prepareBounds( renderer );
		i.setTexture( ExtraBlockTextures.BlockCraftingUnitRing.getIcon() );
		i.renderBlockCurrentBounds( x, y, z, renderer );
	}

	private float fso( ForgeDirection side, float def, ForgeDirection target )
	{
		if( side == target )
		{
			if( side.offsetX > 0 || side.offsetY > 0 || side.offsetZ > 0 )
				return 16;
			return 0;
		}
		return def;
	}

	private void handleSide( AEBaseBlock blk, int meta, int x, int y, int z, BusRenderHelper i, RenderBlocks renderer, IIcon color, boolean emitsLight, boolean isMonitor, ForgeDirection side, IBlockAccess w )
	{
		if( this.isConnected( w, x, y, z, side ) )
			return;

		i.setFacesToRender( EnumSet.of( side ) );

		if( meta == 0 && blk.getClass() == BlockCraftingUnit.class )
		{
			i.setTexture( ExtraBlockTextures.BlockCraftingUnitFit.getIcon() );
			i.renderBlockCurrentBounds( x, y, z, renderer );
		}
		else
		{
			if( color == ExtraBlockTextures.BlockCraftingMonitorFit_Light.getIcon() )
				i.setTexture( ExtraBlockTextures.BlockCraftingMonitorOuter.getIcon() );
			else
				i.setTexture( ExtraBlockTextures.BlockCraftingFitSolid.getIcon() );

			i.renderBlockCurrentBounds( x, y, z, renderer );

			if( color != null )
			{
				i.setTexture( color );

				if( !emitsLight )
				{
					if( color == ExtraBlockTextures.BlockCraftingMonitorFit_Light.getIcon() )
					{
						int b = w.getLightBrightnessForSkyBlocks( x + side.offsetX, y + side.offsetY, z + side.offsetZ, 0 );

						TileCraftingMonitorTile sr = blk.getTileEntity( w, x, y, z );
						AEColor col = sr.getColor();

						Tessellator.instance.setBrightness( b );
						Tessellator.instance.setColorOpaque_I( col.whiteVariant );
						i.renderFace( x, y, z, color, side, renderer );

						Tessellator.instance.setColorOpaque_I( col.mediumVariant );
						i.renderFace( x, y, z, ExtraBlockTextures.BlockCraftingMonitorFit_Medium.getIcon(), side, renderer );

						Tessellator.instance.setColorOpaque_I( col.blackVariant );
						i.renderFace( x, y, z, ExtraBlockTextures.BlockCraftingMonitorFit_Dark.getIcon(), side, renderer );
					}
					else
						i.renderBlockCurrentBounds( x, y, z, renderer );
				}
				else
				{
					if( isMonitor )
					{
						TileCraftingMonitorTile sr = blk.getTileEntity( w, x, y, z );
						AEColor col = sr.getColor();

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

		for( ForgeDirection a : ForgeDirection.VALID_DIRECTIONS )
		{
			if( a == side || a == side.getOpposite() )
				continue;

			if( ( side.offsetX != 0 || side.offsetZ != 0 ) && ( a == ForgeDirection.NORTH || a == ForgeDirection.EAST || a == ForgeDirection.WEST || a == ForgeDirection.SOUTH ) )
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			else if( ( side.offsetY != 0 ) && ( a == ForgeDirection.EAST || a == ForgeDirection.WEST ) )
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			else
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLong.getIcon() );

			double width = 3.0 / 16.0;

			if( !( i.getBound( a ) < 0.001 || i.getBound( a ) > 15.999 ) )
			{
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

	private float gso( ForgeDirection side, float def, ForgeDirection target )
	{
		if( side != target )
		{
			if( side.offsetX > 0 || side.offsetY > 0 || side.offsetZ > 0 )
				return 16;
			return 0;
		}
		return def;
	}
}
