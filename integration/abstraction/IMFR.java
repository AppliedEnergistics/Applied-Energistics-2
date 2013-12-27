package appeng.integration.abstraction;

import net.minecraft.tileentity.TileEntity;
import appeng.api.storage.IMEInventory;

public interface IMFR
{

	IMEInventory getDSU(TileEntity te);

	boolean isDSU(TileEntity te);

}
