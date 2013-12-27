package appeng.me.storage;

import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.IStorageCell;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import appeng.util.item.ItemList;

public class CreativeCellInventory extends CellInventory implements IStorageCell
{

	ItemList itemListCache;

	public static IMEInventoryHandler getCell(ItemStack o)
	{
		try
		{
			return new CellInventoryHandler( new CreativeCellInventory( o ) );
		}
		catch (AppEngException e)
		{
		}

		return null;
	}

	protected CreativeCellInventory(ItemStack o) throws AppEngException {
		super( Platform.openNbtData( o ) );

		if ( MAX_ITEM_TYPES > 63 )
			MAX_ITEM_TYPES = 63;
		if ( MAX_ITEM_TYPES < 1 )
			MAX_ITEM_TYPES = 1;

		storedItems = tagCompound.getShort( ITEM_TYPE_TAG );
		storedItemCount = tagCompound.getInteger( ITEM_COUNT_TAG );
		cellItems = null;
	}

	@Override
	public boolean isStorageCell(ItemStack i)
	{
		return true;
	}

	@Override
	public int getBytes(ItemStack cellItem)
	{
		return 0;
	}

	@Override
	public int BytePerType(ItemStack iscellItem)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 63;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requsetedAddition)
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode)
	{
		IAEItemStack local = getCellItems().findPrecise( input );
		if ( local == null )
			return input;

		return null;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode)
	{
		IAEItemStack local = getCellItems().findPrecise( request );
		if ( local == null )
			return null;

		return request.copy();
	}

	@Override
	ItemList<IAEItemStack> getCellItems()
	{
		if ( itemListCache != null )
			return itemListCache;

		ItemList list = new ItemList();
		CellInventoryHandler handler = new CellInventoryHandler( this );

		for (IAEItemStack is : handler.myPartitionList.getItems())
		{
			IAEItemStack b = is.copy();
			b.setStackSize( Integer.MAX_VALUE );
			list.add( b );
		}

		return itemListCache = list;
	}

}
