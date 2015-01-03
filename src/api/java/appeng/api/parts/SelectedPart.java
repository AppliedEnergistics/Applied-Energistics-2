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

package appeng.api.parts;


import net.minecraftforge.common.util.ForgeDirection;


/**
 * Reports a selected part from th IPartHost
 */
public class SelectedPart
{

	/**
	 * selected part.
	 */
	public final IPart part;

	/**
	 * facade part.
	 */
	public final IFacadePart facade;

	/**
	 * side the part is mounted too, or {@link ForgeDirection}.UNKNOWN for cables.
	 */
	public final ForgeDirection side;

	public SelectedPart()
	{
		this.part = null;
		this.facade = null;
		this.side = ForgeDirection.UNKNOWN;
	}

	public SelectedPart( IPart part, ForgeDirection side )
	{
		this.part = part;
		this.facade = null;
		this.side = side;
	}

	public SelectedPart( IFacadePart facade, ForgeDirection side )
	{
		this.part = null;
		this.facade = facade;
		this.side = side;
	}
}
