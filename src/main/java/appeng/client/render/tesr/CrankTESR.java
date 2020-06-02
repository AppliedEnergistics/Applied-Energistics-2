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

package appeng.client.render.tesr;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.FacingToRotation;
import appeng.tile.grindstone.TileCrank;


/**
 * This FastTESR only handles the animated model of the turning crank. When the crank is at rest, it is rendered using a
 * normal model.
 */
@OnlyIn( Dist.CLIENT )
public class CrankTESR extends TileEntityRenderer<TileCrank>
{

	public CrankTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TileCrank te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers, int combinedLightIn, int combinedOverlayIn) {


		// Apply GL transformations relative to the center of the block: 1) TE rotation and 2) crank rotation
		ms.push();
		ms.translate( 0.5, 0.5, 0.5 );
		FacingToRotation.get( te.getForward(), te.getUp() ).push(ms);
		ms.rotate( new Quaternion(0, te.getVisibleRotation(), 0, true) );
		ms.translate( -0.5, -0.5, -0.5 );

		BlockState blockState = te.getWorld().getBlockState( te.getPos() ); // FIXME: i think world might be null here
		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		IBakedModel model = dispatcher.getModelForState( blockState );
		IVertexBuilder buffer = buffers.getBuffer(Atlases.getTranslucentBlockType());
		dispatcher.getBlockModelRenderer().renderModelBrightnessColor(ms.getLast(), buffer, null, model, 1, 1, 1, combinedLightIn, combinedOverlayIn);
		ms.pop();

//		// Most of this is blatantly copied from FastTESR
//		Tessellator tessellator = Tessellator.getInstance();
//		this.bindTexture( AtlasTexture.LOCATION_BLOCKS_TEXTURE );
//		RenderHelper.disableStandardItemLighting();
//		RenderSystem.blendFunc( GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA );
//		RenderSystem.enableBlend();
//		RenderSystem.disableCull();
//
//		if( Minecraft.isAmbientOcclusionEnabled() )
//		{
//			GlStateManager.shadeModel( GL11.GL_SMOOTH );
//		}
//		else
//		{
//			GlStateManager.shadeModel( GL11.GL_FLAT );
//		}
//
//		BlockState blockState = te.getWorld().getBlockState( te.getPos() );
//
//		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//		IBakedModel model = dispatcher.getModelForState( blockState );
//
//		BufferBuilder buffer = tessellator.getBuffer();
//		buffer.begin( GL11.GL_QUADS, DefaultVertexFormats.BLOCK );
//
//		// The translation ensures the vertex buffer positions are relative to 0,0,0 instead of the block pos
//		// This makes the translations that follow much easier
//		buffer.setTranslation( -te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ() );
//		dispatcher.getBlockModelRenderer().renderModel( te.getWorld(), model, blockState, te.getPos(), buffer, false );
//		buffer.setTranslation( 0, 0, 0 );
//
//		matrixStackIn.push();
//		matrixStackIn.translate( x, y, z );
//
//		// Apply GL transformations relative to the center of the block: 1) TE rotation and 2) crank rotation
//		matrixStackIn.translate( 0.5, 0.5, 0.5 );
//		FacingToRotation.get( te.getForward(), te.getUp() ).push(matrixStack);
//		matrixStackIn.rotate( te.getVisibleRotation(), 0, 1, 0 );
//		matrixStackIn.translate( -0.5, -0.5, -0.5 );
//
//		tessellator.draw();
//
//		matrixStackIn.pop();
//
//		RenderHelper.enableStandardItemLighting();
	}

}
