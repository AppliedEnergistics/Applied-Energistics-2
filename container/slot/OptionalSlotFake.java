package appeng.container.slot;

import net.minecraft.inventory.IInventory;

public class OptionalSlotFake extends SlotFake
{

	int invSlot;
	final int groupNum;
	IOptionalSlotHost host;

	int srcX;
	int srcY;

	public OptionalSlotFake(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
		super( inv, idx, x + offX * 18, y + offY * 18 );
		srcX = x;
		srcY = y;
		invSlot = idx;
		this.groupNum = groupNum;
	}

	@Override
	public boolean isEnabled()
	{
		return host.isSlotEnabled( groupNum, this );
	}

}
