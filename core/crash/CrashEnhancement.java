package appeng.core.crash;

import appeng.core.AEConfig;
import appeng.integration.IntegrationRegistry;
import cpw.mods.fml.common.ICrashCallable;

public class CrashEnhancement implements ICrashCallable
{

	final CrashInfo Output;

	final String ModVersion = AEConfig.CHANNEL + " " + AEConfig.VERSION + " for Forge " + // WHAT?
			net.minecraftforge.common.ForgeVersion.majorVersion + "." // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + "." // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + "." // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion;

	final String IntegrationInfo;

	public CrashEnhancement(CrashInfo ci) {
		Output = ci;

		if ( IntegrationRegistry.instance != null )
			IntegrationInfo = IntegrationRegistry.instance.getStatus();
		else
			IntegrationInfo = "N/A";
	}

	@Override
	public String call() throws Exception
	{
		switch (Output)
		{
		case MOD_VERSION:
			return ModVersion;
		case INTEGRATION:
			return IntegrationInfo;
		}

		return "UNKNOWN_VALUE";
	}

	@Override
	public String getLabel()
	{
		switch (Output)
		{
		case MOD_VERSION:
			return "AE2 Version";
		case INTEGRATION:
			return "AE2 Integration";
		}

		return "AE2_UNKNOWN";
	}

}
