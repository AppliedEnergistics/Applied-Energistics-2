package appeng.integration.modules.helpers.dead;

import net.mcft.copy.betterstorage.api.ICrateStorage;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;

public class BSCrate implements IMEInventory<IAEItemStack>
{

	ICrateStorage cs;
	ForgeDirection side;

	public BSCrate(Object object, ForgeDirection d) {
		cs = (ICrateStorage) object;
		side = d;
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
			return null;

		ItemStack failed = cs.insertItems( side, input.getItemStack() );
		if ( failed == null )
			return null;
		input.setStackSize( failed.stackSize );
		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		if ( mode == Actionable.SIMULATE )
		{
			int howMany = cs.getItemCount( side, request.getItemStack() );
			return howMany > request.getStackSize() ? request : request.copy().setStackSize( howMany );
		}

		ItemStack Obtained = cs.extractItems( side, request.getItemStack() );
		return AEItemStack.create( Obtained );
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		for (ItemStack is : cs.getContents( side ))
		{
			out.add( AEItemStack.create( is ) );
		}
		return out;
	}

}
