package appeng.integration;

import java.util.LinkedList;

import appeng.util.Platform;

public class IntegrationRegistry
{

	public static IntegrationRegistry instance = null;

	private LinkedList<IntegrationNode> modules = new LinkedList<IntegrationNode>();

	public void loadIntegration(IntegrationSide side, String dspname, String modID, String name)
	{
		if ( side == IntegrationSide.CLIENT && Platform.isServer() )
			return;

		if ( side == IntegrationSide.SERVER && Platform.isClient() )
			return;

		modules.add( new IntegrationNode( dspname, modID, name, "appeng.integration.modules." + name ) );
	}

	private void die()
	{
		throw new RuntimeException( "Invalid Mod Integration Registry config, please check parameters." );
	}

	public IntegrationRegistry(Object[] name) {
		instance = this;

		int stage = 0;

		IntegrationSide side = null;
		String dspName = null;
		String modID = null;
		for (Object n : name)
		{
			stage++;
			if ( stage == 1 )
			{
				if ( n instanceof IntegrationSide )
					side = (IntegrationSide) n;
				else
					die();
			}
			else if ( stage == 2 )
			{
				if ( n instanceof String )
					dspName = (String) n;
				else
					die();
			}
			else if ( stage == 3 )
			{
				if ( n instanceof String || n == null )
					modID = (String) n;
				else
					die();
			}
			else
			{
				if ( n instanceof String )
				{
					loadIntegration( side, dspName, modID, (String) n );
					side = null;
					dspName = null;
					modID = null;
					stage = 0;
				}
				else
					die();
			}
		}

		if ( dspName != null || modID != null )
			die();
	}

	public void init()
	{
		for (IntegrationNode node : modules)
			node.Call( IntegrationStage.PREINIT );

		for (IntegrationNode node : modules)
			node.Call( IntegrationStage.INIT );
	}

	public void postinit()
	{
		for (IntegrationNode node : modules)
			node.Call( IntegrationStage.POSTINIT );
	}

	public String getStatus()
	{
		String out = null;

		for (IntegrationNode node : modules)
		{
			String str = node.shortName + ":" + (node.state == IntegrationStage.FAILED ? "OFF" : "ON");

			if ( out == null )
				out = str;
			else
				out += ", " + str;
		}

		return out;
	}

	public boolean isEnabled(String name)
	{
		for (IntegrationNode node : modules)
		{
			if ( node.shortName.equals( name ) )
				return node.isActive();
		}
		throw new RuntimeException( "invalid integration" );
	}

	public Object getInstance(String name)
	{
		for (IntegrationNode node : modules)
		{
			if ( node.shortName.equals( name ) && node.isActive() )
			{
				return node.instance;
			}
		}
		throw new RuntimeException( "invalid integration" );
	}

}
