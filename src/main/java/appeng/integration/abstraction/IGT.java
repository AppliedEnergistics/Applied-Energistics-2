package appeng.integration.abstraction;

import net.minecraft.tileentity.TileEntity;
import appeng.api.storage.IMEInventory;

public interface IGT
{

	boolean isQuantumChest(TileEntity te);

	IMEInventory getQuantumChest(TileEntity te);

}
