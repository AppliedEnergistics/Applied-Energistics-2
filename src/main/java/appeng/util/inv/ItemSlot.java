package appeng.util.inv;

import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public class ItemSlot
{

	public int slot;

	// one or the other..
	private IAEItemStack aeItemStack;
	private ItemStack itemStack;

	public boolean isExtractable;

	public void setItemStack(ItemStack is)
	{
		aeItemStack = null;
		itemStack = is;
	}

	public void setAEItemStack(IAEItemStack is)
	{
		aeItemStack = is;
		itemStack = null;
	}

	public ItemStack getItemStack()
	{
		return itemStack == null ? (aeItemStack == null ? null : (itemStack = aeItemStack.getItemStack())) : itemStack;
	}

	public IAEItemStack getAEItemStack()
	{
		return aeItemStack == null ? (itemStack == null ? null : (aeItemStack = AEItemStack.create( itemStack ))) : aeItemStack;
	}

}
