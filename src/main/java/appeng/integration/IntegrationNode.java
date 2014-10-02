package appeng.integration;

import java.lang.reflect.Field;

import appeng.api.exceptions.ModNotInstalled;
import appeng.core.AEConfig;
import appeng.core.AELog;
import cpw.mods.fml.common.Loader;

public class IntegrationNode
{

	IntegrationStage state = IntegrationStage.PRE_INIT;
	IntegrationStage failedStage = IntegrationStage.PRE_INIT;
	Throwable exception = null;

	final String displayName;
	final String modID;

	final IntegrationType shortName;
	String name = null;
	Class classValue = null;
	Object instance;
	IIntegrationModule mod = null;

	public IntegrationNode(String displayName, String modID, IntegrationType shortName, String name) {
		this.displayName = displayName;
		this.shortName = shortName;
		this.modID = modID;
		this.name = name;
	}

	@Override
	public String toString()
	{
		return shortName.name() + ":" + state.name();
	}

	void Call(IntegrationStage stage)
	{
		if ( state != IntegrationStage.FAILED )
		{
			if ( state.ordinal() > stage.ordinal() )
				return;

			try
			{
				switch (stage)
				{
				case PRE_INIT:

					boolean enabled = modID == null || Loader.isModLoaded( modID );

					AEConfig.instance
							.addCustomCategoryComment(
									"ModIntegration",
									"Valid Values are 'AUTO', 'ON', or 'OFF' - defaults to 'AUTO' ; Suggested that you leave this alone unless your experiencing an issue, or wish to disable the integration for a reason." );
					String Mode = AEConfig.instance.get( "ModIntegration", displayName.replace( " ", "" ), "AUTO" ).getString();

					if ( Mode.toUpperCase().equals( "ON" ) )
						enabled = true;
					if ( Mode.toUpperCase().equals( "OFF" ) )
						enabled = false;

					if ( enabled )
					{
						classValue = getClass().getClassLoader().loadClass( name );
						mod = (IIntegrationModule) classValue.getConstructor().newInstance();
						Field f = classValue.getField( "instance" );
						f.set( classValue, instance = mod );
					}
					else
						throw new ModNotInstalled( modID );

					state = IntegrationStage.INIT;

					break;
				case INIT:
					mod.Init();
					state = IntegrationStage.POST_INIT;

					break;
				case POST_INIT:
					mod.PostInit();
					state = IntegrationStage.READY;

					break;
				case FAILED:
				default:
					break;
				}
			}
			catch (Throwable t)
			{
				failedStage = stage;
				exception = t;
				state = IntegrationStage.FAILED;
			}
		}

		if ( stage == IntegrationStage.POST_INIT )
		{
			if ( state == IntegrationStage.FAILED )
			{
				AELog.info( displayName + " - Integration Disabled" );
				if ( !(exception instanceof ModNotInstalled) )
					AELog.integration( exception );
			}
			else
			{
				AELog.info( displayName + " - Integration Enable" );
			}
		}
	}

	public boolean isActive()
	{
		if ( state == IntegrationStage.PRE_INIT )
			Call( IntegrationStage.PRE_INIT );

		return state != IntegrationStage.FAILED;
	}

}
