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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGridHost;
import appeng.api.parts.IBoxProvider;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.block.crafting.BlockMolecularAssembler;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BusRenderHelper;
import appeng.client.render.BusRenderer;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.TaughtIcon;
import appeng.parts.networking.PartCable;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.util.Platform;

public class RenderBlockAssembler extends BaseBlockRender implements IBoxProvider
{

	IIcon getConnectedCable(IBlockAccess world, int x, int y, int z, ForgeDirection side, boolean covered)
	{
		final int tileYPos = y + side.offsetY;
		if ( -1 < tileYPos && tileYPos < 256 )
		{
			TileEntity ne = world.getTileEntity( x + side.offsetX, tileYPos, z + side.offsetZ );
			if ( ne instanceof IGridHost && ne instanceof IPartHost )
			{
				IPartHost ph = (IPartHost) ne;
				IPart pcx = ph.getPart( ForgeDirection.UNKNOWN );
				if ( pcx instanceof PartCable )
				{
					PartCable pc = (PartCable) pcx;
					if ( pc.isConnected( side.getOpposite() ) )
					{
						if ( covered )
							return pc.getCoveredTexture( pc.getCableColor() );
						return pc.getGlassTexture( pc.getCableColor() );
					}
				}
			}
		}

		return null;
	}

	public void renderCableAt(double Thickness, IBlockAccess world, int x, int y, int z, AEBaseBlock block, RenderBlocks renderer, double pull, boolean covered)
	{
		IIcon texture = null;

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, x, y, z, ForgeDirection.WEST, covered ) );
		if ( texture != null )
		{
			renderer.setRenderBounds( 0.0D, 0.5D - Thickness, 0.5D - Thickness, 0.5D - Thickness - pull, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, x, y, z, ForgeDirection.EAST, covered ) );
		if ( texture != null )
		{
			renderer.setRenderBounds( 0.5D + Thickness + pull, 0.5D - Thickness, 0.5D - Thickness, 1.0D, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, x, y, z, ForgeDirection.NORTH, covered ) );
		if ( texture != null )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.0D, 0.5D + Thickness, 0.5D + Thickness, 0.5D - Thickness - pull );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, x, y, z, ForgeDirection.SOUTH, covered ) );
		if ( texture != null )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.5D + Thickness + pull, 0.5D + Thickness, 0.5D + Thickness, 1.0D );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, x, y, z, ForgeDirection.DOWN, covered ) );
		if ( texture != null )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.0D, 0.5D - Thickness, 0.5D + Thickness, 0.5D - Thickness - pull, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( texture = this.getConnectedCable( world, x, y, z, ForgeDirection.UP, covered ) );
		if ( texture != null )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D + Thickness + pull, 0.5D - Thickness, 0.5D + Thickness, 1.0D, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( null );
	}

	public RenderBlockAssembler() {
		super( false, 20 );
	}

	@Override
	public void renderInventory(AEBaseBlock blk, ItemStack is, RenderBlocks renderer, ItemRenderType type, Object[] obj)
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
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		BlockMolecularAssembler blk = (BlockMolecularAssembler) block;
		TileMolecularAssembler tma = blk.getTileEntity( world, x, y, z );

		if ( BlockMolecularAssembler.booleanAlphaPass )
		{
			if ( tma.isPowered() )
			{
				this.renderBlockBounds( renderer, 1, 1, 1, 15, 15, 15, ForgeDirection.WEST, ForgeDirection.UP, ForgeDirection.SOUTH );
				TaughtIcon lights = new TaughtIcon( ExtraBlockTextures.BlockMolecularAssemblerLights.getIcon(), -2.0f );
				Tessellator.instance.setColorRGBA_F( 1, 1, 1, 0.3f );
				Tessellator.instance.setBrightness( 14 << 20 | 14 << 4 );
				renderer.renderFaceXNeg( blk, x, y, z, lights );
				renderer.renderFaceXPos( blk, x, y, z, lights );
				renderer.renderFaceYNeg( blk, x, y, z, lights );
				renderer.renderFaceYPos( blk, x, y, z, lights );
				renderer.renderFaceZNeg( blk, x, y, z, lights );
				renderer.renderFaceZPos( blk, x, y, z, lights );
				return true;
			}
			return false;
		}

		BusRenderer.instance.renderer.blockAccess = renderer.blockAccess;
		renderer = BusRenderer.instance.renderer;

		this.preRenderInWorld( block, world, x, y, z, renderer );

		tma.lightCache = BusRenderHelper.instance.useSimplifiedRendering( x, y, z, this, tma.lightCache );

		BusRenderer.instance.renderer.isFacade = true;
		IOrientable te = this.getOrientable( block, world, x, y, z );

		ForgeDirection fdy = te.getUp();
		ForgeDirection fdz = te.getForward();
		ForgeDirection fdx = Platform.crossProduct( fdz, fdy ).getOpposite();

		renderer.renderAllFaces = true;

		this.renderCableAt( 0.11D, world, x, y, z, block, renderer, 0.141D, false );
		this.renderCableAt( 0.188D, world, x, y, z, block, renderer, 0.1875D, true );

		blk.getRendererInstance().setTemporaryRenderIcon( blk.getIcon( 0, 0 ) );

		this.renderBlockBounds( renderer, 2, 14, 0, 14, 16, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 0, 14, 2, 2, 16, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 2, 0, 14, 14, 2, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 14, 0, 2, 16, 2, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		// sides...
		this.renderBlockBounds( renderer, 0, 0, 0, 16, 2, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 0, 2, 0, 2, 16, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 0, 0, 2, 2, 2, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 0, 14, 14, 16, 16, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 14, 0, 14, 16, 14, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 14, 14, 0, 16, 16, 14, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 14, 2, 0, 16, 14, 2, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 0, 2, 14, 2, 14, 16, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		this.renderBlockBounds( renderer, 1, 1, 1, 15, 15, 15, fdx, fdy, fdz );
		renderer.renderStandardBlock( blk, x, y, z );

		BusRenderHelper.instance.normalRendering();

		blk.getRendererInstance().setTemporaryRenderIcon( null );

		renderer.renderAllFaces = false;
		BusRenderer.instance.renderer.isFacade = false;

		this.postRenderInWorld( renderer );

		return true;
	}
 
	@Override
	public void getBoxes(IPartCollisionHelper bch)
	{
		bch.addBox( 0, 0, 0, 16, 16, 16 );
	}
}
