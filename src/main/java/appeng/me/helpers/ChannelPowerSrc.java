package appeng.me.helpers;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;

public class ChannelPowerSrc implements IEnergySource
{

	final IGridNode node;
	final IEnergySource realSrc;

	public ChannelPowerSrc(IGridNode networkNode, IEnergySource src) {
		node = networkNode;
		realSrc = src;
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier)
	{
		if ( node.isActive() )
			return realSrc.extractAEPower( amt, mode, usePowerMultiplier );
		return 0.0;
	}

}
