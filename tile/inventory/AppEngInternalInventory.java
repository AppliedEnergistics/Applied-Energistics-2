package appeng.tile.inventory;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.storage.IMEInventory;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.util.Platform;
import appeng.util.iterators.InvIterator;

public class AppEngInternalInventory implements IInventory, Iterable<ItemStack>
{

	protected IAEAppEngInventory te;
	protected int size;
	protected int maxStack;

	protected ItemStack inv[];

	public IMEInventory getIMEI()
	{
		return new MEIInventoryWrapper( this, null );
	}

	public boolean isEmpty()
	{
		for (int x = 0; x < getSizeInventory(); x++)
			if ( getStackInSlot( x ) != null )
				return false;
		return true;
	}

	public AppEngInternalInventory(IAEAppEngInventory _te, int s) {
		te = _te;
		size = s;
		maxStack = 64;
		inv = new ItemStack[s];
	}

	protected boolean eventsEnabled()
	{
		return Platform.isServer();
	}

	public void setMaxStackSize(int s)
	{
		maxStack = s;
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		return inv[var1];
	}

	@Override
	public ItemStack decrStackSize(int slot, int qty)
	{
		if ( inv[slot] != null )
		{
			ItemStack split = getStackInSlot( slot );
			ItemStack ns = null;

			if ( qty >= split.stackSize )
			{
				ns = inv[slot];
				inv[slot] = null;
			}
			else
				ns = split.splitStack( qty );

			if ( te != null && eventsEnabled() )
			{
				te.onChangeInventory( this, slot, InvOperation.decrStackSize, ns, null );
			}

			return ns;
		}

		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack newItemStack)
	{
		ItemStack oldStack = inv[slot];
		inv[slot] = newItemStack;

		if ( te != null && eventsEnabled() )
		{
			ItemStack removed = oldStack;
			ItemStack added = newItemStack;

			if ( oldStack != null && newItemStack != null && Platform.isSameItem( oldStack, newItemStack ) )
			{
				if ( oldStack.stackSize > newItemStack.stackSize )
				{
					removed = removed.copy();
					removed.stackSize -= newItemStack.stackSize;
					added = null;
				}
				else if ( oldStack.stackSize < newItemStack.stackSize )
				{
					added = added.copy();
					added.stackSize -= oldStack.stackSize;
					removed = null;
				}
				else
				{
					removed = added = null;
				}
			}

			te.onChangeInventory( this, slot, InvOperation.setInventorySlotContents, removed, added );
		}
	}

	@Override
	public void onInventoryChanged()
	{
		if ( te != null && eventsEnabled() )
		{
			te.onChangeInventory( this, -1, InvOperation.onInventoryChanged, null, null );
		}
	}

	@Override
	public int getInventoryStackLimit()
	{
		return maxStack > 64 ? 64 : maxStack;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return true;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	public void writeToNBT(NBTTagCompound target)
	{
		for (int x = 0; x < size; x++)
		{
			try
			{
				NBTTagCompound c = new NBTTagCompound();

				if ( inv[x] != null )
				{
					inv[x].writeToNBT( c );
				}

				target.setCompoundTag( "#" + x, c );
			}
			catch (Exception err)
			{
			}
		}
	}

	public void readFromNBT(NBTTagCompound target)
	{
		for (int x = 0; x < size; x++)
		{
			try
			{
				NBTTagCompound c = target.getCompoundTag( "#" + x );

				if ( c != null )
					inv[x] = ItemStack.loadItemStackFromNBT( c );

			}
			catch (Exception err)
			{
				err.printStackTrace();
			}
		}
	}

	public void writeToNBT(NBTTagCompound data, String name)
	{
		NBTTagCompound c = new NBTTagCompound();
		writeToNBT( c );
		data.setCompoundTag( name, c );
	}

	public void readFromNBT(NBTTagCompound data, String name)
	{
		NBTTagCompound c = data.getCompoundTag( name );
		if ( c != null )
			readFromNBT( c );
	}

	@Override
	public int getSizeInventory()
	{
		return size;
	}

	@Override
	public String getInvName()
	{
		return "appeng-internal";
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public Iterator<ItemStack> iterator()
	{
		return new InvIterator( this );
	}
}
