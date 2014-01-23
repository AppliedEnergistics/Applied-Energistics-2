package appeng.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.api.networking.IGridHost;

public interface INetworkTool extends IInventory
{

	IGridHost getGridHost(); // null for most purposes.

	public ItemStack getItemStack();

}
