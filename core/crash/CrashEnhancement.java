package appeng.core.crash;

import appeng.core.AEConfig;
import appeng.integration.IntegrationRegistry;
import cpw.mods.fml.common.ICrashCallable;

public class CrashEnhancement implements ICrashCallable
{

	final CrashInfo Output;

	public CrashEnhancement(CrashInfo ci) {
		Output = ci;
	}

	@Override
	public String call() throws Exception
	{
		switch (Output)
		{
		case MOD_VERSION:
			return AEConfig.CHANNEL + " " + AEConfig.VERSION + " for Forge "
					+ net.minecraftforge.common.ForgeVersion.majorVersion + "." // majorVersion
					+ net.minecraftforge.common.ForgeVersion.minorVersion + "." // minorVersion
					+ net.minecraftforge.common.ForgeVersion.revisionVersion + "." // revisionVersion
					+ net.minecraftforge.common.ForgeVersion.buildVersion;
		case INTEGRATION:
			if ( IntegrationRegistry.instance == null )
				return "N/A";
			return IntegrationRegistry.instance.getStatus();
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
