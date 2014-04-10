package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotPatternTerm extends OptionalSlotFake
{

	public SlotPatternTerm(IInventory inv, IOptionalSlotHost h, int idx, int x, int y, int grpnum) {
		super( inv, h, idx, x, y, 0, 0, grpnum );
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return super.canTakeStack( par1EntityPlayer );
	}

	@Override
	public void onPickupFromSlot(EntityPlayer p, ItemStack is)
	{
		super.onPickupFromSlot( p, is );
	}

}
