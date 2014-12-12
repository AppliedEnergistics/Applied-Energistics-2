package appeng.api.features;

import net.minecraft.item.ItemStack;

public interface INetworkEncodable {

	/**
	 * Used to get the current key from the item.
	 * 
	 * @param item item
	 * @return string key of item
	 */
	String getEncryptionKey(ItemStack item);

	/**
	 * Encode the wireless frequency via the Controller.
	 * 
	 * @param item
	 *            the wireless terminal.
	 * @param encKey
	 *            the wireless encryption key.
	 * @param name
	 *            null for now.
	 */
	void setEncryptionKey(ItemStack item, String encKey, String name);

}
