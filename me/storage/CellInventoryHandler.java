package appeng.me.storage;

import net.minecraft.nbt.NBTTagCompound;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;

public class CellInventoryHandler extends MEInventoryHandler<IAEItemStack>
{

	NBTTagCompound openNbtData()
	{
		return Platform.openNbtData( getCellInv().i );
	}

	public CellInventory getCellInv()
	{
		return (CellInventory) (this.internal instanceof CellInventory ? this.internal : null);
	}

	CellInventoryHandler(IMEInventory c) {
		super( c );
	}

}
