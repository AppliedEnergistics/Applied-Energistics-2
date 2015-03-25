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


import net.minecraftforge.common.util.ForgeDirection;


/**
 * Access to AE's internal grid connections.
 *
 * Messing with connection is generally completely unnecessary, you should be able to just use IGridNode.updateState()
 * to have AE manage them for you.
 *
 * Don't Implement.
 */
public interface IGridConnection
{

	/**
	 * lets you get the opposing node of the connection by passing your own node.
	 *
	 * @param gridNode current grid node
	 *
	 * @return the IGridNode which represents the opposite side of the connection.
	 */
	IGridNode getOtherSide( IGridNode gridNode );

	/**
	 * determine the direction of the connection based on your node.
	 *
	 * @param gridNode current grid node
	 *
	 * @return the direction of the connection, only valid for in world connections.
	 */
	ForgeDirection getDirection( IGridNode gridNode );

	/**
	 * by destroying a connection you may create new grids, and trigger un-expected behavior, you should only destroy
	 * connections if you created them.
	 */
	void destroy();

	/**
	 * @return node A
	 */
	IGridNode a();

	/**
	 * @return node B
	 */
	IGridNode b();

	/**
	 * @return if the connection is invisible this returns false
	 */
	boolean hasDirection();

	/**
	 * @return how many channels pass over this connections.
	 */
	int getUsedChannels();
}