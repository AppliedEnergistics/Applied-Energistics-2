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

package appeng.api.implementations.items;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.util.AEColor;


/**
 * Memory Card API
 *
 * AE's Memory Card Item Class implements this interface.
 */
public interface IMemoryCard
{

	/**
	 * Configures the data stored on the memory card, the SettingsName, will be
	 * localized when displayed.
	 *
	 * The data can contain an optional string with the key "tooltip", which will be used as
	 * unlocalized string to display it after the settings name.
	 *
	 * The data can contain an optional intArray using "colorCode" to be displayed on the model itself.
	 * It needs to have exactly 8 elements representing the ordinal of the matching {@link AEColor}.
	 * The first 4 values represent the top row, left to right. The second 4 the bottom row.
	 *
	 * @param is item
	 * @param settingsName unlocalized string that represents the tile entity.
	 * @param data the NBT tag, refer to the normal comment for special keys.
	 */
	void setMemoryCardContents( ItemStack is, String settingsName, NBTTagCompound data );

	/**
	 * returns the settings name provided by a previous call to
	 * setMemoryCardContents, or "AppEng.GuiITooltip.Blank" if there was no
	 * previous call to setMemoryCardContents.
	 *
	 * @param is item
	 *
	 * @return setting name
	 */
	String getSettingsName( ItemStack is );

	/**
	 * @param is item
	 *
	 * @return the NBT Data previously saved by setMemoryCardContents, or an
	 * empty NBTCompound
	 */
	NBTTagCompound getData( ItemStack is );

	/**
	 * This represent as 4x2 grid of {@link AEColor} without transparent/fluix color.
	 *
	 * First 4 colors are used for the top row, second for the bottom one.
	 *
	 * @param is item
	 *
	 * @return a hash representation of the memory card content
	 */
	AEColor[] getColorCode( ItemStack is );

	/**
	 * notify the user of a outcome related to the memory card.
	 *
	 * @param player that used the card.
	 * @param msg which message to send.
	 */
	void notifyUser( EntityPlayer player, MemoryCardMessages msg );
}
