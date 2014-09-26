package powercrystals.minefactoryreloaded.api;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * @author PowerCrystals
 *
 * Defines an object that can display information about a captured mob in a Safari net.
 */
public interface ISafariNetHandler
{
	/**
	 * @return The class of mob that this handler applies to.
	 */
	public Class<?> validFor();
	
	/**
	 * @param safariNetStack The Safari Net that is requesting information.
	 * @param player The player holding the Safari Net.
	 * @param infoList The current list of information strings. Add yours to this.
	 * @param advancedTooltips True if the advanced tooltips option is on.
	 */
	@SuppressWarnings("rawtypes")
	public void addInformation(ItemStack safariNetStack, EntityPlayer player, List infoList, boolean advancedTooltips);
}
