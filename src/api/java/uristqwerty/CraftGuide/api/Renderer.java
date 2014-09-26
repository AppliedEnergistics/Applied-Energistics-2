package uristqwerty.CraftGuide.api;

import net.minecraft.item.ItemStack;

/**
 * Provides useful methods to render common things, in
 * {@link Slot#draw} and {@link CraftGuideRecipe#draw}.
 */
public interface Renderer
{
	/**
	 * Draws an ItemStack at the specified screen coordinates.
	 * @param x
	 * @param y
	 * @param stack
	 */
	public void renderItemStack(int x, int y, ItemStack stack);


	/**
	 * Draws a solid color rectangle
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color_argb Red, Green, Blue, and Alpha, packed into a single int,
	 * in the form 0xaarrggbb
	 */
	public void renderRect(int x, int y, int width, int height, int color_argb);


	/**
	 * Draws a solid color rectangle
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param color_rgb Red, Green, and Blue, packed into a single int, in the
	 * form 0x00rrggbb
	 * @param alpha
	 */
	public void renderRect(int x, int y, int width, int height, int color_rgb, int alpha);

	/**
	 * Draws a solid color rectangle
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param red
	 * @param green
	 * @param blue
	 * @param alpha
	 */
	public void renderRect(int x, int y, int width, int height, int red, int green, int blue, int alpha);

	/**
	 * Draws a rectangle, textured with a NamedTexture obtained from
	 * {@link Util#getTexture}.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param texture
	 */
	public void renderRect(int x, int y, int width, int height, NamedTexture texture);

	/**
	 * Draws a rectangle, containing a vertical gradient
	 * <br>
	 * Colors are packed into ints, in the form 0xaarrggbb
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param topColor_argb
	 * @param bottomColor_argb
	 */
	public void renderVerticalGradient(int x, int y, int width, int height, int topColor_argb, int bottomColor_argb);

	/**
	 * Draws a rectangle, containing a horizontal gradient.
	 * <br>
	 * Colors are packed into ints, in the form 0xaarrggbb
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param leftColor_argb
	 * @param rightColor_argb
	 */
	public void renderHorizontalGradient(int x, int y, int width, int height, int leftColor_argb, int rightColor_argb);

	/**
	 * Draws a rectangle, containing an arbitrary gradient.
	 * <br>
	 * Colors are packed into ints, in the form 0xaarrggbb
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param topLeftColor_argb
	 * @param topRightColor_argb
	 * @param bottomLeftColor_argb
	 * @param bottomRightColor_argb
	 */
	public void renderGradient(int x, int y, int width, int height, int topLeftColor_argb, int topRightColor_argb, int bottomLeftColor_argb, int bottomRightColor_argb);
}
