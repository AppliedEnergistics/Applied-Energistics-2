package appeng.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class OptionalSlotFake extends SlotFake
{

	int invSlot;
	final int groupNum;
	IOptionalSlotHost host;

	public int srcX;
	public int srcY;

	public OptionalSlotFake(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
		super( inv, idx, x + offX * 18, y + offY * 18 );
		srcX = x;
		srcY = y;
		invSlot = idx;
		this.groupNum = groupNum;
		host = containerBus;
	}

	@Override
	public ItemStack getStack()
	{
		if ( !isEnabled() )
		{
			if ( getDisplayStack() != null )
				clearStack();
		}

		return super.getStack();
	}

	@Override
	public boolean isEnabled()
	{
		if ( host == null )
			return false;

		return host.isSlotEnabled( groupNum, this );
	}

}
