package appeng.tile.powersink;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.tile.AEBaseInvTile;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;

public abstract class AERootPoweredTile extends AEBaseInvTile implements IAEPowerStorage
{

	// values that determin general function, are set by inheriting classes if
	// needed. These should generally remain static.
	protected double internalMaxPower = 10000;
	protected boolean internalCanAcceptPower = true;
	protected boolean internalPublicPowerStorage = false;
	private EnumSet<ForgeDirection> internalPowerSides = EnumSet.allOf( ForgeDirection.class );

	protected AccessRestriction internalPowerFlow = AccessRestriction.READ_WRITE;

	// the current power buffer.
	protected double internalCurrentPower = 0;

	protected void setPowerSides(EnumSet<ForgeDirection> sides)
	{
		internalPowerSides = sides;
		// trigger re-calc!
	}

	protected EnumSet<ForgeDirection> getPowerSides()
	{
		return internalPowerSides.clone();
	}

	private class AEPoweredRootHandler extends AETileEventHandler
	{

		public AEPoweredRootHandler() {
			super( TileEventType.WORLD_NBT );
		}

		@Override
		public void writeToNBT(NBTTagCompound data)
		{
			data.setDouble( "internalCurrentPower", internalCurrentPower );
		}

		@Override
		public void readFromNBT(NBTTagCompound data)
		{
			internalCurrentPower = data.getDouble( "internalCurrentPower" );
		}

	};

	public AERootPoweredTile() {
		addNewHandler( new AEPoweredRootHandler() );
	}

	final protected double getExternalPowerDemand(PowerUnits externalUnit, double maxPowerRequired)
	{
		return PowerUnits.AE.convertTo( externalUnit, Math.max( 0.0, getFunnelPowerDemand( externalUnit.convertTo( PowerUnits.AE, maxPowerRequired ) ) ) );
	}

	protected double getFunnelPowerDemand(double maxRequired)
	{
		return internalMaxPower - internalCurrentPower;
	}

	final public double injectExternalPower(PowerUnits input, double amt)
	{
		return PowerUnits.AE.convertTo( input, funnelPowerIntoStorage( input.convertTo( PowerUnits.AE, amt ), Actionable.MODULATE ) );
	}

	protected double funnelPowerIntoStorage(double AEUnits, Actionable mode)
	{
		return injectAEPower( AEUnits, mode );
	}

	@Override
	final public double injectAEPower(double amt, Actionable mode)
	{
		if ( amt < 0.000001 )
			return 0;

		if ( mode == Actionable.SIMULATE )
		{
			double fakeBattery = internalCurrentPower + amt;

			if ( fakeBattery > internalMaxPower )
				return fakeBattery - internalMaxPower;

			return 0;
		}
		else
		{
			if ( internalCurrentPower < 0.01 && amt > 0.01 )
				PowerEvent( PowerEventType.PROVIDE_POWER );

			internalCurrentPower += amt;
			if ( internalCurrentPower > internalMaxPower )
			{
				amt = internalCurrentPower - internalMaxPower;
				internalCurrentPower = internalMaxPower;
				return amt;
			}

			return 0;
		}
	}

	protected void PowerEvent(PowerEventType x)
	{
		// nothing.
	}

	protected double extractAEPower(double amt, Actionable mode)
	{
		if ( mode == Actionable.SIMULATE )
		{
			if ( internalCurrentPower > amt )
				return amt;
			return internalCurrentPower;
		}

		boolean wasFull = internalCurrentPower >= internalMaxPower - 0.001;
		if ( wasFull && amt > 0.001 )
		{
			PowerEvent( PowerEventType.REQUEST_POWER );
		}

		if ( internalCurrentPower > amt )
		{
			internalCurrentPower -= amt;
			return amt;
		}

		amt = internalCurrentPower;
		internalCurrentPower = 0;
		return amt;
	}

	@Override
	final public double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier)
	{
		return multiplier.divide( extractAEPower( multiplier.multiply( amt ), mode ) );
	}

	@Override
	final public double getAEMaxPower()
	{
		return internalMaxPower;
	}

	@Override
	final public double getAECurrentPower()
	{
		return internalCurrentPower;
	}

	@Override
	final public boolean isAEPublicPowerStorage()
	{
		return internalPublicPowerStorage;
	}

	@Override
	final public AccessRestriction getPowerFlow()
	{
		return internalPowerFlow;
	}
}
