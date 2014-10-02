package appeng.util.inv;

import java.util.Iterator;

import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.InventoryAdaptor;
import appeng.util.item.AEItemStack;

import com.google.common.collect.ImmutableList;

public class IMEAdaptor extends InventoryAdaptor
{

	final IMEInventory<IAEItemStack> target;
	final BaseActionSource src;
	int maxSlots = 0;

	public IMEAdaptor(IMEInventory<IAEItemStack> input, BaseActionSource src) {
		target = input;
		this.src = src;
	}

	IItemList<IAEItemStack> getList()
	{
		return target.getAvailableItems( AEApi.instance().storage().createItemList() );
	}

	@Override
	public Iterator<ItemSlot> iterator()
	{
		return new IMEAdaptorIterator( this, getList() );
	}

	public ItemStack doRemoveItemsFuzzy(int how_many, ItemStack Filter, IInventoryDestination destination, Actionable type, FuzzyMode fuzzyMode)
	{
		IAEItemStack reqFilter = AEItemStack.create( Filter );
		if ( reqFilter == null )
			return null;

		IAEItemStack out = null;

		for (IAEItemStack req : ImmutableList.copyOf( getList().findFuzzy( reqFilter, fuzzyMode ) ))
		{
			if ( req != null )
			{
				req.setStackSize( how_many );
				out = target.extractItems( req, type, src );
				if ( out != null )
					return out.getItemStack();
			}
		}

		return null;
	}

	public ItemStack doRemoveItems(int how_many, ItemStack Filter, IInventoryDestination destination, Actionable type)
	{
		IAEItemStack req = null;

		if ( Filter == null )
		{
			IItemList<IAEItemStack> list = getList();
			if ( !list.isEmpty() )
				req = list.getFirstItem();
		}
		else
			req = AEItemStack.create( Filter );

		IAEItemStack out = null;

		if ( req != null )
		{
			req.setStackSize( how_many );
			out = target.extractItems( req, type, src );
		}

		if ( out != null )
			return out.getItemStack();

		return null;
	}

	@Override
	public ItemStack removeItems(int how_many, ItemStack Filter, IInventoryDestination destination)
	{
		return doRemoveItems( how_many, Filter, destination, Actionable.MODULATE );
	}

	@Override
	public ItemStack simulateRemove(int how_many, ItemStack Filter, IInventoryDestination destination)
	{
		return doRemoveItems( how_many, Filter, destination, Actionable.SIMULATE );
	}

	@Override
	public ItemStack removeSimilarItems(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		if ( filter == null )
			return doRemoveItems( how_many, null, destination, Actionable.MODULATE );
		return doRemoveItemsFuzzy( how_many, filter, destination, Actionable.MODULATE, fuzzyMode );
	}

	@Override
	public ItemStack simulateSimilarRemove(int how_many, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination)
	{
		if ( filter == null )
			return doRemoveItems( how_many, null, destination, Actionable.SIMULATE );
		return doRemoveItemsFuzzy( how_many, filter, destination, Actionable.SIMULATE, fuzzyMode );
	}

	@Override
	public ItemStack addItems(ItemStack A)
	{
		IAEItemStack in = AEItemStack.create( A );
		if ( in != null )
		{
			IAEItemStack out = target.injectItems( in, Actionable.MODULATE, src );
			if ( out != null )
				return out.getItemStack();
		}
		return null;
	}

	@Override
	public ItemStack simulateAdd(ItemStack A)
	{
		IAEItemStack in = AEItemStack.create( A );
		if ( in != null )
		{
			IAEItemStack out = target.injectItems( in, Actionable.SIMULATE, src );
			if ( out != null )
				return out.getItemStack();
		}
		return null;
	}

	@Override
	public boolean containsItems()
	{
		return !getList().isEmpty();
	}

}
