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

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.crafting.TileCraftingTile;


public class RenderBlockCraftingCPU<B extends BlockCraftingUnit, T extends TileCraftingTile> extends BaseBlockRender<B, T>
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
	public boolean renderInWorld( B blk, IBlockAccess w, BlockPos pos, ModelGenerator renderer )
	{
		boolean formed = false;
		boolean emitsLight = false;

		TileCraftingTile ct = blk.getTileEntity( w, pos );
		if( ct != null && ct.isFormed() )
		{
			formed = true;
			emitsLight = ct.isPowered();
		}
		
		boolean isMonitor = blk.getClass() == BlockCraftingMonitor.class;
		IAESprite theIcon = renderer.getIcon( w.getBlockState( pos ) )[EnumFacing.SOUTH.ordinal()];

		int meta = 1;
		for( Block craftingBlock : AEApi.instance().definitions().blocks().craftingUnit().maybeBlock().asSet() )
		{
			if ( craftingBlock == blk )
				meta = 0;
		}
		
		IAESprite nonForward = theIcon;
		if( isMonitor )
		{
			for( Block craftingBlock : AEApi.instance().definitions().blocks().craftingUnit().maybeBlock().asSet() )
			{
				nonForward = renderer.getIcon( craftingBlock.getDefaultState() )[EnumFacing.SOUTH.ordinal()]; // craftingBlock.getIcon( 0, meta | ( formed ? 8 : 0 ) );
			}
		}

		if( formed && renderer.overrideBlockTexture == null )
		{
			renderer = BusRenderer.INSTANCE.renderer;
			BusRenderHelper i = BusRenderHelper.INSTANCE;

			renderer.blockAccess = w;
			i.setPass( MinecraftForgeClient.getRenderLayer() == EnumWorldBlockLayer.TRANSLUCENT ? 1 : 0 );
			i.setOrientation( EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH );

			float highX = this.isConnected( w, pos, EnumFacing.EAST ) ? 16 : 13.01f;
			float lowX = this.isConnected( w, pos, EnumFacing.WEST ) ? 0 : 2.99f;

			float highY = this.isConnected( w, pos, EnumFacing.UP ) ? 16 : 13.01f;
			float lowY = this.isConnected( w, pos, EnumFacing.DOWN ) ? 0 : 2.99f;

			float highZ = this.isConnected( w, pos, EnumFacing.SOUTH ) ? 16 : 13.01f;
			float lowZ = this.isConnected( w, pos, EnumFacing.NORTH ) ? 0 : 2.99f;

			this.renderCorner( i, renderer, w, pos, EnumFacing.UP, EnumFacing.EAST, EnumFacing.NORTH );
			this.renderCorner( i, renderer, w, pos, EnumFacing.UP, EnumFacing.EAST, EnumFacing.SOUTH );
			this.renderCorner( i, renderer, w, pos, EnumFacing.UP, EnumFacing.WEST, EnumFacing.NORTH );
			this.renderCorner( i, renderer, w, pos, EnumFacing.UP, EnumFacing.WEST, EnumFacing.SOUTH );
			this.renderCorner( i, renderer, w, pos, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.NORTH );
			this.renderCorner( i, renderer, w, pos, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.SOUTH );
			this.renderCorner( i, renderer, w, pos, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.NORTH );
			this.renderCorner( i, renderer, w, pos, EnumFacing.DOWN, EnumFacing.WEST, EnumFacing.SOUTH );

			for( EnumFacing side : EnumFacing.VALUES )
			{
				i.setBounds( this.fso( side, lowX, EnumFacing.WEST ), this.fso( side, lowY, EnumFacing.DOWN ), this.fso( side, lowZ, EnumFacing.NORTH ), this.fso( side, highX, EnumFacing.EAST ), this.fso( side, highY, EnumFacing.UP ), this.fso( side, highZ, EnumFacing.SOUTH ) );
				i.prepareBounds( renderer );

				boolean LocalEmit = emitsLight;
				if( blk instanceof BlockCraftingMonitor && ct.getForward() != side )
				{
					LocalEmit = false;
				}

				this.handleSide( blk, meta, pos, i, renderer, ct.getForward() == side ? theIcon : nonForward, LocalEmit, isMonitor, side, w );
			}

			//BusRenderer.INSTANCE.renderer.isFacade = false;
			i.setFacesToRender( EnumSet.allOf( EnumFacing.class ) );

			return true;
		}
		else
		{
			double a = 0.0 / 16.0;
			double o = 16.0 / 16.0;
			renderer.setRenderBounds( a, a, a, o, o, o );

			return renderer.renderStandardBlock( blk, pos );
		}
	}

	private boolean isConnected( IBlockAccess w, BlockPos pos, EnumFacing side )
	{
		final int tileYPos = pos.getY() + side.getFrontOffsetY();
		if( 0 <= tileYPos && tileYPos <= 255 )
		{
			final TileEntity tile = w.getTileEntity( pos.offset( side ) );

			return tile instanceof TileCraftingTile;
		}
		else
		{
			return false;
		}
	}

	private void renderCorner( BusRenderHelper i, ModelGenerator renderer, IBlockAccess w, BlockPos pos, EnumFacing up, EnumFacing east, EnumFacing south )
	{
		if( this.isConnected( w, pos, up ) )
		{
			return;
		}
		if( this.isConnected( w, pos, east ) )
		{
			return;
		}
		if( this.isConnected( w, pos, south ) )
		{
			return;
		}

		i.setBounds( this.gso( east, 3, EnumFacing.WEST ), this.gso( up, 3, EnumFacing.DOWN ), this.gso( south, 3, EnumFacing.NORTH ), this.gso( east, 13, EnumFacing.EAST ), this.gso( up, 13, EnumFacing.UP ), this.gso( south, 13, EnumFacing.SOUTH ) );
		i.prepareBounds( renderer );
		i.setTexture( ExtraBlockTextures.BlockCraftingUnitRing.getIcon() );
		i.renderBlockCurrentBounds( pos, renderer );
	}

	private float fso( EnumFacing side, float def, EnumFacing target )
	{
		if( side == target )
		{
			if( side.getFrontOffsetX() > 0 || side.getFrontOffsetY() > 0 || side.getFrontOffsetZ() > 0 )
			{
				return 16;
			}
			return 0;
		}
		return def;
	}

	private void handleSide( B blk, int meta, BlockPos pos, BusRenderHelper i, ModelGenerator renderer, IAESprite color, boolean emitsLight, boolean isMonitor, EnumFacing side, IBlockAccess w )
	{
		if( this.isConnected( w, pos, side ) )
		{
			return;
		}

		i.setFacesToRender( EnumSet.of( side ) );

		if( meta == 0 && blk.getClass() == BlockCraftingUnit.class )
		{
			i.setTexture( ExtraBlockTextures.BlockCraftingUnitFit.getIcon() );
			i.renderBlockCurrentBounds( pos, renderer );
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

			i.renderBlockCurrentBounds( pos, renderer );

			if( color != null )
			{
				i.setTexture( color );

				if( !emitsLight )
				{
					if( color == ExtraBlockTextures.BlockCraftingMonitorFit_Light.getIcon() )
					{
						int b = w.getCombinedLight( pos.offset( side ), 0 );

						TileCraftingMonitorTile sr = blk.getTileEntity( w, pos );
						AEColor col = sr.getColor();

						renderer.setBrightness( b );
						renderer.setColorOpaque_I( col.whiteVariant );
						i.renderFace( pos, color, side, renderer );

						renderer.setColorOpaque_I( col.mediumVariant );
						i.renderFace( pos, ExtraBlockTextures.BlockCraftingMonitorFit_Medium.getIcon(), side, renderer );

						renderer.setColorOpaque_I( col.blackVariant );
						i.renderFace( pos, ExtraBlockTextures.BlockCraftingMonitorFit_Dark.getIcon(), side, renderer );
					}
					else
					{
						i.renderBlockCurrentBounds( pos, renderer );
					}
				}
				else
				{
					if( isMonitor )
					{
						TileCraftingMonitorTile sr = blk.getTileEntity( w, pos );
						AEColor col = sr.getColor();
						
						renderer.setColorOpaque_I( col.whiteVariant );
						renderer.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( pos, color, side, renderer );

						renderer.setColorOpaque_I( col.mediumVariant );
						renderer.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( pos, ExtraBlockTextures.BlockCraftingMonitorFit_Medium.getIcon(), side, renderer );

						renderer.setColorOpaque_I( col.blackVariant );
						renderer.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( pos, ExtraBlockTextures.BlockCraftingMonitorFit_Dark.getIcon(), side, renderer );
					}
					else
					{
						AEPartLocation aeDir = AEPartLocation.fromFacing( side );
						renderer.setColorOpaque_F( 1.0f, 1.0f, 1.0f );
						renderer.setBrightness( 13 << 20 | 13 << 4 );
						i.renderFace( pos, color, side, renderer );
					}
				}
			}
		}

		for( EnumFacing a : EnumFacing.VALUES )
		{
			if( a == side || a == side.getOpposite() )
			{
				continue;
			}

			if( ( side.getFrontOffsetX() != 0 || side.getFrontOffsetZ() != 0 ) && ( a == EnumFacing.NORTH || a == EnumFacing.EAST || a == EnumFacing.WEST || a == EnumFacing.SOUTH ) )
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			}
			else if( ( side.getFrontOffsetY() != 0 ) && ( a == EnumFacing.EAST || a == EnumFacing.WEST ) )
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLongRotated.getIcon() );
			}
			else
			{
				i.setTexture( ExtraBlockTextures.BlockCraftingUnitRingLong.getIcon() );
			}

			AEPartLocation dir = AEPartLocation.fromFacing( a );
			if( !( i.getBound( dir ) < 0.001 || i.getBound( dir ) > 15.999 ) )
			{
				double width = 3.0 / 16.0;
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
					default:
				}

				i.renderBlockCurrentBounds( pos, renderer );
				i.prepareBounds( renderer );
			}
		}
	}

	private float gso( EnumFacing side, float def, EnumFacing target )
	{
		if( side != target )
		{
			if( side.getFrontOffsetX() > 0 || side.getFrontOffsetY() > 0 || side.getFrontOffsetZ() > 0 )
			{
				return 16;
			}
			return 0;
		}
		return def;
	}
}
