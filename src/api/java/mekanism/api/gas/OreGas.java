package mekanism.api.gas;

import net.minecraft.util.StatCollector;

public class OreGas extends Gas
{
	private String oreName;
	private OreGas cleanGas;

	public OreGas(String s, String name)
	{
		super(s);

		oreName = name;
	}

	public boolean isClean()
	{
		return getCleanGas() == null;
	}

	public OreGas getCleanGas()
	{
		return cleanGas;
	}

	public OreGas setCleanGas(OreGas gas)
	{
		cleanGas = gas;

		return this;
	}

	public String getOreName()
	{
		return StatCollector.translateToLocal(oreName);
	}
}
