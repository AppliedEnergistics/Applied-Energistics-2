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


import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;


/**
 * All Layers must extends this, this get part implementation is provided to interface with the parts, however a real
 * implementation will be used at runtime.
 */
public abstract class LayerBase extends TileEntity // implements IPartHost
{

	/**
	 * Grants access for the layer to the parts of the host.
	 *
	 * This Method looks silly, that is because its not used at runtime, a real implementation will be used instead.
	 *
	 * @param side side of part
	 *
	 * @return the part for the requested side.
	 */
	public IPart getPart( ForgeDirection side )
	{
		return null; // place holder.
	}

	/**
	 * called when the parts change in the container, YOU MUST CALL super.PartChanged();
	 */
	public void notifyNeighbors()
	{
	}

	/**
	 * called when the parts change in the container, YOU MUST CALL super.PartChanged();
	 */
	public void partChanged()
	{
	}

	/**
	 * @return a mutable list of flags you can adjust to track state.
	 */
	public Set<LayerFlags> getLayerFlags()
	{
		return null; // place holder.
	}

	public void markForSave()
	{
		// something!
	}
}
