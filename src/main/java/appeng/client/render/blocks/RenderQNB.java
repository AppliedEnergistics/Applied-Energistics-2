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
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.util.AEColor;
import appeng.block.qnb.BlockQuantumBase;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.qnb.TileQuantumBridge;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Collection;
import java.util.EnumSet;


public class RenderQNB extends BaseBlockRender<BlockQuantumBase, TileQuantumBridge>
{
	private static final float DEFAULT_RENDER_MIN = 2.0f / 16.0f;
	private static final float DEFAULT_RENDER_MAX = 14.0f / 16.0f;

	private static final float CORNER_POWERED_RENDER_MIN = 3.9f / 16.0f;
	private static final float CORNER_POWERED_RENDER_MAX = 12.1f / 16.0f;

	private static final float CENTER_POWERED_RENDER_MIN = -0.01f / 16.0f;
	private static final float CENTER_POWERED_RENDER_MAX = 16.01f / 16.0f;

	@Override
	public void renderInventory( final BlockQuantumBase block, final ItemStack item, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.setRenderBounds( DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX );
		super.renderInventory( block, item, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld( final BlockQuantumBase block, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		final TileQuantumBridge tqb = block.getTileEntity( world, x, y, z );

		if( tqb == null )
		{
			return false;
		}

		renderer.renderAllFaces = true;

		final IDefinitions definitions = AEApi.instance().definitions();
		final IBlocks blocks = definitions.blocks();
		final IParts parts = definitions.parts();

		for( final Block linkBlock : blocks.quantumLink().maybeBlock().asSet() )
		{
			if( tqb.getBlockType() == linkBlock )
			{
				if( tqb.isFormed() )
				{
					final EnumSet<ForgeDirection> sides = tqb.getConnections();

					final Item transGlassCable = parts.cableGlass().item( AEColor.Transparent );
					this.renderCableAt( 0.11D, world, x, y, z, block, renderer, transGlassCable.getIconIndex( parts.cableGlass().stack( AEColor.Transparent, 1 ) ), 0.141D, sides );

					final Item transCoveredCable = parts.cableCovered().item( AEColor.Transparent );
					this.renderCableAt( 0.188D, world, x, y, z, block, renderer, transCoveredCable.getIconIndex( parts.cableCovered().stack( AEColor.Transparent, 1 ) ), 0.1875D, sides );
				}

				renderer.setRenderBounds( DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX );
				renderer.renderStandardBlock( block, x, y, z );
			}
			else
			{
				if( !tqb.isFormed() )
				{
					renderer.setRenderBounds( DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX );
					renderer.renderStandardBlock( block, x, y, z );
				}
				else if( tqb.isCorner() )
				{
					final Item transCoveredCable = parts.cableCovered().item( AEColor.Transparent );

					this.renderCableAt( 0.188D, world, x, y, z, block, renderer, transCoveredCable.getIconIndex( parts.cableCovered().stack( AEColor.Transparent, 1 ) ), 0.05D, tqb.getConnections() );

					renderer.setRenderBounds( DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX );
					renderer.renderStandardBlock( block, x, y, z );

					if( tqb.isPowered() )
					{
						renderer.setRenderBounds( CORNER_POWERED_RENDER_MIN, CORNER_POWERED_RENDER_MIN, CORNER_POWERED_RENDER_MIN, CORNER_POWERED_RENDER_MAX, CORNER_POWERED_RENDER_MAX, CORNER_POWERED_RENDER_MAX );

						Tessellator.instance.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
						final int bn = 15;
						Tessellator.instance.setBrightness( bn << 20 | bn << 4 );
						for( final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
						{
							this.renderFace( x, y, z, block, ExtraBlockTextures.BlockQRingCornerLight.getIcon(), renderer, side );
						}
					}
				}
				else
				{
					renderer.setRenderBounds( 0, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, 1, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX );
					renderer.renderStandardBlock( block, x, y, z );

					renderer.setRenderBounds( DEFAULT_RENDER_MIN, 0, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, 1, DEFAULT_RENDER_MAX );
					renderer.renderStandardBlock( block, x, y, z );

					renderer.setRenderBounds( DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, 0, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, 1 );
					renderer.renderStandardBlock( block, x, y, z );

					if( tqb.isPowered() )
					{
						renderer.setRenderBounds( CENTER_POWERED_RENDER_MIN, CENTER_POWERED_RENDER_MIN, CENTER_POWERED_RENDER_MIN, CENTER_POWERED_RENDER_MAX, CENTER_POWERED_RENDER_MAX, CENTER_POWERED_RENDER_MAX );

						Tessellator.instance.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
						final int bn = 15;
						Tessellator.instance.setBrightness( bn << 20 | bn << 4 );
						for( final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
						{
							this.renderFace( x, y, z, block, ExtraBlockTextures.BlockQRingEdgeLight.getIcon(), renderer, side );
						}
					}
				}
			}
		}

		renderer.renderAllFaces = false;
		return true;
	}

	private void renderCableAt( final double thickness, final IBlockAccess world, final int x, final int y, final int z, final BlockQuantumBase block, final RenderBlocks renderer, final IIcon texture, final double pull, final Collection<ForgeDirection> connections )
	{
		block.getRendererInstance().setTemporaryRenderIcon( texture );

		if( connections.contains( ForgeDirection.UNKNOWN ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.5D - thickness, 0.5D + thickness, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if( connections.contains( ForgeDirection.WEST ) )
		{
			renderer.setRenderBounds( 0.0D, 0.5D - thickness, 0.5D - thickness, 0.5D - thickness - pull, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if( connections.contains( ForgeDirection.EAST ) )
		{
			renderer.setRenderBounds( 0.5D + thickness + pull, 0.5D - thickness, 0.5D - thickness, 1.0D, 0.5D + thickness, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if( connections.contains( ForgeDirection.NORTH ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.0D, 0.5D + thickness, 0.5D + thickness, 0.5D - thickness - pull );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if( connections.contains( ForgeDirection.SOUTH ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D - thickness, 0.5D + thickness + pull, 0.5D + thickness, 0.5D + thickness, 1.0D );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if( connections.contains( ForgeDirection.DOWN ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.0D, 0.5D - thickness, 0.5D + thickness, 0.5D - thickness - pull, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if( connections.contains( ForgeDirection.UP ) )
		{
			renderer.setRenderBounds( 0.5D - thickness, 0.5D + thickness + pull, 0.5D - thickness, 0.5D + thickness, 1.0D, 0.5D + thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( null );
	}
}
