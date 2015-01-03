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

package appeng.api.implementations.parts;


import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGridHost;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;


/**
 * Implemented on the {@link IPart}s cable objects that can be placed at {@link ForgeDirection}.UNKNOWN in
 * {@link IPartHost}s
 */
public interface IPartCable extends IPart, IGridHost
{

	/**
	 * does this cable support buses?
	 */
	BusSupport supportsBuses();

	/**
	 * @return the current color of the cable.
	 */
	AEColor getCableColor();

	/**
	 * @return the Cable type.
	 */
	AECableType getCableConnectionType();

	/**
	 * Change the color of the cable, this should cost a small amount of dye, or something.
	 *
	 * @param newColor new color
	 *
	 * @return if the color change was successful.
	 */
	boolean changeColor( AEColor newColor, EntityPlayer who );

	/**
	 * Change sides on the cables node.
	 *
	 * Called by AE, do not invoke.
	 *
	 * @param sides sides of cable
	 */
	void setValidSides( EnumSet<ForgeDirection> sides );

	/**
	 * used to tests if a cable connects to neighbors visually.
	 *
	 * @param side neighbor side
	 *
	 * @return true if this side is currently connects to an external block.
	 */
	boolean isConnected( ForgeDirection side );
}
