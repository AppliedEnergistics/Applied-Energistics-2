package appeng.helpers;

import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public interface ISharedTagCompoundCache
{

	final class CacheIAEItemStack
	{

		ItemStack src;
		AEItemStack stack;
	};

	CacheIAEItemStack[] itemStackCache = new CacheIAEItemStack[0];

	void checkCacheSetup(int size);

	IAEItemStack getItem(int x, ItemStack is);

}
