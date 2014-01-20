package appeng.items.storage;

import net.minecraft.item.ItemStack;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;

public class CellConfig extends AppEngInternalInventory
{

	final ItemStack is;

	public CellConfig(ItemStack is) {
		super( null, 63 );
		this.is = is;
		readFromNBT( Platform.openNbtData( is ), "list" );
	}

	@Override
	public void onInventoryChanged()
	{
		writeToNBT( Platform.openNbtData( is ), "list" );
	}

}