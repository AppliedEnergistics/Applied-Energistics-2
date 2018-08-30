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

package appeng.api.recipes;


import net.minecraft.item.ItemStack;

import appeng.api.exceptions.MissingIngredientException;
import appeng.api.exceptions.RegistrationException;


@Deprecated
public interface IIngredient
{

	/**
	 * Acquire a single input stack for the current recipe, if more then one ItemStack is possible a
	 * RegistrationError exception will be thrown, ignore these and let the system handle the error.
	 *
	 * @return a single ItemStack for the recipe handler.
	 *
	 * @throws RegistrationException
	 * @throws MissingIngredientException
	 */
	ItemStack getItemStack() throws RegistrationException, MissingIngredientException;

	/**
	 * Acquire a list of all the input stacks for the current recipe, this is for handlers that support
	 * multiple inputs per slot.
	 *
	 * @return an array of ItemStacks for the recipe handler.
	 *
	 * @throws RegistrationException
	 * @throws MissingIngredientException
	 */
	ItemStack[] getItemStackSet() throws RegistrationException, MissingIngredientException;

	/**
	 * If you wish to support air, you must test before getting the ItemStack, or ItemStackSet
	 *
	 * @return true if this slot contains no ItemStack, this is passed as "_"
	 */
	boolean isAir();

	/**
	 * @return The Name Space of the item. Prefer getItemStack or getItemStackSet
	 */
	String getNameSpace();

	/**
	 * @return The Name of the item. Prefer getItemStack or getItemStackSet
	 */
	String getItemName();

	/**
	 * @return The Damage Value of the item. Prefer getItemStack or getItemStackSet
	 */
	int getDamageValue();

	/**
	 * @return The Damage Value of the item. Prefer getItemStack or getItemStackSet
	 */
	int getQty();

	/**
	 * Bakes the lists in for faster runtime look-ups.
	 *
	 * @throws MissingIngredientException
	 * @throws RegistrationException
	 */
	void bake() throws RegistrationException, MissingIngredientException;
}
