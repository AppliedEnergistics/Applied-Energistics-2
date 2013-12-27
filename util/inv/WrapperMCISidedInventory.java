package appeng.util.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class WrapperMCISidedInventory extends WrapperInventoryRange implements IInventory, IInventoryWrapper
{

	private ForgeDirection dir;
	ISidedInventory side;

	public WrapperMCISidedInventory(ISidedInventory a, ForgeDirection d) {
		super( a, a.getAccessibleSlotsFromSide( d.ordinal() ), false );
		side = a;
		dir = d;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{

		if ( ignoreValidItems )
			return true;

		if ( side.isItemValidForSlot( slots[i], itemstack ) )
			return side.canInsertItem( slots[i], itemstack, dir.ordinal() );

		return false;
	}

	@Override
	public boolean canRemoveItemFromSlot(int i, ItemStack is)
	{
		if ( is == null )
			return false;

		return side.canExtractItem( slots[i], is, dir.ordinal() );
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		if ( canRemoveItemFromSlot( var1, getStackInSlot( var1 ) ) )
			return super.decrStackSize( var1, var2 );
		return null;
	}

}
