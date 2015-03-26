package appeng.core.crash;


import appeng.integration.IntegrationRegistry;


public class IntegrationCrashEnhancement extends BaseCrashEnhancement
{
	public IntegrationCrashEnhancement()
	{
		super( "AE2 Integration", IntegrationRegistry.INSTANCE.getStatus() );
	}
}
