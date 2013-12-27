package appeng.util.inv;

import net.minecraft.item.ItemStack;

public interface IInventoryWrapper
{

	boolean canRemoveItemFromSlot(int x, ItemStack is);

}
