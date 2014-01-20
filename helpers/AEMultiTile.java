package appeng.helpers;

import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.Optional.Interface;

@Interface(iface = "powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet", modid = "MineFactoryReloaded")
public interface AEMultiTile extends IGridHost, IPartHost, IConnectableRedNet
{

}
