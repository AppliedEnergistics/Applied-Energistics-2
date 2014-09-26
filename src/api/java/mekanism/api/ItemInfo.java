package mekanism.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemInfo
{
	public Item item;
	public int meta;

	public ItemInfo(Item i, int j)
	{
		item = i;
		meta = j;
	}

	public static ItemInfo get(ItemStack stack)
	{
		return new ItemInfo(stack.getItem(), stack.getItemDamage());
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof BlockInfo &&
				((ItemInfo)obj).item == item &&
				((ItemInfo)obj).meta == meta;
	}

	@Override
	public int hashCode()
	{
		int code = 1;
		code = 31 * code + item.getUnlocalizedName().hashCode();
		code = 31 * code + meta;
		return code;
	}
}
