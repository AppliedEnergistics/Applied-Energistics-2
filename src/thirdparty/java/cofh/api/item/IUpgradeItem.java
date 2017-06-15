package cofh.api.item;

import net.minecraft.item.ItemStack;

public interface IUpgradeItem {

	/**
	 * Enum for Upgrade Types - there aren't many.
	 */
	enum UpgradeType {
		INCREMENTAL, FULL, CREATIVE
	}

	/**
	 * Get the Upgrade Type for a given Upgrade.
	 *
	 * @param stack ItemStack representing the Upgrade.
	 * @return Upgrade Type of the stack.
	 */
	UpgradeType getUpgradeType(ItemStack stack);

	/**
	 * Get the Upgrade Level for a given Upgrade.
	 *
	 * @param stack ItemStack representing the Upgrade.
	 * @return Upgrade Level of the stack; -1 for a Creative Upgrade.
	 */
	int getUpgradeLevel(ItemStack stack);

}
