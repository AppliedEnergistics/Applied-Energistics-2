package appeng.helpers;

import net.minecraft.item.ItemStack;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEItemStack;

public interface ICellItemViewer extends IStorageMonitorable, IMEMonitor<IAEItemStack>, IEnergySource
{

	public ItemStack getItemStack();

}
