package appeng.tile.crafting;

import net.minecraft.item.ItemStack;
import appeng.api.AEApi;

public class TileCraftingStorageTile extends TileCraftingTile
{

	static final ItemStack stackStorage4k = AEApi.instance().blocks().blockCraftingStorage4k.stack( 1 );
	static final ItemStack stackStorage16k = AEApi.instance().blocks().blockCraftingStorage16k.stack( 1 );
	static final ItemStack stackStorage64k = AEApi.instance().blocks().blockCraftingStorage64k.stack( 1 );

	@Override
	protected ItemStack getItemFromTile(Object obj)
	{
		int storage = ((TileCraftingTile) obj).getStorageBytes() / 1024;

		if ( storage == 4 )
			return stackStorage4k;
		if ( storage == 16 )
			return stackStorage16k;
		if ( storage == 64 )
			return stackStorage64k;

		return super.getItemFromTile( obj );
	}

	public boolean isAccelerator()
	{
		return false;
	}

	public boolean isStorage()
	{
		return true;
	}

	public int getStorageBytes()
	{
		if ( worldObj == null )
			return 0;

		switch (worldObj.getBlockMetadata( xCoord, yCoord, zCoord ) & 3)
		{
		default:
		case 0:
			return 1 * 1024;
		case 1:
			return 4 * 1024;
		case 2:
			return 16 * 1024;
		case 3:
			return 64 * 1024;
		}
	}
}
