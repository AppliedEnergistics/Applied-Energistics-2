package appeng.util.inv;

import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public class IMEInventoryDestination implements IInventoryDestination
{

	IMEInventory<IAEItemStack> me;

	public IMEInventoryDestination(IMEInventory<IAEItemStack> o) {
		me = o;
	}

	@Override
	public boolean canInsert(ItemStack stack)
	{

		if ( stack == null )
			return false;

		IAEItemStack failed = me.injectItems( AEItemStack.create( stack ), Actionable.SIMULATE );

		if ( failed == null )
			return true;
		return failed.getStackSize() != stack.stackSize;
	}

}
