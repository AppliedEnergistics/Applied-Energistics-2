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


import appeng.api.definitions.IDefinitions;
import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHelper;
import appeng.api.storage.IStorageHelper;
import appeng.api.util.IClientHelper;


@AEInjectable
public interface IAppEngApi
{
	/**
	 * @return Registry Container for the numerous registries in AE2.
	 */
	IRegistryContainer registries();

	/**
	 * @return A helper for working with storage data types.
	 */
	IStorageHelper storage();

	/**
	 * @return A helper to create {@link IGridNode} and other grid related objects.
	 */
	IGridHelper grid();

	/**
	 * @return A helper for working with grids, and buses.
	 */
	IPartHelper partHelper();

	/**
	 * @return An accessible list of all AE definitions
	 */
	IDefinitions definitions();

	/**
	 * @return Utility methods primarily useful for client side stuff
	 */
	IClientHelper client();

}