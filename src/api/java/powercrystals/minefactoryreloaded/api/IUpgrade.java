package powercrystals.minefactoryreloaded.api;

import net.minecraft.item.ItemStack;

/**
 * @author skyboy
 *
 * Defines an upgrade item for use in various MFR machines.
 */
public interface IUpgrade
{
	public static enum UpgradeType
	{
		RADIUS,
		SPEED,
		EFFICIENCY,
		STRENGTH,
		ENERGY;
	}
	
	/**
	 * Called to get the strength of the upgrade
	 * @param type The type of the upgrade
	 * @param stack The upgrade ItemStack.
	 * @return The strength of the upgrade
	 */
	public int getUpgradeLevel(UpgradeType type, ItemStack stack);
	
	/**
	 * Called to get what the upgrade is applicable for
	 * @param type The type of the upgrade
	 * @param stack The upgrade ItemStack.
	 * @return True if the upgrade is effective for the upgrade type
	 */
	public boolean isApplicableFor(UpgradeType type, ItemStack stack);
}
