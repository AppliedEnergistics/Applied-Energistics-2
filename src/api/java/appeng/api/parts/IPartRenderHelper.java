package appeng.api.parts;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;

public interface IPartRenderHelper
{

	/**
	 * sets the Render Helpers Block Bounds. 0.0 - 16.0 block coords.
	 * 
	 * No complaints about the size, I like using pixels :P
	 * 
	 * @param minX minimal x bound
	 * @param minY minimal y bound
	 * @param minZ minimal z bound
	 * @param maxX maximal x bound
	 * @param maxY maximal y bound
	 * @param maxZ maximal z bound
	 */
	void setBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

	/**
	 * static renderer
	 * 
	 * render a single face.
	 * 
	 * @param x x coord of part
	 * @param y y coord of part
	 * @param z z coord of part
	 * @param ico icon of part
	 * @param face direction its facing
	 * @param renderer renderer of part
	 */
	@SideOnly(Side.CLIENT)
	void renderFace(int x, int y, int z, IIcon ico, ForgeDirection face, RenderBlocks renderer);

	/**
	 * static renderer
	 * 
	 * render a box with a cut out box in the center.
	 * 
	 * @param x x pos of part
	 * @param y y pos of part
	 * @param z z pos of part
	 * @param ico icon of part
	 * @param face face of part
	 * @param edgeThickness thickness of the edge
	 * @param renderer renderer
	 */
	@SideOnly(Side.CLIENT)
	void renderFaceCutout(int x, int y, int z, IIcon ico, ForgeDirection face, float edgeThickness, RenderBlocks renderer);

	/**
	 * static renderer
	 * 
	 * render a block of specified bounds.
	 * 
	 * @param x x pos of block
	 * @param y y pos of block
	 * @param z z pos of block
	 * @param renderer renderer
	 */
	@SideOnly(Side.CLIENT)
	void renderBlock(int x, int y, int z, RenderBlocks renderer);

	/**
	 * render a single face in inventory renderer.
	 * 
	 * @param IIcon icon of part
	 * @param direction face of part
	 * @param renderer renderer
	 */
	@SideOnly(Side.CLIENT)
	void renderInventoryFace(IIcon IIcon, ForgeDirection direction, RenderBlocks renderer);

	/**
	 * render a box in inventory renderer.
	 * 
	 * @param renderer renderer
	 */
	@SideOnly(Side.CLIENT)
	void renderInventoryBox(RenderBlocks renderer);

	/**
	 * inventory, and static renderer.
	 * 
	 * set unique icons for each side of the block.
	 * 
	 * @param down down face
	 * @param up up face
	 * @param north north face
	 * @param south south face
	 * @param west west face
	 * @param east east face
	 */
	void setTexture(IIcon down, IIcon up, IIcon north, IIcon south, IIcon west, IIcon east);

	/**
	 * inventory, and static renderer.
	 * 
	 * set all sides to a single IIcon.
	 * 
	 * @param ico to be set icon
	 */
	void setTexture(IIcon ico);

	/**
	 * configure the color multiplier for the inventory renderer.
	 * 
	 * @param whiteVariant color multiplier
	 */
	void setInvColor(int whiteVariant);

	/**
	 * @return the block used for rendering, might need it for some reason...
	 */
	Block getBlock();

	/**
	 * @return the east vector in world directions, rather then renderer
	 */
	ForgeDirection getWorldX();

	/**
	 * @return the up vector in world directions, rather then renderer.
	 */
	ForgeDirection getWorldY();

	/**
	 * @return the forward vector in world directions, rather then renderer.
	 */
	ForgeDirection getWorldZ();

	/**
	 * Pre-Calculates default lighting for the part, call this before using the render helper to render anything else to
	 * get simplified, but faster lighting for more then one block.
	 * 
	 * Only worth it if you render more then 1 block.
	 */
	ISimplifiedBundle useSimplifiedRendering(int x, int y, int z, IBoxProvider p, ISimplifiedBundle sim);

	/**
	 * disables, useSimplifiedRendering.
	 */
	void normalRendering();

	/**
	 * render a block using the current renderer state.
	 * 
	 * @param x x pos of part
	 * @param y y pos of part
	 * @param z z pos of part
	 * @param renderer renderer of part
	 */
	void renderBlockCurrentBounds(int x, int y, int z, RenderBlocks renderer);

	/**
	 * allow you to enable your part to render during the alpha pass or the standard pass.
	 * 
	 * @param pass render pass
	 */
	void renderForPass(int pass);

	/**
	 * Set which faces to render, remember to set back to ALL when you are done.
	 * 
	 * @param complementOf sides to render
	 */
	void setFacesToRender(EnumSet<ForgeDirection> complementOf);

}