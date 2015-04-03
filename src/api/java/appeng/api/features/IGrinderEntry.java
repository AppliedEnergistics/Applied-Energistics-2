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

package appeng.api.features;


import net.minecraft.item.ItemStack;


/**
 * Registration Records for {@link IGrinderRegistry}
 */
public interface IGrinderEntry
{

	/**
	 * the current input
	 *
	 * @return input that the grinder will accept.
	 */
	ItemStack getInput();

	/**
	 * lets you change the grinder recipe by changing its input.
	 *
	 * @param input input item
	 */
	void setInput( ItemStack input );

	/**
	 * gets the current output
	 *
	 * @return output that the grinder will produce
	 */
	ItemStack getOutput();

	/**
	 * allows you to change the output.
	 *
	 * @param output output item
	 */
	void setOutput( ItemStack output );

	/**
	 * gets the current output
	 *
	 * @return output that the grinder will produce
	 */
	ItemStack getOptionalOutput();

	/**
	 * gets the current output
	 *
	 * @return output that the grinder will produce
	 */
	ItemStack getSecondOptionalOutput();

	/**
	 * stack, and 0.0-1.0 chance that it will be generated.
	 *
	 * @param output output item
	 * @param chance generation chance
	 */
	void setOptionalOutput( ItemStack output, float chance );

	/**
	 * 0.0 - 1.0 the chance that the optional output will be generated.
	 *
	 * @return chance of optional output
	 */
	float getOptionalChance();

	/**
	 * stack, and 0.0-1.0 chance that it will be generated.
	 *
	 * @param output second optional output item
	 * @param chance second optional output chance
	 */
	void setSecondOptionalOutput( ItemStack output, float chance );

	/**
	 * 0.0 - 1.0 the chance that the optional output will be generated.
	 *
	 * @return second optional output chance
	 */
	float getSecondOptionalChance();

	/**
	 * Energy cost, in turns.
	 *
	 * @return number of turns it takes to produce the output from the input.
	 */
	int getEnergyCost();

	/**
	 * Allows you to adjust the number of turns
	 *
	 * @param c number of turns to produce output.
	 */
	void setEnergyCost( int c );
}
