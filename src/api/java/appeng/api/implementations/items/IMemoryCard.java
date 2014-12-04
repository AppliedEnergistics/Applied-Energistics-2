package appeng.api.implementations.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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
	 * @param is item
	 * @param SettingsName
	 *            unlocalized string that represents the tile entity.
	 * @param data
	 *            may contain a String called "tooltip" which is is a
	 *            unlocalized string displayed after the settings name, optional
	 *            but can be used to add details to the card for later.
	 */
	void setMemoryCardContents(ItemStack is, String SettingsName, NBTTagCompound data);

	/**
	 * returns the settings name provided by a previous call to
	 * setMemoryCardContents, or "AppEng.GuiITooltip.Blank" if there was no
	 * previous call to setMemoryCardContents.
	 * 
	 * @param is item
	 * @return setting name
	 */
	String getSettingsName(ItemStack is);

	/**
	 * @param is item
	 * @return the NBT Data previously saved by setMemoryCardContents, or an
	 *         empty NBTCompound
	 */
	NBTTagCompound getData(ItemStack is);

	/**
	 * notify the user of a outcome related to the memory card.
	 * 
	 * @param player
	 *            that used the card.
	 * @param msg
	 *            which message to send.
	 */
	void notifyUser(EntityPlayer player, MemoryCardMessages msg);

}
