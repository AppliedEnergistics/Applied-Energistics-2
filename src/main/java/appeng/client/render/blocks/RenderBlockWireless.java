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

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.block.networking.BlockWireless;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.IRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.client.texture.OffsetIcon;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;


public class RenderBlockWireless extends BaseBlockRender<BlockWireless, TileWireless>
{

	BlockPos center;
	private BlockWireless blk;
	private boolean hasChan = false;
	private boolean hasPower = false;

	public RenderBlockWireless()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( BlockWireless blk, ItemStack is, IRenderHelper renderer, ItemRenderType type, Object[] obj )
	{
		this.blk = blk;
		center = new BlockPos(0,0,0);
		this.hasChan = false;
		this.hasPower = false;
		BlockRenderInfo ri = blk.getRendererInstance();

		renderer.renderAllFaces = true;

		IAESprite r = CableBusTextures.PartMonitorSidesStatus.getIcon();
		ri.setTemporaryRenderIcons( r, r, CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), r, r );
		this.renderBlockBounds( renderer, 5, 5, 0, 11, 11, 1, EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		r = CableBusTextures.PartWirelessSides.getIcon();
		ri.setTemporaryRenderIcons( r, r, ExtraBlockTextures.BlockWirelessInside.getIcon(), ExtraBlockTextures.BlockWirelessInside.getIcon(), r, r );
		this.renderBlockBounds( renderer, 5, 5, 1, 11, 11, 2, EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		//renderer.startDrawingQuads();
		ri.setTemporaryRenderIcon( null );
		this.renderTorchAtAngle( renderer, EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH );
		super.postRenderInWorld( renderer );

		ri.setTemporaryRenderIcons( r, r, ExtraBlockTextures.BlockWirelessInside.getIcon(), ExtraBlockTextures.BlockWirelessInside.getIcon(), r, r );

		AEPartLocation[] sides = new AEPartLocation[] { AEPartLocation.EAST, AEPartLocation.WEST, AEPartLocation.UP, AEPartLocation.DOWN };

		int s = 1;

		for( AEPartLocation side : sides )
		{
			this.renderBlockBounds( renderer, 8 + ( side.xOffset != 0 ? side.xOffset * 2 : -2 ), 8 + ( side.yOffset != 0 ? side.yOffset * 2 : -2 ), 2 + ( side.zOffset != 0 ? side.zOffset * 2 : -1 ) + s, 8 + ( side.xOffset != 0 ? side.xOffset * 4 : 2 ), 8 + ( side.yOffset != 0 ? side.yOffset * 4 : 2 ), 2 + ( side.zOffset != 0 ? side.zOffset * 5 : 1 ) + s, EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH );
			this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is,  0xffffff, renderer );
		}

		s = 3;
		for( AEPartLocation side : sides )
		{
			this.renderBlockBounds( renderer, 8 + ( side.xOffset != 0 ? side.xOffset * 4 : -1 ), 8 + ( side.yOffset != 0 ? side.yOffset * 4 : -1 ), 1 + ( side.zOffset != 0 ? side.zOffset * 4 : -1 ) + s, 8 + ( side.xOffset != 0 ? side.xOffset * 5 : 1 ), 8 + ( side.yOffset != 0 ? side.yOffset * 5 : 1 ), 2 + ( side.zOffset != 0 ? side.zOffset * 5 : 1 ) + s, EnumFacing.EAST, EnumFacing.UP, EnumFacing.SOUTH );

			this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );
		}
	}

	@Override
	public boolean renderInWorld( BlockWireless blk, IBlockAccess world, BlockPos pos, IRenderHelper renderer )
	{
		TileWireless tw = blk.getTileEntity( world, pos );
		this.blk = blk;
		if( tw != null )
		{
			this.hasChan = ( tw.clientFlags & ( TileWireless.POWERED_FLAG | TileWireless.CHANNEL_FLAG ) ) == ( TileWireless.POWERED_FLAG | TileWireless.CHANNEL_FLAG );
			this.hasPower = ( tw.clientFlags & TileWireless.POWERED_FLAG ) == TileWireless.POWERED_FLAG;

			BlockRenderInfo ri = blk.getRendererInstance();

			EnumFacing fdy = tw.getUp();
			EnumFacing fdz = tw.getForward();
			EnumFacing fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

			renderer.renderAllFaces = true;

			IAESprite r = CableBusTextures.PartMonitorSidesStatus.getIcon();
			ri.setTemporaryRenderIcons( r, r, CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), r, r );
			this.renderBlockBounds( renderer, 5, 5, 0, 11, 11, 1, fdx, fdy, fdz );
			super.renderInWorld( blk, world, pos, renderer );

			r = CableBusTextures.PartWirelessSides.getIcon();
			ri.setTemporaryRenderIcons( r, r, ExtraBlockTextures.BlockWirelessInside.getIcon(), ExtraBlockTextures.BlockWirelessInside.getIcon(), r, r );
			this.renderBlockBounds( renderer, 5, 5, 1, 11, 11, 2, fdx, fdy, fdz );
			super.renderInWorld( blk, world, pos, renderer );

			center = pos;
			ri.setTemporaryRenderIcon( null );

			this.renderTorchAtAngle( renderer, fdx, fdy, fdz );
			super.postRenderInWorld( renderer );

			ri.setTemporaryRenderIcons( r, r, ExtraBlockTextures.BlockWirelessInside.getIcon(), ExtraBlockTextures.BlockWirelessInside.getIcon(), r, r );

			AEPartLocation[] sides = new AEPartLocation[] { AEPartLocation.EAST, AEPartLocation.WEST, AEPartLocation.UP, AEPartLocation.DOWN };

			int s = 1;

			for( AEPartLocation side : sides )
			{
				this.renderBlockBounds( renderer, 8 + ( side.xOffset != 0 ? side.xOffset * 2 : -2 ), 8 + ( side.yOffset != 0 ? side.yOffset * 2 : -2 ), 2 + ( side.zOffset != 0 ? side.zOffset * 2 : -1 ) + s, 8 + ( side.xOffset != 0 ? side.xOffset * 4 : 2 ), 8 + ( side.yOffset != 0 ? side.yOffset * 4 : 2 ), 2 + ( side.zOffset != 0 ? side.zOffset * 5 : 1 ) + s, fdx, fdy, fdz );
				super.renderInWorld( blk, world, pos, renderer );
			}

			s = 3;
			for( AEPartLocation side : sides )
			{
				this.renderBlockBounds( renderer, 8 + ( side.xOffset != 0 ? side.xOffset * 4 : -1 ), 8 + ( side.yOffset != 0 ? side.yOffset * 4 : -1 ), 1 + ( side.zOffset != 0 ? side.zOffset * 4 : -1 ) + s, 8 + ( side.xOffset != 0 ? side.xOffset * 5 : 1 ), 8 + ( side.yOffset != 0 ? side.yOffset * 5 : 1 ), 2 + ( side.zOffset != 0 ? side.zOffset * 5 : 1 ) + s, fdx, fdy, fdz );
				super.renderInWorld( blk, world, pos, renderer );
			}

			r = CableBusTextures.PartMonitorSidesStatusLights.getIcon();
			// ri.setTemporaryRenderIcons( r, r, ExtraTextures.BlockChargerInside.getIcon(),
			// ExtraTextures.BlockChargerInside.getIcon(), r, r );
			this.renderBlockBounds( renderer, 5, 5, 0, 11, 11, 1, fdx, fdy, fdz );

			if( this.hasChan )
			{
				int l = 14;
				renderer.setBrightness( l << 20 | l << 4 );
				renderer.setColorOpaque_I( AEColor.Transparent.blackVariant );
			}
			else if( this.hasPower )
			{
				int l = 9;
				renderer.setBrightness( l << 20 | l << 4 );
				renderer.setColorOpaque_I( AEColor.Transparent.whiteVariant );
			}
			else
			{
				renderer.setBrightness( 0 );
				renderer.setColorOpaque_I( 0x000000 );
			}

			if( EnumFacing.UP != fdz.getOpposite() )
			{
				super.renderFace( pos, blk, r, renderer, EnumFacing.UP );
			}
			if( EnumFacing.DOWN != fdz.getOpposite() )
			{
				super.renderFace( pos, blk, r, renderer, EnumFacing.DOWN );
			}
			if( EnumFacing.EAST != fdz.getOpposite() )
			{
				super.renderFace( pos, blk, r, renderer, EnumFacing.EAST );
			}
			if( EnumFacing.WEST != fdz.getOpposite() )
			{
				super.renderFace( pos, blk, r, renderer, EnumFacing.WEST );
			}
			if( EnumFacing.SOUTH != fdz.getOpposite() )
			{
				super.renderFace( pos, blk, r, renderer, EnumFacing.SOUTH );
			}
			if( EnumFacing.NORTH != fdz.getOpposite() )
			{
				super.renderFace( pos, blk, r, renderer, EnumFacing.NORTH );
			}

			ri.setTemporaryRenderIcon( null );
			renderer.renderAllFaces = false;
		}

		return true;
	}

	private void renderTorchAtAngle( IRenderHelper renderer, EnumFacing x, EnumFacing y, EnumFacing z )
	{
		IAESprite r = ( this.hasChan ? CableBusTextures.BlockWirelessOn.getIcon() : renderer.getIcon( blk.getDefaultState() )[0] );
		IAESprite sides = new OffsetIcon( r, 0.0f, -2.0f );

		switch( z )
		{
			case DOWN:
				renderer.uvRotateNorth = 3;
				renderer.uvRotateSouth = 3;
				renderer.uvRotateEast = 3;
				renderer.uvRotateWest = 3;
				break;
			case EAST:
				renderer.uvRotateTop = 1;
				renderer.uvRotateBottom = 2;
				renderer.uvRotateEast = 2;
				renderer.uvRotateWest = 1;
				break;
			case NORTH:
				renderer.uvRotateTop = 0;
				renderer.uvRotateBottom = 0;
				renderer.uvRotateNorth = 2;
				renderer.uvRotateSouth = 1;
				break;
			case SOUTH:
				renderer.uvRotateTop = 3;
				renderer.uvRotateBottom = 3;
				renderer.uvRotateNorth = 1;
				renderer.uvRotateSouth = 2;
				break;
			case WEST:
				renderer.uvRotateTop = 2;
				renderer.uvRotateBottom = 1;
				renderer.uvRotateEast = 1;
				renderer.uvRotateWest = 2;
				break;
			default:
				break;
		}

		renderer.setColorOpaque_I( 0xffffff );
		this.renderBlockBounds( renderer, 0, 7, 1, 16, 9, 16, x,y,z );
		this.renderFace( center, this.blk, sides, renderer, y );
		this.renderFace( center, this.blk, sides, renderer, y.getOpposite() );

		this.renderBlockBounds( renderer, 7, 0, 1, 9, 16, 16, x, y, z );
		this.renderFace( center, this.blk, sides, renderer, x );
		this.renderFace( center, this.blk, sides, renderer, x.getOpposite() );

		this.renderBlockBounds( renderer, 7, 7, 1, 9, 9, 10.6, x, y, z );
		this.renderFace( center, this.blk, r, renderer, z );
	}
}
