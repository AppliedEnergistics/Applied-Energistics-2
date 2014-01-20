package appeng.integration.modules.helpers;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;

public class MFRDSU implements IMEInventory<IAEItemStack>
{

	IDeepStorageUnit dsu;
	TileEntity te;

	public MFRDSU(TileEntity ta) {
		te = ta;
		dsu = (IDeepStorageUnit) ta;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		ItemStack is = dsu.getStoredItemType();
		if ( is != null )
		{
			if ( input.equals( is ) )
			{
				long max = dsu.getMaxStoredCount();
				long additiona = is.stackSize;
				additiona += input.getStackSize();
				if ( additiona > max )
				{
					IAEItemStack overflow = AEItemStack.create( is );
					overflow.setStackSize( (int) (additiona - max) );
					if ( mode == Actionable.MODULATE )
						dsu.setStoredItemCount( (int) max );
					return overflow;
				}
				else
				{
					if ( mode == Actionable.MODULATE )
						dsu.setStoredItemCount( is.stackSize + (int) input.getStackSize() );
					return null;
				}
			}
		}
		else
		{
			if ( input.getTagCompound() != null )
				return input;
			if ( mode == Actionable.MODULATE )
				dsu.setStoredItemType( input.getItemStack(), (int) input.getStackSize() );
			return null;
		}
		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		ItemStack is = dsu.getStoredItemType();
		if ( request.equals( is ) )
		{
			if ( request.getStackSize() >= is.stackSize )
			{
				if ( mode == Actionable.MODULATE )
					dsu.setStoredItemCount( 0 );
				return AEItemStack.create( is );
			}
			else
			{
				if ( mode == Actionable.MODULATE )
					dsu.setStoredItemCount( is.stackSize - (int) request.getStackSize() );
				return request.copy();
			}
		}
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		ItemStack is = dsu.getStoredItemType();
		if ( is != null )
		{
			out.add( AEItemStack.create( is ) );
		}
		return out;
	}

}
