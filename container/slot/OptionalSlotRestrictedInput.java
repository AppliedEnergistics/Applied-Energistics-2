package appeng.container.slot;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

public class OptionalSlotRestrictedInput extends SlotRestrictedInput
{

	final int groupNum;
	IOptionalSlotHost host;

	public OptionalSlotRestrictedInput(PlacableItemType valid, IInventory i, IOptionalSlotHost host, int slotnum, int x, int y, int grpNum,
			InventoryPlayer invPlayer) {
		super( valid, i, slotnum, x, y, invPlayer );
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
