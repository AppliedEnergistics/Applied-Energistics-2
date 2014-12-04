package appeng.api.features;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.util.IConfigManager;

/**
 * A handler for a wireless terminal.
 */
public interface IWirelessTermHandler extends INetworkEncodable
{

	/**
	 * @param is wireless terminal
	 * @return true, if usePower, hasPower, etc... can be called for the provided item
	 */
	boolean canHandle(ItemStack is);

	/**
	 * use an amount of power, in AE units
	 * 
	 * @param amount
	 *            is in AE units ( 5 per MJ ), if you return false, the item should be dead and return false for
	 *            hasPower
	 * @param is wireless terminal
	 * @return true if wireless terminal uses power
	 */
	boolean usePower(EntityPlayer player, double amount, ItemStack is);

	/**
	 * gets the power status of the item.
	 * 
	 * @param is wireless terminal
	 * @return returns true if there is any power left.
	 */
	boolean hasPower(EntityPlayer player, double amount, ItemStack is);

	/**
	 * Return the config manager for the wireless terminal.
	 * 
	 * @param is wireless terminal
	 * @return config manager of wireless terminal
	 */
	IConfigManager getConfigManager(ItemStack is);
	
}