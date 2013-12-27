package appeng.tile.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface IAEAppEngInventory
{

	IInventory getInternalInventory();

	void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack);

}
