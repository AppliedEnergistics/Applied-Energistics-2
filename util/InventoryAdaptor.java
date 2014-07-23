package appeng.util;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.FuzzyMode;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.AdaptorISpecialInventory;
import appeng.util.inv.AdaptorList;
import appeng.util.inv.AdaptorPlayerInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import appeng.util.inv.WrapperMCISidedInventory;
import buildcraft.api.inventory.ISpecialInventory;

public abstract class InventoryAdaptor implements Iterable<ItemSlot>
{

	// return what was extracted.
	public abstract ItemStack removeItems(int how_many, ItemStack Filter, IInventoryDestination destination);

	public abstract ItemStack simulateRemove(int how_many, ItemStack Filter, IInventoryDestination destination);

	// return what was extracted.
	public abstract ItemStack removeSimilarItems(int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination);

	public abstract ItemStack simulateSimilarRemove(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination);

	// return what isn't used...
	public abstract ItemStack addItems(ItemStack A);

	public abstract ItemStack simulateAdd(ItemStack A);

	public abstract boolean containsItems();

	// returns an appropriate adaptor, or null
	public static InventoryAdaptor getAdaptor(Object te, ForgeDirection d)
	{
		if ( te == null )
			return null;

		if ( te instanceof EntityPlayer )
		{
			return new AdaptorIInventory( new AdaptorPlayerInventory( ((EntityPlayer) te).inventory ) );
		}
		else if ( te instanceof ArrayList )
		{
			return new AdaptorList( (ArrayList) te );
		}/*
		 * else if ( BS.instance != null && BS.instance.isStorageCrate( te ) ) { return BS.instance.getAdaptor( te, d );
		 * }
		 */
		else if ( te instanceof TileEntityChest )
		{
			return new AdaptorIInventory( (IInventory) Platform.GetChestInv( te ) );
		}
		else if ( isSpecialInventory( te ) )
		{
			return new AdaptorISpecialInventory( (ISpecialInventory) te, d );
		}
		else if ( te instanceof ISidedInventory )
		{
			ISidedInventory si =(ISidedInventory)te;
			int[] slots = si.getAccessibleSlotsFromSide( d.ordinal() );
			if ( si.getSizeInventory() > 0 && slots != null && slots.length > 0 )
				return new AdaptorIInventory( new WrapperMCISidedInventory( si, d ) );
		}
		else if ( te instanceof IInventory )
		{
			IInventory i =(IInventory)te;
			if ( i.getSizeInventory() > 0 )
				return new AdaptorIInventory( i );
		}

		return null;
	}

	private static boolean canBeSpecial = true;

	private static boolean isSpecialInventory(Object a)
	{
		if ( canBeSpecial )
		{
			try
			{
				return a instanceof ISpecialInventory;
			}
			catch (Throwable e)
			{
				canBeSpecial = false;
			}
		}
		return false;
	}
}
