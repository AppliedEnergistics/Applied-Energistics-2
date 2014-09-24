package appeng.container.slot;

import net.minecraft.inventory.IInventory;

public class SlotPlayerHotBar extends AppEngSlot
{

	public SlotPlayerHotBar(IInventory par1iInventory, int par2, int par3, int par4) {
		super( par1iInventory, par2, par3, par4 );
		isPlayerSide = true;
	}
}
