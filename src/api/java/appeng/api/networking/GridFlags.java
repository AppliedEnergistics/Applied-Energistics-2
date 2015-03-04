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


/**
 * Various flags to determine network node behavior.
 */
public enum GridFlags
{
	/**
	 * import/export buses, terminals, and other devices that use network features, will use this setting.
	 */
	REQUIRE_CHANNEL,

	/**
	 * P2P ME tunnels use this setting.
	 */
	COMPRESSED_CHANNEL,

	/**
	 * cannot carry channels over this node.
	 */
	CANNOT_CARRY,

	/**
	 * Used by P2P Tunnels to prevent tunnels from tunneling recursively.
	 */
	CANNOT_CARRY_COMPRESSED,

	/**
	 * This node can transmit 32 signals, this should only apply to Tier2 Cable, P2P Tunnels, and Quantum Network
	 * Bridges.
	 */
	DENSE_CAPACITY,

	/**
	 * This block is part of a multiblock, used in conjunction with REQUIRE_CHANNEL, and {@link IGridMultiblock} see this
	 * interface for details.
	 */
	MULTIBLOCK,

	/**
	 * Indicates which path might be preferred, this only matters if two routes of equal length exist, ad only changes
	 * the order they are processed in.
	 */
	PREFERRED
}
