package appeng.container.slot;

import net.minecraft.inventory.IInventory;

public class SlotFakeBlacklist extends SlotFakeTypeOnly
{

	public SlotFakeBlacklist(IInventory inv, int idx, int x, int y) {
		super( inv, idx, x, y );
	}

	@Override
	public boolean renderIconWithItem()
	{
		return true;
	}

	@Override
	public float getOpacityOfIcon()
	{
		return 0.8f;
	}

	@Override
	public int getIcon()
	{
		if ( getHasStack() )
		{
			return getStack().stackSize > 0 ? 16 + 14 : 14;
		}
		return -1;
	}

}
