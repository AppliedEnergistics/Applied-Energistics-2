package appeng.api.parts.layers;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.parts.IPart;
import appeng.api.parts.LayerBase;
import cofh.api.energy.IEnergyHandler;

public class LayerIEnergyHandler extends LayerBase implements IEnergyHandler
{

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).receiveEnergy( from, maxReceive, simulate );

		return 0;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).extractEnergy( from, maxExtract, simulate );

		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).getEnergyStored( from );

		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).getMaxEnergyStored( from );

		return 0;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		IPart part = getPart( from );
		if ( part instanceof IEnergyHandler )
			return ((IEnergyHandler) part).canConnectEnergy( from );

		return false;
	}

}
