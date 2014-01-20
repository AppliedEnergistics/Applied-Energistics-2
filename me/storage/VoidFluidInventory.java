package appeng.me.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.tile.misc.TileCondenser;

public class VoidFluidInventory implements IMEInventoryHandler<IAEFluidStack>
{

	TileCondenser target;

	public VoidFluidInventory(TileCondenser te) {
		target = te;
	}

	@Override
	public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src)
	{
		if ( input != null )
			target.addPower( (double) input.getStackSize() / 1000.0 );
		return null;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src)
	{
		return null;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems(IItemList out)
	{
		return out;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.WRITE;
	}

	@Override
	public boolean isPrioritized(IAEFluidStack input)
	{
		return false;
	}

	@Override
	public boolean canAccept(IAEFluidStack input)
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

}
