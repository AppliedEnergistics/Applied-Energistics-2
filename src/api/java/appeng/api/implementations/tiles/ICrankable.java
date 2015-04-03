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

package appeng.api.implementations.tiles;


import net.minecraftforge.common.util.ForgeDirection;


/**
 * Crank/Crankable API,
 *
 * Tiles that Implement this can receive power, from the crank, and have the
 * crank placed on them.
 *
 * Tiles that access other tiles that implement this method can act as Cranks.
 *
 * This interface must be implemented by a tile entity.
 */
public interface ICrankable
{

	/**
	 * Test if the crank can turn, return false if there is no work to be done.
	 *
	 * @return if crank should be allowed to turn.
	 */
	boolean canTurn();

	/**
	 * The crank has completed one turn.
	 */
	void applyTurn();

	/**
	 * @return true if the crank can attach on the given side.
	 */
	boolean canCrankAttach( ForgeDirection directionToCrank );
}
