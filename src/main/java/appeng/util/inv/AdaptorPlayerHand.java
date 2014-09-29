package appeng.util.inv;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import appeng.api.config.FuzzyMode;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.iterators.NullIterator;

/*
 * Lets you do simply tests with the players cursor, without messing with the specifics.
 */
public class AdaptorPlayerHand extends InventoryAdaptor
{

	private EntityPlayer p;

	public AdaptorPlayerHand(EntityPlayer _p) {
		p = _p;
	}

	@Override
	public ItemStack removeSimilarItems(int how_many, ItemStack Filter, FuzzyMode fuzzyMode, IInventoryDestination dest)
	{
		ItemStack hand = p.inventory.getItemStack();
		if ( hand == null )
			return null;

		if ( Filter == null || Platform.isSameItemFuzzy( Filter, hand, fuzzyMode ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > how_many ? how_many : hand.stackSize;
			hand.stackSize -= how_many;
			if ( hand.stackSize <= 0 )
				p.inventory.setItemStack( null );
			return result;
		}

		return null;
	}

	@Override
	public ItemStack simulateSimilarRemove(int how_many, ItemStack Filter, FuzzyMode fuzzyMode, IInventoryDestination dest)
	{

		ItemStack hand = p.inventory.getItemStack();
		if ( hand == null )
			return null;

		if ( Filter == null || Platform.isSameItemFuzzy( Filter, hand, fuzzyMode ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > how_many ? how_many : hand.stackSize;
			return result;
		}

		return null;
	}

	@Override
	public ItemStack removeModItems(int how_many, ItemStack Filter, IInventoryDestination dest)
	{
		ItemStack hand = p.inventory.getItemStack();
		if ( hand == null )
			return null;

		if ( Filter == null || compareMods( hand.getItem(), Filter.getItem()) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > how_many ? how_many : hand.stackSize;
			hand.stackSize -= how_many;
			if ( hand.stackSize <= 0 )
				p.inventory.setItemStack( null );
			return result;
		}

		return null;
	}

	@Override
	public ItemStack simulateModRemove(int how_many, ItemStack Filter, IInventoryDestination dest)
	{

		ItemStack hand = p.inventory.getItemStack();
		if ( hand == null )
			return null;

		if ( Filter == null || compareMods( hand.getItem(), Filter.getItem()) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > how_many ? how_many : hand.stackSize;
			return result;
		}

		return null;
	}

	@Override
	public ItemStack removeItems(int how_many, ItemStack Filter, IInventoryDestination dest)
	{
		ItemStack hand = p.inventory.getItemStack();
		if ( hand == null )
			return null;

		if ( Filter == null || Platform.isSameItemPrecise( Filter, hand ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > how_many ? how_many : hand.stackSize;
			hand.stackSize -= how_many;
			if ( hand.stackSize <= 0 )
				p.inventory.setItemStack( null );
			return result;
		}

		return null;
	}

	@Override
	public ItemStack simulateRemove(int how_many, ItemStack Filter, IInventoryDestination dest)
	{

		ItemStack hand = p.inventory.getItemStack();
		if ( hand == null )
			return null;

		if ( Filter == null || Platform.isSameItemPrecise( Filter, hand ) )
		{
			ItemStack result = hand.copy();
			result.stackSize = hand.stackSize > how_many ? how_many : hand.stackSize;
			return result;
		}

		return null;
	}

	@Override
	public ItemStack addItems(ItemStack A)
	{

		if ( A == null )
			return null;
		if ( A.stackSize == 0 )
			return null;
		if ( p == null )
			return A;
		if ( p.inventory == null )
			return A;

		ItemStack hand = p.inventory.getItemStack();

		if ( hand != null && !Platform.isSameItem( A, hand ) )
			return A;

		int original = 0;
		ItemStack newHand = null;
		if ( hand == null )
			newHand = A.copy();
		else
		{
			newHand = hand;
			original = hand.stackSize;
			newHand.stackSize += A.stackSize;
		}

		if ( newHand.stackSize > newHand.getMaxStackSize() )
		{
			newHand.stackSize = newHand.getMaxStackSize();
			ItemStack B = A.copy();
			B.stackSize -= newHand.stackSize - original;
			p.inventory.setItemStack( newHand );
			return B;
		}

		p.inventory.setItemStack( newHand );
		return null;
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		ItemStack hand = p.inventory.getItemStack();
		if ( A == null )
			return null;

		if ( hand != null && !Platform.isSameItem( A, hand ) )
			return A;

		int original = 0;
		ItemStack newHand = null;
		if ( hand == null )
			newHand = A.copy();
		else
		{
			newHand = hand.copy();
			original = hand.stackSize;
			newHand.stackSize += A.stackSize;
		}

		if ( newHand.stackSize > newHand.getMaxStackSize() )
		{
			newHand.stackSize = newHand.getMaxStackSize();
			ItemStack B = A.copy();
			B.stackSize -= newHand.stackSize - original;
			return B;
		}

		return null;
	}

	@Override
	public boolean containsItems()
	{
		return p.inventory.getItemStack() != null;
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new NullIterator();
	}
}
