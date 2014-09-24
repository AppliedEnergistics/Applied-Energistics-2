package appeng.integration.abstraction;

import net.minecraft.tileentity.TileEntity;
import appeng.api.storage.IMEInventory;

public interface IDSU
{

	IMEInventory getDSU(TileEntity te);

	boolean isDSU(TileEntity te);

}
