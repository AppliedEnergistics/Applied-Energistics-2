package appeng.helpers;

import net.minecraft.tileentity.TileEntity;
import appeng.api.networking.IGridHost;

public interface IInterfaceHost extends IGridHost
{

	DualityInterface getInterfaceDuality();

	TileEntity getTileEntity();
}
