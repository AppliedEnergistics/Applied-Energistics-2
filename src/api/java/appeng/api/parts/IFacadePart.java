/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 - 2015 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.parts;


import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


/**
 * Used Internally.
 *
 * not intended for implementation.
 */
public interface IFacadePart
{

	/**
	 * used to save the part.
	 */
	ItemStack getItemStack();

	/**
	 * used to collide, and pick the part
	 *
	 * @param ch collision helper
	 * @param e  colliding entity
	 */
	void getBoxes( IPartCollisionHelper ch, Entity e );

	/**
	 * render the part.
	 *
	 * @param x           x pos of part
	 * @param y           y pos of part
	 * @param z           z pos of part
	 * @param instance    render helper
	 * @param renderer    renderer
	 * @param fc          face container
	 * @param busBounds   bounding box
	 * @param renderStilt if to render stilt
	 */
	@SideOnly( Side.CLIENT )
	void renderStatic( int x, int y, int z, IPartRenderHelper instance, RenderBlocks renderer, IFacadeContainer fc, AxisAlignedBB busBounds, boolean renderStilt );

	/**
	 * render the part in inventory.
	 *
	 * @param instance render helper
	 * @param renderer renderer
	 */
	@SideOnly( Side.CLIENT )
	void renderInventory( IPartRenderHelper instance, RenderBlocks renderer );

	/**
	 * @return side the facade is in
	 */
	ForgeDirection getSide();

	/**
	 * @return the box for the face of the facade
	 */
	AxisAlignedBB getPrimaryBox();

	Item getItem();

	int getItemDamage();

	boolean notAEFacade();

	void setThinFacades( boolean useThinFacades );

	boolean isTransparent();
}