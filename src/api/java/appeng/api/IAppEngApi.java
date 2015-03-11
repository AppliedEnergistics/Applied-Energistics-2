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

package appeng.api;


import appeng.api.definitions.Blocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.exceptions.FailedConnection;
import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHelper;
import appeng.api.storage.IStorageHelper;


public interface IAppEngApi
{

	/**
	 * @return Registry Container for the numerous registries in AE2.
	 */
	IRegistryContainer registries();

	/**
	 * @return helper for working with storage data types.
	 */
	IStorageHelper storage();

	/**
	 * @return helper for working with grids, and buses.
	 */
	IPartHelper partHelper();

	/**
	 * @return an accessible list of all of AE's Items
	 *
	 * @deprecated use {@link appeng.api.definitions.IDefinitions#items()}
	 */
	@Deprecated
	Items items();

	/**
	 * @return an accessible list of all of AE's materials; materials are items
	 *
	 * @deprecated use {@link appeng.api.definitions.IDefinitions#materials()}
	 */
	@Deprecated
	Materials materials();

	/**
	 * @return an accessible list of all of AE's blocks
	 *
	 * @deprecated use {@link appeng.api.definitions.IDefinitions#blocks()}
	 */
	@Deprecated
	Blocks blocks();

	/**
	 * @return an accessible list of all of AE's parts, parts are items
	 *
	 * @deprecated use {@link appeng.api.definitions.IDefinitions#parts()}
	 */
	@Deprecated
	Parts parts();

	/**
	 * @return an accessible list of all AE definitions
	 */
	IDefinitions definitions();

	/**
	 * create a grid node for your {@link appeng.api.networking.IGridHost}
	 *
	 * @param block grid block
	 *
	 * @return grid node of block
	 */
	IGridNode createGridNode( IGridBlock block );

	/**
	 * create a connection between two {@link appeng.api.networking.IGridNode}
	 *
	 * @param a to be connected gridnode
	 * @param b to be connected gridnode
	 *
	 * @throws appeng.api.exceptions.FailedConnection
	 */
	IGridConnection createGridConnection( IGridNode a, IGridNode b ) throws FailedConnection;
}