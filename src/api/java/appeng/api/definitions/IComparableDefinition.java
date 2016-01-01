
package appeng.api.definitions;


import net.minecraft.item.ItemStack;


/**
 * Interface to compare a definition with an itemstack or a block
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IComparableDefinition
{
	/**
	 * Compare {@link ItemStack} with this
	 *
	 * @param comparableStack compared item
	 *
	 * @return true if the item stack is a matching item.
	 */
	boolean isSameAs( ItemStack comparableStack );
}
