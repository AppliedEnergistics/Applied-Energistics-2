package appeng.integration;


import appeng.integration.abstraction.IIC2;
import appeng.integration.abstraction.IInvTweaks;
import appeng.integration.abstraction.IJEI;
import appeng.integration.abstraction.IMekanism;
import appeng.integration.abstraction.IRC;


public final class Integrations
{

	static IIC2 ic2 = new IIC2.Stub();

	static IJEI jei = new IJEI.Stub();

	static IRC rc = new IRC.Stub();

	static IMekanism mekanism = new IMekanism.Stub();

	static IInvTweaks invTweaks = new IInvTweaks.Stub();

	private Integrations()
	{
	}

	public static IIC2 ic2()
	{
		return ic2;
	}

	public static IJEI jei()
	{
		return jei;
	}

	public static IRC rc()
	{
		return rc;
	}

	public static IMekanism mekanism()
	{
		return mekanism;
	}

	public static IInvTweaks invTweaks()
	{
		return invTweaks;
	}
}
