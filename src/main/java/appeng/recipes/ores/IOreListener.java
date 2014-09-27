package appeng.recipes.ores;

import net.minecraft.item.ItemStack;

public interface IOreListener
{

	/**
	 * Called with various items registered in the dictionary.
	 * AppEng.oreDictionary.observe(...) to register them.
	 * 
	 * @param name name of ore
	 * @param item item with name
	 */
	void oreRegistered(String name, ItemStack item);

}
