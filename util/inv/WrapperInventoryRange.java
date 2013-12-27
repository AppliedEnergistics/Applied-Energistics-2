package appeng.util.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class WrapperInventoryRange implements IInventory
{

	private IInventory src;
	int[] slots;
	protected boolean ignoreValidItems = false;

	public static String concateLines(int[] s, String separator)
	{
		if ( s.length > 0 )
		{
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length; i++)
			{
				if ( sb.length() > 0 )
					sb.append( separator );
				sb.append( s[i] );
			}
			return sb.toString();
		}
		return "";
	}

	public WrapperInventoryRange(IInventory a, int[] s, boolean ignoreValid) {
		src = a;
		slots = s;
		if ( slots == null )
			slots = new int[0];

		ignoreValidItems = ignoreValid;
	}

	public WrapperInventoryRange(IInventory a, int _min, int _size, boolean ignoreValid) {
		src = a;
		slots = new int[_size];
		for (int x = 0; x < _size; x++)
			slots[x] = _min + x;
		ignoreValidItems = ignoreValid;
	}

	@Override
	public int getSizeInventory()
	{
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		return src.getStackInSlot( slots[var1] );
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		return src.decrStackSize( slots[var1], var2 );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return src.getStackInSlotOnClosing( slots[var1] );
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2)
	{
		src.setInventorySlotContents( slots[var1], var2 );
	}

	@Override
	public String getInvName()
	{
		return src.getInvName();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return src.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged()
	{
		src.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return src.isUseableByPlayer( var1 );
	}

	@Override
	public void openChest()
	{
		src.openChest();
	}

	@Override
	public void closeChest()
	{
		src.closeChest();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if ( ignoreValidItems )
			return true;

		return src.isItemValidForSlot( slots[i], itemstack );
	}

}
