package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotPatternTerm extends AppEngSlot
{

	public SlotPatternTerm(IInventory inv, int idx, int x, int y) {
		super( inv, idx, x, y );
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer p, ItemStack is)
	{
	}

}
