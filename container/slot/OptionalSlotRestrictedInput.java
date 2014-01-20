package appeng.container.slot;

import net.minecraft.inventory.IInventory;

public class OptionalSlotRestrictedInput extends SlotRestrictedInput
{

	final int groupNum;
	IOptionalSlotHost host;

	public OptionalSlotRestrictedInput(PlaceableItemType valid, IInventory i, IOptionalSlotHost host, int slotnum, int x, int y, int grpNum) {
		super( valid, i, slotnum, x, y );
		this.groupNum = grpNum;
		this.host = host;
	}

	@Override
	public boolean isEnabled()
	{
		if ( host == null )
			return false;

		return host.isSlotEnabled( groupNum );
	}

}
