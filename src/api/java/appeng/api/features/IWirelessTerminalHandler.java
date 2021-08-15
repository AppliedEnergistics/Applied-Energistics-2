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

import java.util.OptionalLong;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.api.util.IConfigManager;

/**
 * Handles the interaction between an open terminal menu and the item in the player's inventory that represents that
 * terminal's host.
 *
 * @see WirelessTerminals
 */
public interface IWirelessTerminalHandler {

    /**
     * Gets the key of the grid that the wireless terminal is linked to. This can be empty to signal that the terminal
     * screen should be closed or be otherwise unavailable. The grid will be looked up using
     * {@link Locatables#securityStations()}. To support setting the grid key using the standard security station slot,
     * register a {@link IGridLinkableHandler} for your item.
     */
    OptionalLong getGridKey(ItemStack is);

    /**
     * use an amount of power, in AE units
     *
     * @param amount is in AE units ( 5 per MJ ), if you return false, the item should be dead and return false for
     *               hasPower
     * @return true if wireless terminal uses power
     */
    boolean usePower(Player player, double amount, ItemStack is);

    /**
     * gets the power status of the item.
     *
     * @return returns true if there is any power left.
     */
    boolean hasPower(Player player, double amount, ItemStack is);

    /**
     * Return the config manager for the wireless terminal.
     *
     * @return config manager of wireless terminal
     */
    IConfigManager getConfigManager(ItemStack is);

}
