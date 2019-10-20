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


import java.util.EnumSet;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;


/**
 * An Implementation is required to create your node for IGridHost
 *
 * Implement for use with IGridHost
 */
public interface IGridBlock
{

	/**
	 * how much power to drain per tick as part of idle network usage.
	 *
	 * if the value of this changes, you must post a MENetworkPowerIdleChange
	 *
	 * @return ae/t to use.
	 */
	@Nonnegative
	double getIdlePowerUsage();

	/**
	 * Various flags that AE uses to modify basic behavior for various parts of the network.
	 *
	 * @return Set of flags for this IGridBlock
	 */
	@Nonnull
	EnumSet<GridFlags> getFlags();

	/**
	 * Generally speaking you will return true for this, the one exception is buses, or worm holes where the node
	 * represents something that isn't a real connection in the world, but rather one represented internally to the
	 * block.
	 *
	 * @return if the world can connect to this node, and the node can connect to the world.
	 */
	boolean isWorldAccessible();

	/**
	 * Must not return when {@link #isWorldAccessible()} is true.
	 * Otherwise the behavior is unspecified.
	 *
	 * @return current location of this node
	 */
	@Nonnull
	DimensionalCoord getLocation();

	/**
	 * @return Transparent, or a valid color, NULL IS NOT A VALID RETURN
	 */
	@Nonnull
	AEColor getGridColor();

	/**
	 * Notifies your IGridBlock that changes were made to your connections
	 */
	void onGridNotification( @Nonnull GridNotification notification );

	/**
	 * Update Blocks network/connection/booting status. grid,
	 *
	 * @param grid grid
	 * @param channelsInUse used channels
	 * @deprecated to be removed in rv7
	 */
	@Deprecated
	void setNetworkStatus( IGrid grid, int channelsInUse );

	/**
	 * Determine which sides of the block can be connected too, only used when isWorldAccessible returns true, not used
	 * for {@link IPart} implementations.
	 */
	@Nonnull
	EnumSet<EnumFacing> getConnectableSides();

	/**
	 * @return the IGridHost for the node, this will be an IGridPart or a TileEntity generally speaking.
	 */
	@Nonnull
	IGridHost getMachine();

	/**
	 * called when the grid for the node has changed, the general grid state should not be trusted at this point.
	 */
	void gridChanged();

	/**
	 * Determines what item stack is used to render this node in the GUI.
	 *
	 * @return the render item stack to use to render this node. Returning an empty ItemStack will not show this node.
	 */
	@Nonnull
	ItemStack getMachineRepresentation();
}
