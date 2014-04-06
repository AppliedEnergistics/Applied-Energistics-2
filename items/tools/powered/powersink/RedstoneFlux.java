package appeng.items.tools.powered.powersink;

import net.minecraft.item.ItemStack;
import appeng.api.config.PowerUnits;
import appeng.transformer.annotations.integration.Interface;
import cofh.api.energy.IEnergyContainerItem;

@Interface(iface = "cofh.api.energy.IEnergyContainerItem", iname = "RF")
public class RedstoneFlux extends IC2 implements IEnergyContainerItem
{

	public RedstoneFlux(Class c, String subname) {
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
		return (int) PowerUnits.AE.convertTo( PowerUnits.RF, getAEMaxPower( is ) );
	}

}
