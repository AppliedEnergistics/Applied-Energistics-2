package appeng.integration.modules.helpers;

import appeng.integration.abstraction.helpers.BaseMJPerdition;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public class MJPerdition extends BaseMJPerdition
{

	final protected PowerHandler bcPowerHandler;

	public MJPerdition(IPowerReceptor te) {
		bcPowerHandler = new PowerHandler( te, Type.MACHINE );
	}

	@Override
	public void Tick()
	{
		bcPowerHandler.update();
	}

	@Override
	public void writeToNBT(NBTTagCompound data)
	{
		bcPowerHandler.writeToNBT( data, "bcPowerHandler" );
	}

	@Override
	public void readFromNBT(NBTTagCompound data)
	{
		bcPowerHandler.readFromNBT( data, "bcPowerHandler" );
	}

	@Override
	public PowerReceiver getPowerReceiver()
	{
		return bcPowerHandler.getPowerReceiver();
	}

	@Override
	public double useEnergy(double min, double max, boolean doUse)
	{
		return bcPowerHandler.useEnergy( min, max, doUse );
	}

	@Override
	public void addEnergy(float failed)
	{
		bcPowerHandler.addEnergy( failed );
	}

	@Override
	public void configure(int i, int j, float f, int k)
	{
		bcPowerHandler.configure( i, j, f, k );
	}

}