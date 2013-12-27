package appeng.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotOutput extends AppEngSlot
{

	public SlotOutput(IInventory a, int b, int c, int d, int i) {
		super( a, b, c, d );
		icon = i;
	}

	@Override
	public boolean isItemValid(ItemStack i)
	{
		return false;
	}
}
