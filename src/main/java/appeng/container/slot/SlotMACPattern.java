package appeng.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import appeng.container.implementations.ContainerMAC;

public class SlotMACPattern extends AppEngSlot
{

	final ContainerMAC mac;

	public SlotMACPattern(ContainerMAC mac, IInventory i, int slotIdx, int x, int y) {
		super( i, slotIdx, x, y );
		this.mac = mac;
	}

	@Override
	public boolean isItemValid(ItemStack i)
	{
		return mac.isValidItemForSlot( this.getSlotIndex(), i );
	}

}
