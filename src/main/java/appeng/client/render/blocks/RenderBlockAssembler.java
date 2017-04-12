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


import appeng.api.networking.IGridHost;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.IOrientable;
import appeng.block.crafting.BlockMolecularAssembler;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.TaughtIcon;
import appeng.parts.networking.PartCable;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;


public class RenderBlockAssembler extends BaseBlockRender<BlockMolecularAssembler, TileMolecularAssembler> implements IBoxProvider
{

	public RenderBlockAssembler()
	{
		super( false, 20 );
	}

	@Override
	public void renderInventory( final BlockMolecularAssembler blk, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.setOverrideBlockTexture( blk.getIcon( 0, 0 ) );

		this.setInvRenderBounds( renderer, 2, 14, 0, 14, 16, 2 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 14, 2, 2, 16, 14 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 2, 0, 14, 14, 2, 16 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 0, 2, 16, 2, 14 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 0, 0, 16, 2, 2 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 2, 0, 2, 16, 2 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 0, 2, 2, 2, 16 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 14, 14, 16, 16, 16 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 0, 14, 16, 14, 16 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 14, 0, 16, 16, 14 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 14, 2, 0, 16, 14, 2 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 0, 2, 14, 2, 14, 16 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		this.setInvRenderBounds( renderer, 1, 1, 1, 15, 15, 15 );
		this.renderInvBlock( EnumSet.allOf( ForgeDirection.class ), blk, is, Tessellator.instance, 0xffffff, renderer );

		renderer.setOverrideBlockTexture( null );
	}

	@Override
	public boolean renderInWorld( final BlockMolecularAssembler maBlock, final IBlockAccess world, final int x, final int y, final int z, RenderBlocks renderer )
	{
		final TileMolecularAssembler tma = maBlock.getTileEntity( world, x, y, z );

		if( BlockMolecularAssembler.isBooleanAlphaPass() )
		{
			if( tma.isPowered() )
			{
				this.renderBlockBounds( renderer, 1, 1, 1, 15, 15, 15, ForgeDirection.WEST, ForgeDirection.UP, ForgeDirection.SOUTH );
				final TaughtIcon lights = new TaughtIcon( ExtraBlockTextures.BlockMolecularAssemblerLights.getIcon(), -2.0f );
				Tessellator.instance.setColorRGBA_F( 1, 1, 1, 0.3f );
				Tessellator.instance.setBrightness( 14 << 20 | 14 << 4 );
				renderer.renderFaceXNeg( maBlock, x, y, z, lights );
				renderer.renderFaceXPos( maBlock, x, y, z, lights );
				renderer.renderFaceYNeg( maBlock, x, y, z, lights );
				renderer.renderFaceYPos( maBlock, x, y, z, lights );
				renderer.renderFaceZNeg( maBlock, x, y, z, lights );
				renderer.renderFaceZPos( maBlock, x, y, z, lights );
				return true;
			}
			return false;
		}

		BusRenderer.INSTANCE.getRenderer().blockAccess = renderer.blockAccess;
		renderer = BusRenderer.INSTANCE.getRenderer();

		this.preRenderInWorld( maBlock, world, x, y, z, renderer );

		tma.setLightCache( BusRenderHelper.INSTANCE.useSimplifiedRendering( x, y, z, this, tma.getLightCache() ) );

		BusRenderer.INSTANCE.getRenderer().setFacade( true );
		final IOrientable te = this.getOrientable( maBlock, world, x, y, z );

		final ForgeDirection fdy = te.getUp();
		final ForgeDirection fdz = te.getForward();
		final ForgeDirection fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.renderAllFaces = true;

		this.renderCableAt( 0.11D, world, x, y, z, maBlock, renderer, 0.141D, false );
		this.renderCableAt( 0.188D, world, x, y, z, maBlock, renderer, 0.1875D, true );

		maBlock.getRendererInstance().setTemporaryRenderIcon( maBlock.getIcon( 0, 0 ) );

		this.renderBlockBounds( renderer, 2, 14, 0, 14, 16, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 0, 14, 2, 2, 16, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 2, 0, 14, 14, 2, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 14, 0, 2, 16, 2, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		// sides...
		this.renderBlockBounds( renderer, 0, 0, 0, 16, 2, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 0, 2, 0, 2, 16, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 0, 0, 2, 2, 2, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 0, 14, 14, 16, 16, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 14, 0, 14, 16, 14, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 14, 14, 0, 16, 16, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 14, 2, 0, 16, 14, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 0, 2, 14, 2, 14, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		this.renderBlockBounds( renderer, 1, 1, 1, 15, 15, 15, fdx, fdy, fdz );
		renderer.renderStandardBlock( maBlock, x, y, z );

		BusRenderHelper.INSTANCE.normalRendering();

		maBlock.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.renderAllFaces = false;
		BusRenderer.INSTANCE.getRenderer().setFacade( false );

		this.postRenderInWorld( renderer );

		return true;
	}

	private void renderCableAt( final double thickness, final IBlockAccess world, final int x, final int y, final int z, final BlockMolecularAssembler block, final RenderBlocks renderer, final double pull, final boolean covered )
	{
		IIcon texture = null;

		texture = this.getConnectedCable( world, x, y, z, ForgeDirection.WEST, covered );
		block.getRendererInstance().setTemporaryRenderIcon( texture );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.0D, 0.5D - thickness, 0.5D - thickness, 0.5D - thickness - pull, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		texture = this.getConnectedCable( world, x, y, z, ForgeDirection.EAST, covered );
		block.getRendererInstance().setTemporaryRenderIcon( texture );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D + thickness + pull, 0.5D - thickness, 0.5D - thickness, 1.0D, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		texture = this.getConnectedCable( world, x, y, z, ForgeDirection.NORTH, covered );
		block.getRendererInstance().setTemporaryRenderIcon( texture );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.0D, 0.5D + thickness, 0.5D + thickness, 0.5D - thickness - pull );
			renderer.renderStandardBlock( block, x, y, z );
		}

		texture = this.getConnectedCable( world, x, y, z, ForgeDirection.SOUTH, covered );
		block.getRendererInstance().setTemporaryRenderIcon( texture );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.5D + thickness + pull, 0.5D + thickness, 0.5D + thickness, 1.0D );
			renderer.renderStandardBlock( block, x, y, z );
		}

		texture = this.getConnectedCable( world, x, y, z, ForgeDirection.DOWN, covered );
		block.getRendererInstance().setTemporaryRenderIcon( texture );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.0D, 0.5D - thickness, 0.5D + thickness, 0.5D - thickness - pull, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		texture = this.getConnectedCable( world, x, y, z, ForgeDirection.UP, covered );
		block.getRendererInstance().setTemporaryRenderIcon( texture );
		if( texture != null )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D + thickness + pull, 0.5D - thickness, 0.5D + thickness, 1.0D, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( null );
	}

	private IIcon getConnectedCable( final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection side, final boolean covered )
	{
		final int tileYPos = y + side.offsetY;

		if( -1 < tileYPos && tileYPos < 256 )
		{
			final TileEntity ne = world.getTileEntity( x + side.offsetX, tileYPos, z + side.offsetZ );
			if( ne instanceof IGridHost && ne instanceof IPartHost )
			{
				final IPartHost ph = (IPartHost) ne;
				final IPart pcx = ph.getPart( ForgeDirection.UNKNOWN );

				if( pcx instanceof PartCable )
				{
					final PartCable pc = (PartCable) pcx;

					if( pc.isConnected( side.getOpposite() ) )
					{
						if( covered )
						{
							return pc.getCoveredTexture( pc.getCableColor() );
						}
						return pc.getGlassTexture( pc.getCableColor() );
					}
				}
			}
		}

		return null;
	}

	@Override
	public void getBoxes( final IPartCollisionHelper bch )
	{
		bch.addBox( 0, 0, 0, 16, 16, 16 );
	}
}
