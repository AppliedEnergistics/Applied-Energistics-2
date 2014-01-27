package appeng.helpers;

import net.minecraft.tileentity.TileEntity;
import appeng.api.networking.security.IActionHost;

public interface IInterfaceHost extends IActionHost
{

	DualityInterface getInterfaceDuality();

	TileEntity getTileEntity();
}
