package appeng.util.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class WrapperInvSlot
{

	class InternalInterfaceWrapper implements IInventory
	{

		private final IInventory inv;
		private final int slot;

		public InternalInterfaceWrapper(IInventory target, int slot) {
			this.inv = target;
			this.slot = slot;
		}

		@Override
		public int getSizeInventory()
		{
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int i)
		{
			return inv.getStackInSlot( slot );
		}

		@Override
		public ItemStack decrStackSize(int i, int num)
		{
			return inv.decrStackSize( slot, num );
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int i)
		{
			return inv.getStackInSlotOnClosing( slot );
		}

		@Override
		public void setInventorySlotContents(int i, ItemStack itemstack)
		{
			inv.setInventorySlotContents( slot, itemstack );
		}

		@Override
		public String getInventoryName()
		{
			return inv.getInventoryName();
		}

		@Override
		public boolean hasCustomInventoryName()
		{
			return inv.hasCustomInventoryName();
		}

		@Override
		public int getInventoryStackLimit()
		{
			return inv.getInventoryStackLimit();
		}

		@Override
		public void markDirty()
		{
			inv.markDirty();
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer entityplayer)
		{
			return inv.isUseableByPlayer( entityplayer );
		}

		@Override
		public void openInventory()
		{
			inv.openInventory();
		}

		@Override
		public void closeInventory()
		{
			inv.closeInventory();
		}

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return isItemValid( itemstack ) && inv.isItemValidForSlot( slot, itemstack );
		}
	}

	private final IInventory inv;

	public WrapperInvSlot(IInventory inv) {
		this.inv = inv;
	}

	public IInventory getWrapper(int slot)
	{
		return new InternalInterfaceWrapper( inv, slot );
	}

	protected boolean isItemValid(ItemStack itemstack)
	{
		return true;
	}

}
