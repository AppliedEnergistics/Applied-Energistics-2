package net.mcft.copy.betterstorage.api;

import net.minecraft.item.ItemStack;

public final class BetterStorageUtils
{

	private BetterStorageUtils() {
	}

	/**
	 * Returns if the stack matches the match stack. Stack size is ignored. <br>
	 * If the match stack damage is WILDCARD_VALUE, it will match any damage. <br>
	 * If the match stack doesn't have an NBT compound, it will match any NBT data. <br>
	 * (If the match stack has an empty NBT compound it'll only match stacks without NBT data.)
	 */
	public static boolean wildcardMatch(ItemStack match, ItemStack stack)
	{
		return false;
	}

}
