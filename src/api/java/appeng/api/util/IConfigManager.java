package appeng.api.util;

import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;

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
	Set<Enum> getSettings();

	/**
	 * used to initialize the configuration manager, should be called for all settings.
	 * 
	 * @param settingName name of setting
	 * @param defaultValue default value of setting
	 */
	void registerSetting(Enum settingName, Enum defaultValue);

	/**
	 * Get Value of a particular setting
	 * 
	 * @param settingName name of setting
	 * @return value of setting
	 */
	Enum getSetting(Enum settingName);

	/**
	 * Change setting
	 * 
	 * @param settingName to be changed setting
	 * @param newValue  new value for setting
	 * @return changed setting
	 */
	Enum putSetting(Enum settingName, Enum newValue);

	/**
	 * write all settings to the NBT Tag so they can be read later.
	 * 
	 * @param dest to be written nbt tag
	 */
	void writeToNBT(NBTTagCompound dest);

	/**
	 * Only works after settings have been registered
	 * 
	 * @param src to be read nbt tag
	 */
	void readFromNBT(NBTTagCompound src);

}
