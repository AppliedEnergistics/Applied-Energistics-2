package appeng.me.storage;

import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.items.contents.CellConfig;
import appeng.util.item.AEItemStack;

public class CreativeCellInventory implements IMEInventoryHandler<IAEItemStack>
{

	final IItemList<IAEItemStack> itemListCache = AEApi.instance().storage().createItemList();

	public static IMEInventoryHandler getCell(ItemStack o)
	{
		try
		{
			return new CellInventoryHandler( new CreativeCellInventory( o ) );
		}
		catch (AppEngException ignored)
		{
		}

		return null;
	}

	protected CreativeCellInventory(ItemStack o) throws AppEngException {
		CellConfig cc = new CellConfig( o );
		for (ItemStack is : cc)
			if ( is != null )
			{
				IAEItemStack i = AEItemStack.create( is );
				i.setStackSize( Integer.MAX_VALUE );
				itemListCache.add( i );
			}
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		IAEItemStack local = itemListCache.findPrecise( input );
		if ( local == null )
			return input;

		return null;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		IAEItemStack local = itemListCache.findPrecise( request );
		if ( local == null )
			return null;

		return request.copy();
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		for (IAEItemStack ais : itemListCache)
			out.add( ais );
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public boolean isPrioritized(IAEItemStack input)
	{
		return itemListCache.findPrecise( input ) != null;
	}

	@Override
	public boolean canAccept(IAEItemStack input)
	{
		return itemListCache.findPrecise( input ) != null;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public boolean validForPass(int i)
	{
		return true;
	}

}
