/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 AlgorithmX2
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

package appeng.api.networking;


import javax.annotation.Nonnull;

import appeng.api.exceptions.FailedConnectionException;


/**
 * A helper responsible for creating new {@link IGridNode}, {@link IGridConnection} or potentially similar tasks.
 *
 * @author yueh
 * @version rv5
 * @since rv5
 */
public interface IGridHelper
{

	/**
	 * Create a grid node for your {@link IGridHost}
	 *
	 * The passed {@link IGridBlock} represents the definition for properties like connectable sides.
	 * Refer to its documentation for further details.
	 *
	 * @param block grid block
	 *
	 * @return grid node of block
	 */
	@Nonnull
	IGridNode createGridNode( @Nonnull IGridBlock block );

	/**
	 * Create a direct connection between two {@link IGridNode}.
	 *
	 * This will be considered as having a distance of 1, regardless of the location of both nodes.
	 *
	 * @param a to be connected gridnode
	 * @param b to be connected gridnode
	 *
	 * @throws appeng.api.exceptions.FailedConnectionException
	 */
	@Nonnull
	IGridConnection createGridConnection( @Nonnull IGridNode a, @Nonnull IGridNode b ) throws FailedConnectionException;

}