package appeng.tile.powersink;

import ic2.api.energy.tile.IEnergySink;

import java.util.EnumSet;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.config.PowerUnits;
import appeng.core.AppEng;
import appeng.integration.abstraction.IIC2;
import appeng.util.Platform;
import cpw.mods.fml.common.Optional.Interface;

@Interface(modid = "IC2", iface = "ic2.api.energy.tile.IEnergySink")
public abstract class IC2 extends BuildCraft implements IEnergySink
{

	boolean isInIC2 = false;

	@Override
	final public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return internalCanAcceptPower && getPowerSides().contains( direction );
	}

	@Override
	final public double demandedEnergyUnits()
	{
		return getExternalPowerDemand( PowerUnits.EU );
	}

	@Override
	final public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		// just store the excess in the current block, if I return the waste,
		// IC2 will just disintegrate it - Oct 20th 2013
		double overflow = PowerUnits.EU.convertTo( PowerUnits.AE, injectExternalPower( PowerUnits.EU, amount ) );
		internalCurrentPower += overflow;
		return 0; // see above comment.
	}

	@Override
	final public int getMaxSafeInput()
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
		if ( AppEng.instance.isIntegrationEnabled( "IC2" ) )
		{
			IIC2 ic2Integration = (IIC2) AppEng.instance.getIntegration( "IC2" );
			if ( !isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.addToEnergyNet( this );
				isInIC2 = true;
			}
		}
	}

	final private void removeFromENet()
	{
		if ( AppEng.instance.isIntegrationEnabled( "IC2" ) )
		{
			IIC2 ic2Integration = (IIC2) AppEng.instance.getIntegration( "IC2" );
			if ( isInIC2 && Platform.isServer() && ic2Integration != null )
			{
				ic2Integration.removeFromEnergyNet( this );
				isInIC2 = false;
			}
		}
	}

}
