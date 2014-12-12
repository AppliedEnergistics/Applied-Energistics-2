package appeng.api.implementations.items;

import net.minecraft.item.ItemStack;
import appeng.api.config.AccessRestriction;
import appeng.api.networking.energy.IAEPowerStorage;

/**
 * Basically the same as {@link IAEPowerStorage}, but for items.
 */
public interface IAEItemPowerStorage
{

	/**
	 * Inject amt, power into the device, it will store what it can, and return
	 * the amount unable to be stored.
	 * 
	 * @return amount unable to be stored
	 */
	public double injectAEPower(ItemStack is, double amt);

	/**
	 * Attempt to extract power from the device, it will extract what it can and
	 * return it.
	 * 
	 * @param amt to be extracted power from device
	 * @return what it could extract
	 */
	public double extractAEPower(ItemStack is, double amt);

	/**
	 * @return the current maximum power ( this can change :P )
	 */
	public double getAEMaxPower(ItemStack is);

	/**
	 * @return the current AE Power Level, this may exceed getMEMaxPower()
	 */
	public double getAECurrentPower(ItemStack is);

	/**
	 * Control the power flow by telling what the network can do, either add? or
	 * subtract? or both!
	 * 
	 * @return access restriction of network
	 */
	public AccessRestriction getPowerFlow(ItemStack is);

}