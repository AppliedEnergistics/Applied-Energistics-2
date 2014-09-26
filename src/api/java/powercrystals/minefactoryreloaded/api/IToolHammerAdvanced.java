package powercrystals.minefactoryreloaded.api;

import net.minecraft.item.ItemStack;

/**
 * @author PowerCrystals
 *
 * This interface is like IToolHammer, but is for items that change state on a per-stack basis. Implement this
 * instead of IToolHammer - not both!
 * 
 * This interface will replace IToolHammer in MC 1.6.
 */
public interface IToolHammerAdvanced
{
	public boolean isActive(ItemStack stack);
}
