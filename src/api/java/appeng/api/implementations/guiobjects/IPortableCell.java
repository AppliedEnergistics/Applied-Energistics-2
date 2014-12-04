package appeng.api.implementations.guiobjects;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;

/**
 * Obtained via {@link IGuiItem} getGuiObject
 */
public interface IPortableCell extends ITerminalHost, IMEMonitor<IAEItemStack>, IEnergySource, IGuiItemObject
{

}
