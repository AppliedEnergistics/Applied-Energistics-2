package appeng.helpers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.implementations.IUpgradeModule;
import appeng.api.networking.IGridHost;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;

public class NetworkToolInv implements INetworkTool
{

	final AppEngInternalInventory inv;
	final ItemStack is;
	final IGridHost gh;

	public NetworkToolInv(ItemStack is, IGridHost gHost) {
		this.is = is;
		gh = gHost;
		inv = new AppEngInternalInventory( null, 9 );
		inv.readFromNBT( Platform.openNbtData( is ), "inv" );
	}

	@Override
	public int getSizeInventory()
	{
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return inv.getStackInSlot( i );
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return inv.decrStackSize( i, j );
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return inv.getStackInSlotOnClosing( i );
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		inv.setInventorySlotContents( i, itemstack );
	}

	@Override
	public String getInvName()
	{
		return inv.getInvName();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return inv.isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return inv.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged()
	{
		inv.onInventoryChanged();
		inv.writeToNBT( Platform.openNbtData( is ), "inv" );
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return inv.isUseableByPlayer( entityplayer );
	}

	@Override
	public void openChest()
	{
		inv.openChest();
	}

	@Override
	public void closeChest()
	{
		inv.closeChest();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return inv.isItemValidForSlot( i, itemstack ) && itemstack.getItem() instanceof IUpgradeModule
				&& ((IUpgradeModule) itemstack.getItem()).getType( itemstack ) != null;
	}

	@Override
	public ItemStack getItemStack()
	{
		return is;
	}

	@Override
	public IGridHost getGridHost()
	{
		return gh;
	}

}
