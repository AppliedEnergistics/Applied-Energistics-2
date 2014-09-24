package appeng.items.contents;

import net.minecraft.item.ItemStack;
import appeng.api.implementations.guiobjects.IGuiItemObject;

public class QuartzKnifeObj implements IGuiItemObject
{

	final ItemStack is;

	public QuartzKnifeObj(ItemStack o) {
		is = o;
	}

	@Override
	public ItemStack getItemStack()
	{
		return is;
	}

}
