package appeng.container.slot;

import net.minecraft.inventory.IInventory;

public class SlotPatternOutputs extends OptionalSlotFake
{

	public SlotPatternOutputs(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
		super( inv, containerBus, idx, x, y, offX, offY, groupNum );
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public boolean shouldDisplay()
	{
		return super.isEnabled();
	}
}
