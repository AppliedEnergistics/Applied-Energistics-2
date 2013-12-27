package appeng.container.slot;

import net.minecraft.inventory.IInventory;

// there is nothing special about this slot, its simply used to represent the players inventory, vs a container slot.

public class SlotPlayerInv extends AppEngSlot implements ISlotPlayerSide
{

	public SlotPlayerInv(IInventory par1iInventory, int par2, int par3, int par4) {
		super( par1iInventory, par2, par3, par4 );
	}
}
