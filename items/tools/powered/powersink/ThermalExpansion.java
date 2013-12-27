package appeng.items.tools.powered.powersink;

import net.minecraft.item.ItemStack;
import appeng.api.config.PowerUnits;
import cofh.api.energy.IEnergyContainerItem;

public class ThermalExpansion extends IC2 implements IEnergyContainerItem
{

	public ThermalExpansion(Class c, String subname) {
		super( c, subname );
	}

	@Override
	public int receiveEnergy(ItemStack is, int maxReceive, boolean simulate)
	{
		return maxReceive - (int) injectExternalPower( PowerUnits.RF, is, maxReceive, simulate );
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public int getEnergyStored(ItemStack is)
	{
		return (int) PowerUnits.AE.convertTo( PowerUnits.RF, getAECurrentPower( is ) );
	}

	@Override
	public int getMaxEnergyStored(ItemStack is)
	{
		return (int) PowerUnits.AE.convertTo( PowerUnits.EU, getMaxEnergyStored( is ) );
	}

}
