package appeng.items.contents;

import net.minecraft.item.ItemStack;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.storage.CellInventory;

public class PortableCellViewer extends MEMonitorHandler<IAEItemStack> implements IPortableCell
{

	private ItemStack target;
	private IAEItemPowerStorage ips;

	public PortableCellViewer(ItemStack is) {
		super( CellInventory.getCell( is ) );
		ips = (IAEItemPowerStorage) is.getItem();
		target = is;
	}

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

}
