package appeng.tile.powersink;

import ic2.api.energy.tile.IEnergySink;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.core.AppEng;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.IIC2;
import appeng.transformer.annotations.integration.Interface;
import appeng.util.Platform;

@Interface(iname = "IC2", iface = "ic2.api.energy.tile.IEnergySink")
public abstract class IC2 extends MinecraftJoules6 implements IEnergySink
{

	boolean isInIC2 = false;

	@Override
	final public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return internalCanAcceptPower && getPowerSides().contains( direction );
	}

	@Override
	final public double getDemandedEnergy()
	{
		return getExternalPowerDemand( PowerUnits.EU, Double.MAX_VALUE );
	}

	@Override
	final public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage)
	{
		// just store the excess in the current block, if I return the waste,
		// IC2 will just disintegrate it - Oct 20th 2013
		double overflow = PowerUnits.EU.convertTo( PowerUnits.AE, injectExternalPower( PowerUnits.EU, amount ) );
		internalCurrentPower += overflow;
		return 0; // see above comment.
	}

	@Override
	final public int getSinkTier()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		removeFromENet();
	}

	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		removeFromENet();
	}

	@Override
	public void onReady()
	{
		super.onReady();
		addToENet();
	}

	@Override
	protected void setPowerSides(EnumSet<ForgeDirection> sides)
	{
		super.setPowerSides( sides );
		removeFromENet();
		addToENet();
	}

	final private void addToENet()
	{
		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
		{
			IIC2 ic2Integration = (IIC2) AppEng.instance.getIntegration( IntegrationType.IC2 );
			if ( !isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.addToEnergyNet( this );
				isInIC2 = true;
			}
		}
	}

	final private void removeFromENet()
	{
		if ( AppEng.instance.isIntegrationEnabled( IntegrationType.IC2 ) )
		{
			IIC2 ic2Integration = (IIC2) AppEng.instance.getIntegration( IntegrationType.IC2 );
			if ( isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.removeFromEnergyNet( this );
				isInIC2 = false;
			}
		}
	}

}
