package appeng.util.inv;

import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public class ItemSlot
{

	public int slot;

	// one or the other..
	private IAEItemStack aeitemstack;
	private ItemStack itemStack;

	public boolean isExtractable;

	public void setItemStack(ItemStack is)
	{
		aeitemstack = null;
		itemStack = is;
	}

	public void setAEItemStack(IAEItemStack is)
	{
		aeitemstack = is;
		itemStack = null;
	}

	public ItemStack getItemStack()
	{
		return itemStack == null ? (aeitemstack == null ? null : (itemStack = aeitemstack.getItemStack())) : itemStack;
	}

	public IAEItemStack getAEItemStack()
	{
		return aeitemstack == null ? (itemStack == null ? null : (aeitemstack = AEItemStack.create( itemStack ))) : aeitemstack;
	}

}
