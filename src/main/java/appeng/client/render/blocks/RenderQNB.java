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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.qnb.TileQuantumBridge;

public class RenderQNB extends BaseBlockRender
{

	public void renderCableAt(double Thickness, IBlockAccess world, int x, int y, int z, AEBaseBlock block, RenderBlocks renderer, IIcon texture, double pull,
			EnumSet<ForgeDirection> connections)
	{
		block.getRendererInstance().setTemporaryRenderIcon( texture );

		if ( connections.contains( ForgeDirection.UNKNOWN ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.5D - Thickness, 0.5D + Thickness, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.WEST ) )
		{
			renderer.setRenderBounds( 0.0D, 0.5D - Thickness, 0.5D - Thickness, 0.5D - Thickness - pull, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.EAST ) )
		{
			renderer.setRenderBounds( 0.5D + Thickness + pull, 0.5D - Thickness, 0.5D - Thickness, 1.0D, 0.5D + Thickness, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.NORTH ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.0D, 0.5D + Thickness, 0.5D + Thickness, 0.5D - Thickness - pull );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.SOUTH ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D - Thickness, 0.5D + Thickness + pull, 0.5D + Thickness, 0.5D + Thickness, 1.0D );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.DOWN ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.0D, 0.5D - Thickness, 0.5D + Thickness, 0.5D - Thickness - pull, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		if ( connections.contains( ForgeDirection.UP ) )
		{
			renderer.setRenderBounds( 0.5D - Thickness, 0.5D + Thickness + pull, 0.5D - Thickness, 0.5D + Thickness, 1.0D, 0.5D + Thickness );
			renderer.renderStandardBlock( block, x, y, z );
		}

		block.getRendererInstance().setTemporaryRenderIcon( null );
	}

	@Override
	public void renderInventory(AEBaseBlock block, ItemStack item, RenderBlocks renderer, ItemRenderType type, Object[] obj)
	{
		float minPx = 2.0f / 16.0f;
		float maxPx = 14.0f / 16.0f;
		renderer.setRenderBounds( minPx, minPx, minPx, maxPx, maxPx, maxPx );

		super.renderInventory( block, item, renderer, type, obj );
	}

	@Override
	public boolean renderInWorld(AEBaseBlock block, IBlockAccess world, int x, int y, int z, RenderBlocks renderer)
	{
		TileQuantumBridge tqb = block.getTileEntity( world, x, y, z );
		if ( tqb == null )
			return false;

		renderer.renderAllFaces = true;

		if ( tqb.getBlockType() == AEApi.instance().blocks().blockQuantumLink.block() )
		{
			if ( tqb.isFormed() )
			{
				AEColoredItemDefinition glassCableDefinition = AEApi.instance().parts().partCableGlass;
				Item transparentGlassCable = glassCableDefinition.item( AEColor.Transparent );

				AEColoredItemDefinition coveredCableDefinition = AEApi.instance().parts().partCableCovered;
				Item transparentCoveredCable = coveredCableDefinition.item( AEColor.Transparent );

				EnumSet<ForgeDirection> sides = tqb.getConnections();
				renderCableAt( 0.11D, world, x, y, z, block, renderer, transparentGlassCable.getIconIndex( glassCableDefinition.stack( AEColor.Transparent, 1 ) ), 0.141D, sides );
				renderCableAt( 0.188D, world, x, y, z, block, renderer, transparentCoveredCable.getIconIndex( coveredCableDefinition.stack( AEColor.Transparent, 1 ) ), 0.1875D, sides );
			}

			float renderMin = 2.0f / 16.0f;
			float renderMax = 14.0f / 16.0f;
			renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );
			renderer.renderStandardBlock( block, x, y, z );
			// super.renderWorldBlock(world, x, y, z, block, modelId, renderer);
		}
		else
		{
			if ( !tqb.isFormed() )
			{
				float renderMin = 2.0f / 16.0f;
				float renderMax = 14.0f / 16.0f;
				renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );
				renderer.renderStandardBlock( block, x, y, z );
			}
			else if ( tqb.isCorner() )
			{
				// renderCableAt(0.11D, world, x, y, z, block, modelId,
				// renderer,
				// AppEngTextureRegistry.Blocks.MECable.get(), true, 0.0D);
				AEColoredItemDefinition coveredCableDefinition = AEApi.instance().parts().partCableCovered;
				Item transparentCoveredCable = coveredCableDefinition.item( AEColor.Transparent );

				renderCableAt( 0.188D, world, x, y, z, block, renderer, transparentCoveredCable.getIconIndex( coveredCableDefinition.stack( AEColor.Transparent, 1 ) ), 0.05D,
						tqb.getConnections() );

				float renderMin = 4.0f / 16.0f;
				float renderMax = 12.0f / 16.0f;

				renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );
				renderer.renderStandardBlock( block, x, y, z );

				if ( tqb.isPowered() )
				{

					renderMin = 3.9f / 16.0f;
					renderMax = 12.1f / 16.0f;
					renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );

					int bn = 15;
					Tessellator.instance.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
					Tessellator.instance.setBrightness( bn << 20 | bn << 4 );
					for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
						renderFace( x, y, z, block, ExtraBlockTextures.BlockQRingCornerLight.getIcon(), renderer, side );

				}
			}
			else
			{
				float renderMin = 2.0f / 16.0f;
				float renderMax = 14.0f / 16.0f;
				renderer.setRenderBounds( 0, renderMin, renderMin, 1, renderMax, renderMax );
				renderer.renderStandardBlock( block, x, y, z );

				renderer.setRenderBounds( renderMin, 0, renderMin, renderMax, 1, renderMax );
				renderer.renderStandardBlock( block, x, y, z );

				renderer.setRenderBounds( renderMin, renderMin, 0, renderMax, renderMax, 1 );
				renderer.renderStandardBlock( block, x, y, z );

				if ( tqb.isPowered() )
				{
					renderMin = -0.01f / 16.0f;
					renderMax = 16.01f / 16.0f;
					renderer.setRenderBounds( renderMin, renderMin, renderMin, renderMax, renderMax, renderMax );

					int bn = 15;
					Tessellator.instance.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
					Tessellator.instance.setBrightness( bn << 20 | bn << 4 );
					for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
						renderFace( x, y, z, block, ExtraBlockTextures.BlockQRingEdgeLight.getIcon(), renderer, side );
				}
			}
		}

		renderer.renderAllFaces = false;
		return true;
	}
}
