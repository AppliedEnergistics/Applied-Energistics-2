
package appeng.fluids.helper;


import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.IGridProxyable;


public interface IFluidInterfaceHost extends IActionHost, IGridProxyable
{
	DualityFluidInterface getDualityFluidInterface();

	EnumSet<EnumFacing> getTargets();

	TileEntity getTileEntity();
}
