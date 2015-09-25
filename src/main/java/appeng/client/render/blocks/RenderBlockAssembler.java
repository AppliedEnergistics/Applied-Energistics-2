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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IOrientable;
import appeng.block.crafting.BlockMolecularAssembler;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderer;
import appeng.client.render.ModelGenerator;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.IAESprite;
import appeng.client.texture.TaughtIcon;
import appeng.parts.networking.PartCable;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;


public class RenderBlockAssembler extends BaseBlockRender<BlockMolecularAssembler, TileMolecularAssembler> implements IBoxProvider
{

	public RenderBlockAssembler()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( BlockMolecularAssembler blk, ItemStack is, ModelGenerator renderer, ItemRenderType type, Object[] obj )
	{
		renderer.setOverrideBlockTexture( renderer.getIcon( blk.getStateFromMeta( is.getMetadata() ) )[0] );

		this.setInvRenderBounds( renderer, 2, 14, 0, 14, 16, 2 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 14, 2, 2, 16, 14 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 2, 0, 14, 14, 2, 16 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 0, 2, 16, 2, 14 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 0, 0, 16, 2, 2 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 2, 0, 2, 16, 2 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 0, 2, 2, 2, 16 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 14, 14, 16, 16, 16 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 0, 14, 16, 14, 16 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 14, 0, 16, 16, 14 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is,  0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 2, 0, 16, 14, 2 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is,  0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 2, 14, 2, 14, 16 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 1, 1, 1, 15, 15, 15 );
		this.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), blk, is,  0xffffff, renderer );

		renderer.setOverrideBlockTexture( null );
	}

	@Override
	public boolean renderInWorld( BlockMolecularAssembler maBlock, IBlockAccess world, BlockPos pos, ModelGenerator renderer )
	{
		TileMolecularAssembler tma = maBlock.getTileEntity( world, pos );

		if ( renderer.isAlphaPass() )
		{
			if( tma.isPowered() )
			{
				this.renderBlockBounds( renderer, 1, 1, 1, 15, 15, 15, EnumFacing.WEST, EnumFacing.UP, EnumFacing.SOUTH );
				TaughtIcon lights = new TaughtIcon( ExtraBlockTextures.BlockMolecularAssemblerLights.getIcon(), -2.0f );
				renderer.setColorRGBA_F( 1, 1, 1, 0.3f );
				renderer.setBrightness( 14 << 20 | 14 << 4 );
				renderer.renderFaceXNeg( maBlock, pos, lights );
				renderer.renderFaceXPos( maBlock, pos, lights );
				renderer.renderFaceYNeg( maBlock, pos, lights );
				renderer.renderFaceYPos( maBlock, pos, lights );
				renderer.renderFaceZNeg( maBlock, pos, lights );
				renderer.renderFaceZPos( maBlock, pos, lights );
				return true;
			}
			return false;
		}

		//BusRenderer.INSTANCE.renderer.blockAccess = renderer.blockAccess;
		//renderer = BusRenderer.INSTANCE.renderer;
		BusRenderer.INSTANCE.renderer=renderer;
		
		this.preRenderInWorld( maBlock, world, pos, renderer );

		IOrientable te = this.getOrientable( maBlock, world, pos );

		EnumFacing fdy = te.getUp();
		EnumFacing fdz = te.getForward();
		EnumFacing fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.renderAllFaces = true;

		this.renderCableAt( 0.11D, world, pos, maBlock, renderer, 0.141D, false );
		this.renderCableAt( 0.188D, world, pos, maBlock, renderer, 0.1875D, true );

		maBlock.getRendererInstance().setTemporaryRenderIcon(  renderer.getIcon( world.getBlockState( pos ) )[0] );

		this.renderBlockBounds( renderer, 2, 14, 0, 14, 16, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 0, 14, 2, 2, 16, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 2, 0, 14, 14, 2, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 14, 0, 2, 16, 2, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		// sides...
		this.renderBlockBounds( renderer, 0, 0, 0, 16, 2, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 0, 2, 0, 2, 16, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 0, 0, 2, 2, 2, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 0, 14, 14, 16, 16, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 14, 0, 14, 16, 14, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 14, 14, 0, 16, 16, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 14, 2, 0, 16, 14, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 0, 2, 14, 2, 14, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		this.renderBlockBounds( renderer, 1, 1, 1, 15, 15, 15, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, pos );

		maBlock.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.renderAllFaces = false;

		this.postRenderInWorld( renderer );

		return true;
	}

	public void renderCableAt( double thickness, IBlockAccess world, BlockPos pos, BlockMolecularAssembler block, ModelGenerator renderer, double pull, boolean covered )
	{
		IAESprite texture = null;

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, pos, EnumFacing.WEST, covered, renderer ) );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.0D, 0.5D - thickness, 0.5D - thickness, 0.5D - thickness - pull, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, pos, EnumFacing.EAST, covered, renderer ) );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D + thickness + pull, 0.5D - thickness, 0.5D - thickness, 1.0D, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, pos, EnumFacing.NORTH, covered, renderer ) );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.0D, 0.5D + thickness, 0.5D + thickness, 0.5D - thickness - pull );
			renderer.renderStandardBlock( block, pos );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, pos, EnumFacing.SOUTH, covered, renderer ) );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.5D + thickness + pull, 0.5D + thickness, 0.5D + thickness, 1.0D );
			renderer.renderStandardBlock( block, pos );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, pos, EnumFacing.DOWN, covered, renderer ) );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.0D, 0.5D - thickness, 0.5D + thickness, 0.5D - thickness - pull, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, pos, EnumFacing.UP, covered, renderer ) );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D + thickness + pull, 0.5D - thickness, 0.5D + thickness, 1.0D, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		block.getRendererInstance().setTemporaryRenderIcon( null );
	}

	IAESprite getConnectedCable( IBlockAccess world, BlockPos pos, EnumFacing side, boolean covered,ModelGenerator renderer )
	{
		final int tileYPos = pos.getY() + side.getFrontOffsetY();
		if( -1 < tileYPos && tileYPos < 256 )
		{
			TileEntity ne = world.getTileEntity( pos.offset( side ) );
			if( ne instanceof IGridHost && ne instanceof IPartHost )
			{
				IPartHost ph = (IPartHost) ne;
				IPart pcx = ph.getPart( AEPartLocation.INTERNAL );
				if( pcx instanceof PartCable )
				{
					PartCable pc = (PartCable) pcx;
					if( pc.isConnected( side.getOpposite() ) )
					{
						if( covered )
						{
							return pc.getCoveredTexture( pc.getCableColor(),renderer );
						}
						return pc.getGlassTexture( pc.getCableColor(),renderer );
					}
				}
			}
		}

		return null;
	}

	@Override
	public void getBoxes( IPartCollisionHelper bch )
	{
		bch.addBox( 0, 0, 0, 16, 16, 16 );
	}
}
