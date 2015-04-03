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


import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;
import appeng.api.util.AECableType;


/**
 * Implement to create a networked {@link TileEntity} or {@link IPart} must
 * be implemented for a part, or tile entity to become part of a grid.
 */
public interface IGridHost
{

	/**
	 * get the grid node for a particular side of a block, you can return null,
	 * by returning a valid node later and calling updateState, you can join the
	 * Grid when your block is ready.
	 *
	 * @param dir feel free to ignore this, most blocks will use the same node
	 *            for every side.
	 *
	 * @return a new IGridNode, create these with
	 * AEApi.INSTANCE().createGridNode( MyIGridBlock )
	 */
	IGridNode getGridNode( ForgeDirection dir );

	/**
	 * Determines how cables render when they connect to this block. Priority is
	 * Smart &gt; Covered &gt; Glass
	 *
	 * @param dir direction
	 */
	AECableType getCableConnectionType( ForgeDirection dir );

	/**
	 * break this host, its violating security rules, just break your block, or part.
	 */
	void securityBreak();
}
