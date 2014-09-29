package appeng.items.contents;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.me.storage.CellInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

public class PortableCellViewer extends MEMonitorHandler<IAEItemStack> implements IPortableCell
{

	private final ItemStack target;
	private final IAEItemPowerStorage ips;

	public PortableCellViewer(ItemStack is) {
		super( CellInventory.getCell( is, null ) );
		ips = (IAEItemPowerStorage) is.getItem();
		target = is;
	}

	@Override
	public ItemStack getItemStack()
	{
		return target;
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier)
	{
		amt = usePowerMultiplier.multiply( amt );

		if ( mode == Actionable.SIMULATE )
			return usePowerMultiplier.divide( Math.min( amt, ips.getAECurrentPower( getItemStack() ) ) );

		return usePowerMultiplier.divide( ips.extractAEPower( getItemStack(), amt ) );
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		return null;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		final ConfigManager out = new ConfigManager( new IConfigManagerHost() {

			@Override
			public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue)
			{
				NBTTagCompound data = Platform.openNbtData( target );
				manager.writeToNBT( data );
			}
		} );

		out.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		out.registerSetting( Settings.VIEW_MODE, ViewItems.ALL );
		out.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		out.readFromNBT( (NBTTagCompound) Platform.openNbtData( target ).copy() );
		return out;
	}

}
