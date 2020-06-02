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

package appeng.client.render;


import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.Vector3f;
import net.minecraft.util.Direction;

import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;


/**
 * Helper methods for rendering TESRs.
 */
public class TesrRenderHelper
{

	private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;

	/**
	 * Move the current coordinate system to the center of the given block face, assuming that the origin is currently
	 * at the center of a block.
	 */
	public static void moveToFace( MatrixStack mStack, Direction face )
	{
		mStack.translate( face.getXOffset() * 0.50, face.getYOffset() * 0.50, face.getZOffset() * 0.50 );
	}

	/**
	 * Rotate the current coordinate system so it is on the face of the given block side. This can be used to render on
	 * the given face as if it was
	 * a 2D canvas.
	 */
	public static void rotateToFace( MatrixStack mStack, Direction face, byte spin )
	{
		switch( face )
		{
			case UP:
				mStack.scale( 1.0f, -1.0f, 1.0f );
				mStack.rotate( Vector3f.XP.rotationDegrees( 90.0F ) );
				mStack.rotate( Vector3f.ZP.rotationDegrees( spin * 90.0F ) );
				break;

			case DOWN:
				mStack.scale( 1.0f, -1.0f, 1.0f );
				mStack.rotate( Vector3f.XP.rotationDegrees( -90.0F ) );
				mStack.rotate( Vector3f.ZP.rotationDegrees( spin * -90.0F ) );
				break;

			case EAST:
				mStack.scale( -1.0f, -1.0f, -1.0f );
				mStack.rotate( Vector3f.YP.rotationDegrees( -90.0F ) );
				break;

			case WEST:
				mStack.scale( -1.0f, -1.0f, -1.0f );
				mStack.rotate( Vector3f.YP.rotationDegrees( 90.0F ) );
				break;

			case NORTH:
				mStack.scale( -1.0f, -1.0f, -1.0f );
				break;

			case SOUTH:
				mStack.scale( -1.0f, -1.0f, -1.0f );
				mStack.rotate( Vector3f.YP.rotationDegrees( 180.0F ) );
				break;

			default:
				break;
		}
	}

	//TODO, A different approach will have to be used for this from TESRs, -covers, i have ideas.
	//	/**
	//	 * Render an item in 2D.
	//	 */
	//	public static void renderItem2d( ItemStack itemStack, float scale )
	//	{
	//		if( !itemStack.isEmpty() )
	//		{
	//			RenderSystem.glMultiTexCoord2f( GL13.GL_TEXTURE22, 240.f, 240.0f );
	//
	//			RenderSystem.pushMatrix();
	//
	//			// The Z-scaling by 0.0001 causes the model to be visually "flattened"
	//			// This cannot replace a proper projection, but it's cheap and gives the desired
	//			// effect at least from head-on
	//			RenderSystem.scaled( scale / 32.0f, scale / 32.0f, 0.0001f );
	//			// Position the item icon at the top middle of the panel
	//			RenderSystem.translated( -8, -11, 0 );
	//
	//			ItemRenderer renderItem = Minecraft.getInstance().getItemRenderer();
	//			renderItem.renderItemAndEffectIntoGUI( itemStack, 0, 0 );
	//
	//			RenderSystem.popMatrix();
	//		}
	//	}
	//
	//	/**
	//	 * Render an item in 2D and the given text below it.
	//	 *
	//	 * @param spacing Specifies how far apart the item and the item stack amount are rendered.
	//	 */
	//	public static void renderItem2dWithAmount( IAEItemStack itemStack, float itemScale, float spacing )
	//	{
	//		final ItemStack renderStack = itemStack.asItemStackRepresentation();
	//
	//		TesrRenderHelper.renderItem2d( renderStack, itemScale );
	//
	//		final long stackSize = itemStack.getStackSize();
	//		final String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm( stackSize );
	//
	//		// Render the item count
	//		final FontRenderer fr = Minecraft.getInstance().fontRenderer;
	//		final int width = fr.getStringWidth( renderedStackSize );
	//		RenderSystem.translated( 0.0f, spacing, 0 );
	//		RenderSystem.scaled( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );
	//		RenderSystem.translated( -0.5f * width, 0.0f, 0.5f );
	//		fr.drawString( renderedStackSize, 0, 0, 0 );
	//
	//	}
}
