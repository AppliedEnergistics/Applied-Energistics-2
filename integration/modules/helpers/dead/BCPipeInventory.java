package appeng.integration.modules.helpers.dead;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.integration.modules.BC;

public class BCPipeInventory implements IMEInventory<IAEItemStack>
{

	TileEntity te;
	ForgeDirection dir;

	public BCPipeInventory(TileEntity _te, ForgeDirection _dir) {
		te = _te;
		dir = _dir;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		if ( mode == Actionable.SIMULATE )
		{
			if ( BC.instance.canAddItemsToPipe( te, input.getItemStack(), dir ) )
				return null;
			return input;
		}

		if ( BC.instance.addItemsToPipe( te, input.getItemStack(), dir ) )
			return null;
		return input;
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

}
