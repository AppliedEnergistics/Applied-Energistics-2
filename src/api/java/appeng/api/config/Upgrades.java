package appeng.api.config;

import java.util.HashMap;

import net.minecraft.item.ItemStack;

public enum Upgrades
{
	/**
	 * Gold Tier Upgrades.
	 */
	CAPACITY(0), REDSTONE(0), CRAFTING(0),

	/**
	 * Diamond Tier Upgrades.
	 */
	FUZZY(1), SPEED(1), INVERTER(1);

	public final int myTier;
	public final HashMap<ItemStack, Integer> supportedMax = new HashMap<ItemStack, Integer>();

	private Upgrades(int tier) {
		myTier = tier;
	}

	/**
	 * @return list of Items/Blocks that support this upgrade, and how many it supports.
	 */
	public HashMap<ItemStack, Integer> getSupported()
	{
		return supportedMax;
	}

	public void registerItem(ItemStack myItem, int maxSupported)
	{
		if ( myItem != null )
			supportedMax.put( myItem, maxSupported );
	}
}
