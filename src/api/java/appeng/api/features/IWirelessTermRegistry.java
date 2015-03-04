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

package appeng.api.features;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


/**
 * Registration record for a Custom Cell handler.
 */
public interface IWirelessTermRegistry
{

	/**
	 * add this handler to the list of other wireless handler.
	 *
	 * @param handler wireless handler
	 */
	void registerWirelessHandler( IWirelessTermHandler handler );

	/**
	 * @param is item which might have a handler
	 *
	 * @return true if there is a handler for this item
	 */
	boolean isWirelessTerminal( ItemStack is );

	/**
	 * @param is item with handler
	 *
	 * @return a register handler for the item in question, or null if there
	 * isn't one
	 */
	IWirelessTermHandler getWirelessTerminalHandler( ItemStack is );

	/**
	 * opens the wireless terminal gui, the wireless terminal item, must be in
	 * the active slot on the tool bar.
	 */
	void openWirelessTerminalGui( ItemStack item, World w, EntityPlayer player );
}
