package cofh.api.item;

import net.minecraft.item.ItemStack;

public interface IAugmentItem {

	/**
	 * Enum for Augment Types.
	 *
	 * BASIC - Standard augment, can have multiple.
	 * ADVANCED - Rare augment, multiples may or may not be allowed.
	 * MODE - Changes functionality greatly. Only allow one.
	 * ENDER - Integration with Ender Frequencies.
	 * CREATIVE - Super-powerful augments which cannot normally be obtained.
	 */
	enum AugmentType {
		BASIC, ADVANCED, MODE, ENDER, CREATIVE
	}

	/**
	 * Get the Augment Type for a given Augment.
	 *
	 * @param stack ItemStack representing the Augment.
	 * @return Augment Type of the stack.
	 */
	AugmentType getAugmentType(ItemStack stack);

	/**
	 * Get the Augment Identifier for a given Augment. This is simply a string with some description of what the Augment does. Individual
	 *
	 * @param stack ItemStack representing the Augment.
	 * @return Augment Type of the stack.
	 */
	String getAugmentIdentifier(ItemStack stack);

}
