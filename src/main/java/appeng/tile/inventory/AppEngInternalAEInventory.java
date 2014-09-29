package appeng.tile.inventory;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.iterators.AEInvIterator;
import appeng.util.iterators.InvIterator;

public class AppEngInternalAEInventory implements IInventory, Iterable<ItemStack>
{

	protected IAEAppEngInventory te;
	int size;
	int maxStack;

	protected IAEItemStack inv[];

	public boolean isEmpty()
	{
		for (int x = 0; x < getSizeInventory(); x++)
			if ( getStackInSlot( x ) != null )
				return false;
		return true;
	}

	public AppEngInternalAEInventory(IAEAppEngInventory _te, int s) {
		te = _te;
		size = s;
		maxStack = 64;
		inv = new IAEItemStack[s];
	}

	public void setMaxStackSize(int s)
	{
		maxStack = s;
	}

	public IAEItemStack getAEStackInSlot(int var1)
	{
		return inv[var1];
	}

	@Override
	public ItemStack getStackInSlot(int var1)
	{
		if ( inv[var1] == null )
			return null;

		return inv[var1].getItemStack();
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
				ns = getStackInSlot( slot );
				inv[slot] = null;
			}
			else
				ns = split.splitStack( qty );

			if ( te != null && Platform.isServer() )
			{
				te.onChangeInventory( this, slot, InvOperation.decreaseStackSize, ns, null );
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
		ItemStack oldStack = getStackInSlot( slot );
		inv[slot] = AEApi.instance().storage().createItemStack( newItemStack );

		if ( te != null && Platform.isServer() )
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
	public void markDirty()
	{
		if ( te != null && Platform.isServer() )
		{
			te.onChangeInventory( this, -1, InvOperation.markDirty, null, null );
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
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
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

				target.setTag( "#" + x, c );
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
					inv[x] = AEItemStack.loadItemStackFromNBT( c );

			}
			catch (Exception e)
			{
				AELog.error( e );
			}
		}
	}

	public void writeToNBT(NBTTagCompound data, String name)
	{
		NBTTagCompound c = new NBTTagCompound();
		writeToNBT( c );
		data.setTag( name, c );
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
	public String getInventoryName()
	{
		return "appeng-internal";
	}

	@Override
	public boolean hasCustomInventoryName()
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

	public Iterator<IAEItemStack> getNewAEIterator()
	{
		return new AEInvIterator( this );
	}
}
