package appeng.client.render;


import org.lwjgl.opengl.GL11;

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
	 * Move the current coordinate system to the center of the given block face, assuming that the origin is currently at the center of a block.
	 */
	public static void moveToFace( EnumFacing face )
	{
		GL11.glTranslated( face.getFrontOffsetX() * 0.50, face.getFrontOffsetY() * 0.50, face.getFrontOffsetZ() * 0.50 );
	}

	/**
	 * Rotate the current coordinate system so it is on the face of the given block side. This can be used to render on the given face as if it was
	 * a 2D canvas.
	 */
	public static void rotateToFace( EnumFacing face, byte spin )
	{
		switch( face )
		{
			case UP:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
				GL11.glRotatef( spin * 90.0F, 0, 0, 1 );
				break;

			case DOWN:
				GL11.glScalef( 1.0f, -1.0f, 1.0f );
				GL11.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
				GL11.glRotatef( spin * -90.0F, 0, 0, 1 );
				break;

			case EAST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
				break;

			case WEST:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 90.0f, 0.0f, 1.0f, 0.0f );
				break;

			case NORTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				break;

			case SOUTH:
				GL11.glScalef( -1.0f, -1.0f, -1.0f );
				GL11.glRotatef( 180.0f, 0.0f, 1.0f, 0.0f );
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
		if( itemStack != null )
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
	 * @param spacing Specifies how far apart the item and the item stack amount are rendered.
	 */
	public static void renderItem2dWithAmount( IAEItemStack itemStack, float itemScale, float spacing ) {

		TesrRenderHelper.renderItem2d( itemStack.getItemStack(), itemScale );

		final long stackSize = itemStack.getStackSize();
		final String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm( stackSize );

		// Render the item count
		final FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		final int width = fr.getStringWidth( renderedStackSize );
		GL11.glTranslatef( 0.0f, spacing, 0 );
		GL11.glScalef( 1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f );
		GL11.glTranslatef( -0.5f * width, 0.0f, 0.5f );
		fr.drawString( renderedStackSize, 0, 0, 0 );

	}

}
