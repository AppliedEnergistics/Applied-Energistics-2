package appeng.recipes.ores;

import net.minecraft.item.ItemStack;

public interface IOreListener
{

	/**
	 * Called with various items registered in the dictionary.
	 * AppEng.oreDictionary.observe(...) to register them.
	 * 
	 * @param Name
	 * @param item
	 */
	void oreRegistered(String Name, ItemStack item);

}
