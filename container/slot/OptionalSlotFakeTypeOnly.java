package appeng.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class OptionalSlotFakeTypeOnly extends OptionalSlotFake
{

	public OptionalSlotFakeTypeOnly(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
		super( inv, containerBus, idx, x, y, offX, offY, groupNum );
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
