package appeng.util;


import appeng.api.AEApi;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;


public class NBTHelper
{
	public static final int DEFAULT_LIST_TYPE = 0;

	public static CompoundNBT writeItem( IAEItemStack item )
	{
		final CompoundNBT out = new CompoundNBT();

		if( item != null )
		{
			item.write( out );
		}

		return out;
	}

	public static ListNBT writeList( final IItemList<IAEItemStack> itemList) {
		final ListNBT out = new ListNBT();

		for( final IAEItemStack ais : itemList )
		{
			out.add( NBTHelper.writeItem( ais ) );
		}

		return out;
	}

	public static IItemList<IAEItemStack> readList( final ListNBT nbtList) {
		final IItemList<IAEItemStack> out = AEApi.instance().storage().getStorageChannel( IItemStorageChannel.class ).createList();

		if( nbtList == null )
		{
			return out;
		}

		for( int x = 0; x < nbtList.size(); x++ )
		{
			final IAEItemStack ais = AEItemStack.fromNBT( nbtList.getCompound( x ) );
			if( ais != null )
			{
				out.add( ais );
			}
		}

		return out;
	}
}
