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

import appeng.api.util.IConfigManager;


/**
 * A handler for a wireless terminal.
 */
public interface IWirelessTermHandler extends INetworkEncodable
{

	/**
	 * @param is wireless terminal
	 *
	 * @return true, if usePower, hasPower, etc... can be called for the provided item
	 */
	boolean canHandle( ItemStack is );

	/**
	 * use an amount of power, in AE units
	 *
	 * @param amount is in AE units ( 5 per MJ ), if you return false, the item should be dead and return false for
	 *               hasPower
	 * @param is     wireless terminal
	 *
	 * @return true if wireless terminal uses power
	 */
	boolean usePower( EntityPlayer player, double amount, ItemStack is );

	/**
	 * gets the power status of the item.
	 *
	 * @param is wireless terminal
	 *
	 * @return returns true if there is any power left.
	 */
	boolean hasPower( EntityPlayer player, double amount, ItemStack is );

	/**
	 * Return the config manager for the wireless terminal.
	 *
	 * @param is wireless terminal
	 *
	 * @return config manager of wireless terminal
	 */
	IConfigManager getConfigManager( ItemStack is );
}