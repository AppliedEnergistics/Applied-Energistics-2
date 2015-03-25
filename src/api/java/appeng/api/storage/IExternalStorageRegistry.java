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

package appeng.api.storage;


import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.IAppEngApi;
import appeng.api.networking.security.BaseActionSource;


/**
 * A Registry of External Storage handlers.
 *
 * Do not implement obtain via {@link IAppEngApi}.registries().getExternalStorageRegistry()
 */
public interface IExternalStorageRegistry
{

	/**
	 * A registry for StorageBus interactions
	 *
	 * @param esh storage handler
	 */
	void addExternalStorageInterface( IExternalStorageHandler esh );

	/**
	 * @param te       tile entity
	 * @param opposite direction
	 * @param channel  channel
	 * @param mySrc    source
	 *
	 * @return the handler for a given tile / forge direction
	 */
	IExternalStorageHandler getHandler( TileEntity te, ForgeDirection opposite, StorageChannel channel, BaseActionSource mySrc );
}