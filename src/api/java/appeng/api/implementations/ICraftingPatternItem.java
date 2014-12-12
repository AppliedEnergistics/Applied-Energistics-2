package appeng.api.implementations;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.networking.crafting.ICraftingPatternDetails;

/**
 * Implemented on {@link Item}
 */
public interface ICraftingPatternItem
{

	/**
	 * Access Details about a pattern
	 * 
	 * @param is pattern
	 * @param w crafting world
	 * @return details of pattern
	 */
	ICraftingPatternDetails getPatternForItem(ItemStack is, World w);
}
