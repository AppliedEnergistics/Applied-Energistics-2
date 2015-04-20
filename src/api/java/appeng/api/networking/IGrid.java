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


import appeng.api.networking.events.MENetworkEvent;
import appeng.api.util.IReadOnlyCollection;


/**
 * Gives you access to Grid based information.
 *
 * Don't Implement.
 */
public interface IGrid
{

	/**
	 * Get Access to various grid modules
	 *
	 * @param iface face
	 *
	 * @return the IGridCache you requested.
	 */
	<C extends IGridCache> C getCache( Class<? extends IGridCache> iface );

	/**
	 * Post an event into the network event bus.
	 *
	 * @param ev - event to post
	 *
	 * @return returns ev back to original poster
	 */
	MENetworkEvent postEvent( MENetworkEvent ev );

	/**
	 * Post an event into the network event bus, but direct it at a single node.
	 *
	 * @param ev event to post
	 *
	 * @return returns ev back to original poster
	 */
	MENetworkEvent postEventTo( IGridNode node, MENetworkEvent ev );

	/**
	 * get a list of the diversity of classes, you can use this to better detect which machines your interested in,
	 * rather then iterating the entire grid to test them.
	 *
	 * @return IReadOnlyCollection of all available host types (Of Type IGridHost).
	 */
	IReadOnlyCollection<Class<? extends IGridHost>> getMachinesClasses();

	/**
	 * Get machines on the network.
	 *
	 * @param gridHostClass class of the grid host
	 *
	 * @return IMachineSet of all nodes belonging to hosts of specified class.
	 */
	IMachineSet getMachines( Class<? extends IGridHost> gridHostClass );

	/**
	 * @return IReadOnlyCollection for all nodes on the network, node visitors are preferred.
	 */
	IReadOnlyCollection<IGridNode> getNodes();

	/**
	 * @return true if the last node has been removed from the grid.
	 */
	boolean isEmpty();

	/**
	 * @return the node considered the pivot point of the grid.
	 */
	IGridNode getPivot();
}