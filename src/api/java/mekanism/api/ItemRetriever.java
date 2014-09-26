package mekanism.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Use this class's 'getItem()' method to retrieve ItemStacks from the 'Mekanism'
 * class.
 * @author AidanBrady
 *
 */
public final class ItemRetriever
{
	/** The 'Mekanism' class that items and blocks are retrieved from. */
	private static Class Mekanism;

	/**
	 * Attempts to retrieve an ItemStack of an item or block with the declared identifier.
	 *
	 * Mekanism identifiers follow an easy-to-remember pattern.  All identifiers
	 * are identical to the String returned by 'getItemName().'  None include spaces,
	 * and all start with a capital letter. The name that shows up in-game can be
	 * stripped down to identifier form by removing spaces and all non-alphabetic
	 * characters (,./'=-_). Below is an example:
	 *
	 * ItemStack enrichedAlloy = ItemRetriever.getItem("EnrichedAlloy");
	 *
	 * The same also works for blocks.
	 *
	 * ItemStack refinedObsidian = ItemRetriever.getItem("RefinedObsidian");
	 *
	 * Note that for items or blocks that have specific metadata you will need to create
	 * a new ItemStack with that specified value, as this will only return an ItemStack
	 * with the meta value '0.'
	 *
	 * Make sure you run this in or after FMLPostInitializationEvent runs, because most
	 * items are registered when FMLInitializationEvent runs. However, some items ARE
	 * registered later in order to hook into other mods. In a rare circumstance you may
	 * have to add "after:Mekanism" in the @Mod 'dependencies' annotation.
	 *
	 * @param identifier - a String to be searched in the 'Mekanism' class
	 * @return an ItemStack of the declared identifier, otherwise null.
	 */
	public static ItemStack getItem(String identifier)
	{
		try {
			if(Mekanism == null)
			{
				Mekanism = Class.forName("mekanism.common.Mekanism");
			}

			Object ret = Mekanism.getField(identifier).get(null);

			if(ret instanceof Item)
			{
				return new ItemStack((Item)ret, 1);
			}
			else if(ret instanceof Block)
			{
				return new ItemStack((Block)ret, 1);
			}
			else {
				return null;
			}
		} catch(Exception e) {
			System.err.println("Error retrieving item with identifier '" + identifier + "': " + e.getMessage());
			return null;
		}
	}
}
