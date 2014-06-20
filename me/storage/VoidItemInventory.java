package appeng.me.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.tile.misc.TileCondenser;

public class VoidItemInventory implements IMEInventoryHandler<IAEItemStack>
{

	TileCondenser target;

	public VoidItemInventory(TileCondenser te) {
		target = te;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		if ( input != null )
			target.addPower( input.getStackSize() );
		return null;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		return out;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public boolean isPrioritized(IAEItemStack input)
	{
		return false;
	}

	@Override
	public boolean canAccept(IAEItemStack input)
	{
		return true;
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
		return i == 2;
	}

}
