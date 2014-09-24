package appeng.container.slot;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotCraftingMatrix extends AppEngSlot
{

	Container c;

	public SlotCraftingMatrix(Container c, IInventory par1iInventory, int par2, int par3, int par4) {
		super( par1iInventory, par2, par3, par4 );
		this.c = c;
	}

	@Override
	public boolean isPlayerSide()
	{
		return true;
	}

	@Override
	public void clearStack()
	{
		super.clearStack();
		c.onCraftMatrixChanged( inventory );
	}

	@Override
	public ItemStack decrStackSize(int par1)
	{
		ItemStack is = super.decrStackSize( par1 );
		c.onCraftMatrixChanged( inventory );
		return is;
	}

	@Override
	public void putStack(ItemStack par1ItemStack)
	{
		super.putStack( par1ItemStack );
		c.onCraftMatrixChanged( inventory );
	}

}
