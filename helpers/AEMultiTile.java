package appeng.helpers;

import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
import appeng.api.networking.IGridHost;
import appeng.api.parts.IPartHost;
import appeng.transformer.annotations.integration.Interface;

@Interface(iface = "powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet", iname = "MFR")
public interface AEMultiTile extends IGridHost, IPartHost, IConnectableRedNet
{

}
