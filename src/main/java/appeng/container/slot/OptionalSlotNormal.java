package appeng.container.slot;

import net.minecraft.inventory.IInventory;

public class OptionalSlotNormal extends AppEngSlot
{

	final int groupNum;
	final IOptionalSlotHost host;

	public OptionalSlotNormal(IInventory inv, IOptionalSlotHost containerBus, int slot, int xPos, int yPos, int groupNum) {
		super( inv, slot, xPos, yPos );
		this.groupNum = groupNum;
		host = containerBus;
	}

	@Override
	public boolean isEnabled()
	{
		if ( host == null )
			return false;

		return host.isSlotEnabled( groupNum );
	}

}
