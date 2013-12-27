package appeng.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotInaccessable extends AppEngSlot
{

	public SlotInaccessable(IInventory i, int slotIdx, int x, int y) {
		super( i, slotIdx, x, y );
	}

	ItemStack dspStack = null;

	@Override
	public ItemStack getDisplayStack()
	{
		if ( dspStack == null )
		{
			ItemStack dsp = super.getDisplayStack();
			if ( dsp != null )
				dspStack = dsp.copy();
		}
		return dspStack;
	}

	@Override
	public void onSlotChanged()
	{
		super.onSlotChanged();
		dspStack = null;
	}

	@Override
	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	@Override
	public boolean isItemValid(ItemStack i)
	{
		return false;
	}

}
