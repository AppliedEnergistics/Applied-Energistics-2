package mekanism.api.gas;

import java.util.HashMap;

import net.minecraftforge.fluids.FluidContainerRegistry;
import buildcraft.api.fuels.IronEngineFuel;
import buildcraft.api.fuels.IronEngineFuel.Fuel;
import cpw.mods.fml.common.ModAPIManager;

public class FuelHandler
{

	public static HashMap<String, FuelGas> fuels = new HashMap<String, FuelGas>();

	public static void addGas(Gas gas, int burnTicks, double energyPerMilliBucket)
	{
		fuels.put( gas.getName(), new FuelGas( burnTicks, energyPerMilliBucket ) );
	}

	public static FuelGas getFuel(Gas gas)
	{
		if ( fuels.containsKey( gas.getName() ) )
		{
			return fuels.get( gas.getName() );
		}

		if ( ModAPIManager.INSTANCE.hasAPI( "BuildCraftAPI|fuel" ) && gas.hasFluid() )
		{
			Fuel bcFuel = IronEngineFuel.getFuelForFluid( gas.getFluid() );

			if ( bcFuel != null )
			{
				FuelGas fuel = new FuelGas( bcFuel );
				fuels.put( gas.getName(), fuel );

				return fuel;
			}
		}

		return null;
	}

	public static class FuelGas
	{

		public int burnTicks;
		public double energyPerTick;

		public FuelGas(int duration, double energyDensity) {
			burnTicks = duration;
			energyPerTick = energyDensity / duration;
		}

		public FuelGas(Fuel bcFuel) {
			burnTicks = bcFuel.totalBurningTime / FluidContainerRegistry.BUCKET_VOLUME;
			energyPerTick = bcFuel.powerPerCycle * 1;
		}
	}
}
