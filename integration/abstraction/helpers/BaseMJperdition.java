package appeng.integration.abstraction.helpers;

import net.minecraft.nbt.NBTTagCompound;
import appeng.transformer.annotations.integration.Method;
import buildcraft.api.power.PowerHandler.PowerReceiver;

public abstract class BaseMJperdition
{

	@Method(iname = "MJ5")
	public abstract PowerReceiver getPowerReceiver();

	public abstract double useEnergy(double f, double required, boolean b);

	public abstract void addEnergy(float failed);

	public abstract void configure(int i, int j, float f, int k);

	public abstract void writeToNBT(NBTTagCompound tag);

	public abstract void readFromNBT(NBTTagCompound tag);

	public abstract void Tick();

}
