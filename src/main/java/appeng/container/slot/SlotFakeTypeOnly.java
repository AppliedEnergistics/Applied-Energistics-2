package appeng.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotFakeTypeOnly extends SlotFake
{

	public SlotFakeTypeOnly(IInventory inv, int idx, int x, int y) {
		super( inv, idx, x, y );
	}

	@Override
	public void putStack(ItemStack is)
	{
		if ( is != null )
		{
			is = is.copy();
			if ( is.stackSize > 1 )
				is.stackSize = 1;
			else if ( is.stackSize < -1 )
				is.stackSize = -1;
		}

		super.putStack( is );
	}
}
