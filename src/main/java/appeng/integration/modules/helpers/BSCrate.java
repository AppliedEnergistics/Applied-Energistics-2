package appeng.integration.modules.helpers;

import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
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

	final ICrateStorage cs;
	final ForgeDirection side;

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

		ItemStack failed = cs.insertItems( input.getItemStack() );
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
			int howMany = cs.getItemCount( request.getItemStack() );
			return howMany > request.getStackSize() ? request : request.copy().setStackSize( howMany );
		}

		ItemStack Obtained = cs.extractItems( request.getItemStack(), (int) request.getStackSize() );
		return AEItemStack.create( Obtained );
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		for (ItemStack is : cs.getContents())
		{
			out.add( AEItemStack.create( is ) );
		}
		return out;
	}

}
