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


import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

//@formatter:off


/**
 * This is a pretty basic requirement, once you implement the interface, and createPartFromItemStack
 *
 * you must register your bus with the Bus renderer, using AEApi.INSTANCE().partHelper().setItemBusRenderer( this );
 *
 * then simply add this, and call AE's Bus Placement Code.
 *
 * <pre>
 * <code>
 *
 * {@literal @}Override
 * public default EnumActionResult onItemUse(ItemStack is, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
 *	{
 *		return AEApi.instance().partHelper().placeBus( is, pos, side, player, hand, world );
 *	}
 * </code>
 * </pre>
 */
public interface IPartItem<P extends IPart>
{

	/**
	 * create a new part INSTANCE, from the item stack.
	 *
	 * @param is item
	 *
	 * @return part from item
	 */
	@Nullable
	P createPartFromItemStack( ItemStack is );

}