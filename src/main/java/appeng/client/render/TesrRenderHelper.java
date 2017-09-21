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


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.api.storage.data.IAEItemStack;
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
	public static void moveToFace( EnumFacing face )
	{
		GlStateManager.translate( face.getFrontOffsetX() * 0.50, face.getFrontOffsetY() * 0.50, face.getFrontOffsetZ() * 0.50 );
	}

	/**
	 * Rotate the current coordinate system so it is on the face of the given block side. This can be used to render on
	 * the given face as if it was
	 * a 2D canvas.
	 */
	public static void rotateToFace( EnumFacing face, byte spin )
	{
		switch( face )
		{
			case UP:
				GlStateManager.scale( 1.0f, -1.0f, 1.0f );
				GlStateManager.rotate( 90.0f, 1.0f, 0.0f, 0.0f );
				GlStateManager.rotate( spin * 90.0F, 0, 0, 1 );
				break;

			case DOWN:
				GlStateManager.scale( 1.0f, -1.0f, 1.0f );
				GlStateManager.rotate( -90.0f, 1.0f, 0.0f, 0.0f );
				GlStateManager.rotate( spin * -90.0F, 0, 0, 1 );
				break;

			case EAST:
				GlStateManager.scale( -1.0f, -1.0f, -1.0f );
				GlStateManager.rotate( -90.0f, 0.0f, 1.0f, 0.0f );
				break;

			case WEST:
				GlStateManager.scale( -1.0f, -1.0f, -1.0f );
				GlStateManager.rotate( 90.0f, 0.0f, 1.0f, 0.0f );
				break;

			case NORTH:
				GlStateManager.scale( -1.0f, -1.0f, -1.0f );
				break;

			case SOUTH:
				GlStateManager.scale( -1.0f, -1.0f, -1.0f );
				GlStateManager.rotate( 180.0f, 0.0f, 1.0f, 0.0f );
				break;

			default:
				break;
		}
	}

	/**
	 * Render an item in 2D.
	 */
	public static void renderItem2d( ItemStack itemStack, float scale )
	{
		if( !itemStack.isEmpty() )
		{
			OpenGlHelper.setLightmapTextureCoords( OpenGlHelper.lightmapTexUnit, 240.f, 240.0f );

			GlStateManager.pushMatrix();

			// The Z-scaling by 0.0001 causes the model to be visually "flattened"
			// This cannot replace a proper projection, but it's cheap and gives the desired
			// effect at least from head-on
			GlStateManager.scale( scale / 32.0f, scale / 32.0f, 0.0001f );
			// Position the item icon at the top middle of the panel
			GlStateManager.translate( -8, -11, 0 );

			RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
			renderItem.renderItemAndEffectIntoGUI( itemStack, 0, 0 );

			GlStateManager.popMatrix();
		}
	}

	/**
	 * Render an item in 2D and the given text below it.
	 *
	 * @param spacing Specifies how far apart the item and the item stack amount are rendered.
	 */
	public static void renderItem2dWithAmount( IAEItemStack itemStack, float itemScale, float spacing )
	{
		final ItemStack renderStack = itemStack.asItemStackRepresentation();

		TesrRenderHelper.renderItem2d( renderStack, itemScale );

		final long stackSize = itemStack.getStackSize();
		final String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm( stackSize );

		// Render the item count
		final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		final int width = fr.getStringWidth( renderedStackSize );
		GlStateManager.translate( 0.0f, spacing, 0 );
		GlStateManager.scale( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );
		GlStateManager.translate( -0.5f * width, 0.0f, 0.5f );
		fr.drawString( renderedStackSize, 0, 0, 0 );

	}

}
