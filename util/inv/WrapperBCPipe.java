package appeng.util.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.core.AppEng;
import appeng.integration.abstraction.IBC;

public class WrapperBCPipe implements IInventory
{

	final private IBC bc;
	final private TileEntity ad;
	final private ForgeDirection dir;

	public WrapperBCPipe(TileEntity te, ForgeDirection d) {
		bc = (IBC) AppEng.instance.getIntegration( "BC" );
		ad = te;
		dir = d;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		bc.addItemsToPipe( ad, itemstack, dir );
	}

	@Override
	public String getInventoryName()
	{
		return "BC Pipe Wrapper";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public void closeInventory()
	{

	}

	@Override
	public void openInventory()
	{

	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public void markDirty()
	{

	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return bc.canAddItemsToPipe( ad, itemstack, dir );
	}

}
