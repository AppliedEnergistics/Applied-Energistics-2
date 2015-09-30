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


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;

import org.lwjgl.opengl.GL11;

import appeng.block.grindstone.BlockCrank;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.ModelGenerator;
import appeng.tile.grindstone.TileCrank;


public class RenderBlockCrank extends BaseBlockRender<BlockCrank, TileCrank>
{

	public RenderBlockCrank()
	{
		super( true, 60 );
	}

	@Override
	public void renderInventory( final BlockCrank blk, final ItemStack is, final ModelGenerator renderer, final ItemRenderType type, final Object[] obj )
	{
		renderer.renderAllFaces = true;

		renderer.setRenderBounds( 0.5D - 0.05, 0.5D - 0.5, 0.5D - 0.05, 0.5D + 0.05, 0.5D + 0.3, 0.5D + 0.05 );
		super.renderInventory( blk, is, renderer, type, obj );

		renderer.setRenderBounds( 0.70D - 0.15, 0.75D - 0.05, 0.5D - 0.05, 0.70D + 0.28, 0.75D + 0.05, 0.5D + 0.05 );
		super.renderInventory( blk, is, renderer, type, obj );

		renderer.renderAllFaces = false;
	}

	@Override
	public boolean renderInWorld( final BlockCrank imb, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{
		return true;
	}

	@Override
	public void renderTile( final BlockCrank blk, final TileCrank tile, final WorldRenderer tess, final double x, final double y, final double z, final float f, final ModelGenerator renderBlocks )
	{
		if( tile.getUp() == null )
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

		GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
		this.applyTESRRotation( x, y, z, tile.getForward(), tile.getUp() );

		GL11.glTranslated( 0.5, 0, 0.5 );
		GL11.glRotatef( tile.visibleRotation, 0, 1, 0 );
		GL11.glScalef( -1, 1, 1 );
		GL11.glTranslated( -0.5, 0, -0.5 );

		//tess.setTranslation( -tc.getPos().getX(), -tc.getPos().getY(), -tc.getPos().getZ() );
		//tess.startDrawingQuads();
		
		final RenderItem ri = Minecraft.getMinecraft().getRenderItem();
		
		final ItemStack stack = new ItemStack( blk );
		final IBakedModel model = ri.getItemModelMesher().getItemModel( stack );
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor( model, 1.0F, 1.0F, 1.0F, 1.0F );
		
		/*
		renderBlocks.renderAllFaces = true;
		renderBlocks.blockAccess = tc.getWorld();

		renderBlocks.setRenderBounds( 0.5D - 0.05, 0.5D - 0.5, 0.5D - 0.05, 0.5D + 0.05, 0.5D + 0.1, 0.5D + 0.05 );

		renderBlocks.renderStandardBlock( blk,  tc.getPos());
		renderBlocks.setRenderBounds( 0.70D - 0.15, 0.55D - 0.05, 0.5D - 0.05, 0.70D + 0.15, 0.55D + 0.05, 0.5D + 0.05 );

		renderBlocks.renderStandardBlock( blk, tc.getPos()  );
		*/

		//Tessellator.getInstance().draw();
		//tess.setTranslation( 0, 0, 0 );
		RenderHelper.enableStandardItemLighting();
	}
}
