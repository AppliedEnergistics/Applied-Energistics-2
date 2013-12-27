package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotDisabled extends AppEngSlot
{

	public SlotDisabled(IInventory par1iInventory, int slotIndex, int x, int y) {
		super( par1iInventory, slotIndex, x, y );
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return false;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}
}
