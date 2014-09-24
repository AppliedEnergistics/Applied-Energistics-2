package appeng.core.crash;

import appeng.core.AEConfig;
import appeng.integration.IntegrationRegistry;
import cpw.mods.fml.common.ICrashCallable;

public class CrashEnhancement implements ICrashCallable
{

	private final String name;
	private final String value;

	private final String ModVersion = AEConfig.CHANNEL + " " + AEConfig.VERSION + " for Forge " + // WHAT?
			net.minecraftforge.common.ForgeVersion.majorVersion + "." // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + "." // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + "." // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion;

	public CrashEnhancement(CrashInfo Output) {
		
		if ( Output == CrashInfo.MOD_VERSION )
		{
			name = "AE2 Version";
			value = ModVersion;
		}
		else if ( Output == CrashInfo.INTEGRATION )
		{
			name ="AE2 Integration";
			if ( IntegrationRegistry.instance != null )
				value = IntegrationRegistry.instance.getStatus();
			else
				value = "N/A";
		}
		else
		{
			name = "AE2_UNKNOWN";
			value = "UNKNOWN_VALUE";
		}
	}

	@Override
	public String call() throws Exception
	{
		return value;
	}

	@Override
	public String getLabel()
	{
		return name;
	}

}
