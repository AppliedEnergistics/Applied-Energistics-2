package appeng.integration.modules;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IMJ6;
import appeng.transformer.annotations.integration.Method;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.IBatteryProvider;
import buildcraft.api.mj.ISidedBatteryProvider;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public class MJ6 extends BaseModule implements IMJ6
{

	public static MJ6 instance;

	public MJ6() {
		TestClass( IBatteryObject.class );
		TestClass( IBatteryProvider.class );
		TestClass( ISidedBatteryProvider.class );
	}

	@Override
	public void Init() throws Throwable
	{
	}

	@Override
	public void PostInit() throws Throwable
	{
	}

	@Override
	@Method(iname = "MJ5")
	public IBatteryObject provider(final TileEntity te, final ForgeDirection side)
	{
		if ( te instanceof IPowerReceptor )
		{
			final IPowerReceptor receptor = (IPowerReceptor) te;
			final PowerReceiver ph = receptor.getPowerReceiver( side );

			if ( ph == null )
				return null;

			return new IBatteryObject() {

				@Override
				public void setEnergyStored(double mj)
				{

				}

				@Override
				public IBatteryObject reconfigure(double maxCapacity, double maxReceivedPerCycle, double minimumConsumption)
				{
					return this;
				}

				@Override
				public double minimumConsumption()
				{
					return ph.getMinEnergyReceived();
				}

				@Override
				public double maxReceivedPerCycle()
				{
					return ph.getMaxEnergyReceived();
				}

				@Override
				public double maxCapacity()
				{
					return ph.getMaxEnergyStored();
				}

				@Override
				public String kind()
				{
					return MjAPI.DEFAULT_POWER_FRAMEWORK;
				}

				@Override
				public double getEnergyStored()
				{
					return ph.getEnergyStored();
				}

				@Override
				public double getEnergyRequested()
				{
					return ph.getMaxEnergyStored() - ph.getEnergyStored();
				}

				@Override
				public double addEnergy(double mj, boolean ignoreCycleLimit)
				{
					return ph.receiveEnergy( Type.PIPE, mj, side );
				}

				@Override
				public double addEnergy(double mj)
				{
					return ph.receiveEnergy( Type.PIPE, mj, side );
				}
			};
		}
		return null;
	}
}
