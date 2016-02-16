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


import java.util.Collection;
import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IAESprite;
import appeng.api.util.ModelGenerator;
import appeng.block.qnb.BlockQuantumBase;
import appeng.client.ItemRenderType;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.qnb.TileQuantumBridge;


public class RenderQNB extends BaseBlockRender<BlockQuantumBase, TileQuantumBridge>
{

	@Override
	public void renderInventory( final BlockQuantumBase block, final ItemStack item, final ModelGenerator renderer, final ItemRenderType type, final Object[] obj )
	{
		final float minPx = 2.0f / 16.0f;
		final float maxPx = 14.0f / 16.0f;
		renderer.setRenderBounds( minPx, minPx, minPx, maxPx, maxPx, maxPx );

		super.renderInventory( block, item, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( final BlockQuantumBase block, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		final TileQuantumBridge tqb = block.getTileEntity( world, pos );
		if( tqb == null )
		{
			return false;
		}

		renderer.setRenderAllFaces( true );

		final IDefinitions definitions = AEApi.instance().definitions();
		final IBlocks blocks = definitions.blocks();
		final IParts parts = definitions.parts();

		for( final Block linkBlock : blocks.quantumLink().maybeBlock().asSet() )
		{
			if( tqb.getBlockType() == linkBlock )
			{
				if( tqb.isFormed() )
				{
					final EnumSet<AEPartLocation> sides = tqb.getConnections();

					this.renderCableAt( 0.11D, world, pos, block, renderer, renderer.getIcon( parts.cableGlass().stack( AEColor.Transparent, 1 ) ), 0.141D, sides );

					final Item transCoveredCable = parts.cableCovered().item( AEColor.Transparent );
					this.renderCableAt( 0.188D, world, pos, block, renderer, renderer.getIcon( parts.cableCovered().stack( AEColor.Transparent, 1 ) ), 0.1875D, sides );
				}

				final float renderMin = 2.0f / 16.0f;
				final float renderMax = 14.0f / 16.0f;
				renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );
				renderer.renderStandardBlock( block, pos );
			}
			else
			{
				if( !tqb.isFormed() )
				{
					final float renderMin = 2.0f / 16.0f;
					final float renderMax = 14.0f / 16.0f;
					renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );
					renderer.renderStandardBlock( block, pos );
				}
				else if( tqb.isCorner() )
				{
					final Item transCoveredCable = parts.cableCovered().item( AEColor.Transparent );
					this.renderCableAt( 0.188D, world, pos, block, renderer, renderer.getIcon( parts.cableCovered().stack( AEColor.Transparent, 1 ) ), 0.05D, tqb.getConnections() );

					float renderMin = 4.0f / 16.0f;
					float renderMax = 12.0f / 16.0f;

					renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );
					renderer.renderStandardBlock( block, pos );

					if( tqb.isPowered() )
					{

						renderMin = 3.9f / 16.0f;
						renderMax = 12.1f / 16.0f;
						renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );

						renderer.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
						final int bn = 15;
						renderer.setBrightness( bn << 20 | bn << 4 );
						for( final EnumFacing side : EnumFacing.VALUES )
						{
							this.renderFace( pos, block, ExtraBlockTextures.BlockQRingCornerLight.getIcon(), renderer, side );
						}
					}
				}
				else
				{
					float renderMin = 2.0f / 16.0f;
					float renderMax = 14.0f / 16.0f;
					renderer.setRenderBounds( 0, renderMin, renderMin, 1, renderMax, renderMax );
					renderer.renderStandardBlock( block, pos );

					renderer.setRenderBounds( renderMin, 0, renderMin, renderMax, 1, renderMax );
					renderer.renderStandardBlock( block, pos );

					renderer.setRenderBounds( renderMin, renderMin, 0, renderMax, renderMax, 1 );
					renderer.renderStandardBlock( block, pos );

					if( tqb.isPowered() )
					{
						renderMin = -0.01f / 16.0f;
						renderMax = 16.01f / 16.0f;
						renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );

						renderer.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
						final int bn = 15;
						renderer.setBrightness( bn << 20 | bn << 4 );
						for( final EnumFacing side : EnumFacing.VALUES )
						{
							this.renderFace( pos, block, ExtraBlockTextures.BlockQRingEdgeLight.getIcon(), renderer, side );
						}
					}
				}
			}
		}

		renderer.setRenderAllFaces( false );
		return true;
	}

	private void renderCableAt( final double thickness, final IBlockAccess world, final BlockPos pos, final BlockQuantumBase block, final ModelGenerator renderer, final IAESprite texture, final double pull, final Collection<AEPartLocation> connections )
	{
		block.getRendererInstance().setTemporaryRenderIcon( texture );

		if( connections.contains( AEPartLocation.WEST ) )
		{
			renderer.setRenderBounds( 0.0D, 0.5D - thickness, 0.5D - thickness, 0.5D - thickness - pull, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		if( connections.contains( AEPartLocation.EAST ) )
		{
			renderer.setRenderBounds( 0.5D + thickness + pull, 0.5D - thickness, 0.5D - thickness, 1.0D, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		if( connections.contains( AEPartLocation.NORTH ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.0D, 0.5D + thickness, 0.5D + thickness, 0.5D - thickness - pull );
			renderer.renderStandardBlock( block, pos );
		}

		if( connections.contains( AEPartLocation.SOUTH ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.5D + thickness + pull, 0.5D + thickness, 0.5D + thickness, 1.0D );
			renderer.renderStandardBlock( block, pos );
		}

		if( connections.contains( AEPartLocation.DOWN ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.0D, 0.5D - thickness, 0.5D + thickness, 0.5D - thickness - pull, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		if( connections.contains( AEPartLocation.UP ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D + thickness + pull, 0.5D - thickness, 0.5D + thickness, 1.0D, 0.5D + thickness );
			renderer.renderStandardBlock( block, pos );
		}

		block.getRendererInstance().setTemporaryRenderIcon( null );
	}
}
