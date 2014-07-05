package appeng.helpers;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionHost;

public interface IInterfaceHost extends IActionHost, ICraftingProvider, IUpgradeableHost
{

	DualityInterface getInterfaceDuality();

	EnumSet<ForgeDirection> getTargets();

	TileEntity getTileEntity();

	void saveChanges();
}
