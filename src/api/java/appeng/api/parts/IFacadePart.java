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


import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.client.BakingPipeline;
import appeng.api.util.AEPartLocation;


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
	 * @param e colliding entity
	 */
	void getBoxes( IPartCollisionHelper ch, Entity e );

	/**
	 * @return side the facade is in
	 */
	AEPartLocation getSide();

	/**
	 * @return the box for the face of the facade
	 */
	AxisAlignedBB getPrimaryBox();

	Item getItem();

	int getItemDamage();

	boolean notAEFacade();

	void setThinFacades( boolean useThinFacades );

	boolean isTransparent();

	@SideOnly( Side.CLIENT )
	public List<BakedQuad> getOrBakeQuads( IPartHost host, BakingPipeline<BakedQuad, BakedQuad> rotatingPipeline, IBlockState state, EnumFacing side, long rand );
}