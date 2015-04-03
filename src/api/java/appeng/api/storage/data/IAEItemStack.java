/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
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

package appeng.api.storage.data;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


/**
 * An alternate version of ItemStack for AE to keep tabs on things easier, and to support larger storage. stackSizes of
 * getItemStack will be capped.
 *
 * You may hold on to these if you want, just make sure you let go of them when your not using them.
 *
 * Don't Implement.
 *
 * Construct with Util.createItemStack( ItemStack )
 */
public interface IAEItemStack extends IAEStack<IAEItemStack>
{

	/**
	 * creates a standard MC ItemStack for the item.
	 *
	 * @return new ItemStack
	 */
	ItemStack getItemStack();

	/**
	 * is there NBT Data for this item?
	 *
	 * @return if there is
	 */
	boolean hasTagCompound();

	/**
	 * Combines two IAEItemStacks via addition.
	 *
	 * @param option to add to the current one.
	 */
	@Override
	void add( IAEItemStack option );

	/**
	 * create a AE Item clone
	 *
	 * @return the copy
	 */
	@Override
	IAEItemStack copy();

	/**
	 * quick way to get access to the MC Item Definition.
	 *
	 * @return item definition
	 */
	Item getItem();

	/**
	 * @return the items damage value
	 */
	int getItemDamage();

	/**
	 * Compare the Ore Dictionary ID for this to another item.
	 */
	boolean sameOre( IAEItemStack is );

	/**
	 * compare the item/damage/nbt of the stack.
	 *
	 * @param otherStack to be compared item
	 *
	 * @return true if it is the same type (same item, damage, nbt)
	 */
	boolean isSameType( IAEItemStack otherStack );

	/**
	 * compare the item/damage/nbt of the stack.
	 *
	 * @param stored to be compared item
	 *
	 * @return true if it is the same type (same item, damage, nbt)
	 */
	boolean isSameType( ItemStack stored );
}