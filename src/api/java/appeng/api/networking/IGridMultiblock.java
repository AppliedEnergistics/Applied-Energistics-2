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

package appeng.api.networking;


import java.util.Iterator;


/**
 * An extension of IGridBlock, only means something when your getFlags() contains REQUIRE_CHANNEL, when done properly it
 * will call the method to get a list of all related nodes and give each of them a channel simultaneously for the entire
 * set. This means your entire Multiblock can work with a single channel, instead of one channel per block.
 */
public interface IGridMultiblock extends IGridBlock
{

	/**
	 * Used to acquire a list of all nodes that are part of the multiblock.
	 *
	 * @return an iterator that will iterate all the nodes for the multiblock. ( read-only iterator expected. )
	 */
	Iterator<IGridNode> getMultiblockNodes();
}
