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


import appeng.block.grindstone.BlockCrank;
import appeng.client.render.BaseBlockRender;
import appeng.tile.grindstone.TileCrank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;


public class RenderBlockCrank extends BaseBlockRender<BlockCrank, TileCrank>
{

	public RenderBlockCrank()
	{
		super( true, 60 );
	}

	@Override
	public void renderInventory( final BlockCrank blk, final ItemStack is, final RenderBlocks renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.renderAllFaces = true;

		renderer.setRenderBounds( 0.5D - 0.05, 0.5D - 0.5, 0.5D - 0.05, 0.5D + 0.05, 0.5D + 0.3, 0.5D + 0.05 );
		super.renderInventory( blk, is, renderer, type, obj );

		renderer.setRenderBounds( 0.70D - 0.15, 0.75D - 0.05, 0.5D - 0.05, 0.70D + 0.28, 0.75D + 0.05, 0.5D + 0.05 );
		super.renderInventory( blk, is, renderer, type, obj );

		renderer.renderAllFaces = false;
	}

	@Override
	public boolean renderInWorld( final BlockCrank imb, final IBlockAccess world, final int x, final int y, final int z, final RenderBlocks renderer )
	{
		return true;
	}

	@Override
	public void renderTile( final BlockCrank blk, final TileCrank tile, final Tessellator tess, final double x, final double y, final double z, final float f, final RenderBlocks renderBlocks )
	{
		if( tile.getUp() == null || tile.getUp() == ForgeDirection.UNKNOWN )
		{
			return;
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture( TextureMap.locationBlocksTexture );
		RenderHelper.disableStandardItemLighting();

		if( Minecraft.isAmbientOcclusionEnabled() )
		{
			GL11.glShadeModel( GL11.GL_SMOOTH );
		}
		else
		{
			GL11.glShadeModel( GL11.GL_FLAT );
		}

		GL11.glCullFace( GL11.GL_FRONT );
		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );

		this.applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

		GL11.glTranslated( 0.5, 0, 0.5 );
		GL11.glRotatef( tile.getVisibleRotation(), 0, 1, 0 );
		GL11.glTranslated( -0.5, 0, -0.5 );

		tess.setTranslation( -tile.xCoord, -tile.yCoord, -tile.zCoord );
		tess.startDrawingQuads();
		renderBlocks.renderAllFaces = true;
		renderBlocks.blockAccess = tile.getWorldObj();

		renderBlocks.setRenderBounds( 0.5D - 0.05, 0.5D - 0.5, 0.5D - 0.05, 0.5D + 0.05, 0.5D + 0.1, 0.5D + 0.05 );

		renderBlocks.renderStandardBlock( blk, tile.xCoord, tile.yCoord, tile.zCoord );

		renderBlocks.setRenderBounds( 0.70D - 0.15, 0.55D - 0.05, 0.5D - 0.05, 0.70D + 0.15, 0.55D + 0.05, 0.5D + 0.05 );

		renderBlocks.renderStandardBlock( blk, tile.xCoord, tile.yCoord, tile.zCoord );

		tess.draw();
		tess.setTranslation( 0, 0, 0 );

		GL11.glCullFace( GL11.GL_BACK );

		RenderHelper.enableStandardItemLighting();
	}
}
