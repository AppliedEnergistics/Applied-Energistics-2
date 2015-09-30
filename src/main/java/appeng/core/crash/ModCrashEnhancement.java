package appeng.core.crash;


import appeng.core.AEConfig;


public class ModCrashEnhancement extends BaseCrashEnhancement
{
	private static final String MOD_VERSION = AEConfig.CHANNEL + ' ' + AEConfig.VERSION + " for Forge " + // WHAT?
			net.minecraftforge.common.ForgeVersion.majorVersion + '.' // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + '.' // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + '.' // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion;

	public ModCrashEnhancement( final CrashInfo output )
	{
		super( "AE2 Version", MOD_VERSION );
	}
}
