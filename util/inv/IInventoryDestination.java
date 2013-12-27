package appeng.util.inv;

import net.minecraft.item.ItemStack;

public interface IInventoryDestination
{

	public boolean canInsert(ItemStack stack);

}
