package appeng.integration;

import java.lang.reflect.Field;

import appeng.api.exceptions.ModNotInstalled;
import appeng.core.AELog;
import appeng.core.Configuration;
import cpw.mods.fml.common.Loader;

public class IntegrationNode
{

	IntegrationStage state = IntegrationStage.PREINIT;
	IntegrationStage failedStage = IntegrationStage.PREINIT;
	Throwable exception = null;

	String displayName;
	String modID;

	String shortName;
	String name = null;
	Class classValue = null;
	Object instance;
	IIntegrationModule mod = null;

	public IntegrationNode(String dspname, String _modID, String sName, String n) {
		displayName = dspname;
		shortName = sName;
		modID = _modID;
		name = n;
	}

	void Call(IntegrationStage stage)
	{
		if ( isActive() )
		{
			try
			{
				switch (stage)
				{
				case PREINIT:

					boolean enabled = modID == null || Loader.isModLoaded( modID );

					Configuration.instance
							.addCustomCategoryComment(
									"ModIntegration",
									"Valid Values are 'AUTO', 'ON', or 'OFF' - defaults to 'AUTO' ; Suggested that you leave this alone unless your experiencing an issue, or wish to disable the integration for a reason." );
					String Mode = Configuration.instance.get( "ModIntegration", displayName.replace( " ", "" ), "AUTO" )
							.getString();

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

					break;
				case INIT:
					mod.Init();
					break;
				case POSTINIT:
					mod.PostInit();
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

		if ( stage == IntegrationStage.POSTINIT )
		{
			if ( state == IntegrationStage.FAILED )
			{
				AELog.info( displayName + " - Integration Disabled" );
				if ( !(exception instanceof ModNotInstalled) )
					exception.printStackTrace();
			}
			else
			{
				AELog.info( displayName + " - Integration Enable" );
			}
		}
	}

	public boolean isActive()
	{
		return state != IntegrationStage.FAILED;
	}

}
