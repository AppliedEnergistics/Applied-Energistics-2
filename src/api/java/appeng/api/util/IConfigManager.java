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

package appeng.api.util;


import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.Settings;


/**
 * Used to adjust settings on an object,
 *
 * Obtained via {@link IConfigurableObject}
 */
public interface IConfigManager
{

	/**
	 * get a list of different settings
	 *
	 * @return enum set of settings
	 */
	Set<Settings> getSettings();

	/**
	 * used to initialize the configuration manager, should be called for all settings.
	 *
	 * @param settingName  name of setting
	 * @param defaultValue default value of setting
	 */
	void registerSetting( Settings settingName, Enum<?> defaultValue );

	/**
	 * Get Value of a particular setting
	 *
	 * @param settingName name of setting
	 *
	 * @return value of setting
	 */
	Enum<?> getSetting( Settings settingName );

	/**
	 * Change setting
	 *
	 * @param settingName to be changed setting
	 * @param newValue    new value for setting
	 *
	 * @return changed setting
	 */
	Enum<?> putSetting( Settings settingName, Enum<?> newValue );

	/**
	 * write all settings to the NBT Tag so they can be read later.
	 *
	 * @param destination to be written nbt tag
	 */
	void writeToNBT( NBTTagCompound destination );

	/**
	 * Only works after settings have been registered
	 *
	 * @param src to be read nbt tag
	 */
	void readFromNBT( NBTTagCompound src );
}
