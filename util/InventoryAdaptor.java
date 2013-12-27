package appeng.util;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.FuzzyMode;
import appeng.integration.modules.BS;
import appeng.util.inv.AdaptorIInventory;
import appeng.util.inv.AdaptorISpecialInventory;
import appeng.util.inv.AdaptorList;
import appeng.util.inv.AdaptorPlayerInventory;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import appeng.util.inv.WrapperMCISidedInventory;
import buildcraft.api.inventory.ISpecialInventory;
import cpw.mods.fml.common.network.Player;

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

		if ( te instanceof Player )
		{
			return new AdaptorIInventory( new AdaptorPlayerInventory( ((EntityPlayer) te).inventory ) );
		}
		else if ( te instanceof ArrayList )
		{
			return new AdaptorList( (ArrayList) te );
		}
		else if ( BS.instance != null && BS.instance.isStorageCrate( te ) )
		{
			return BS.instance.getAdaptor( te, d );
		}
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
			return new AdaptorIInventory( new WrapperMCISidedInventory( (ISidedInventory) te, d ) );
		}
		else if ( te instanceof IInventory )
		{
			return new AdaptorIInventory( (IInventory) te );
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
