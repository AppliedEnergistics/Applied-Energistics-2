package appeng.integration;

import java.util.LinkedList;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

public class IntegrationRegistry
{

	public static IntegrationRegistry instance = null;

	private LinkedList<IntegrationNode> modules = new LinkedList<IntegrationNode>();

	public void add(IntegrationSide side, String dspname, String modID, String name)
	{
		if ( side == IntegrationSide.CLIENT && FMLLaunchHandler.side() == Side.SERVER )
			return;

		if ( side == IntegrationSide.SERVER && FMLLaunchHandler.side() == Side.CLIENT )
			return;

		modules.add( new IntegrationNode( dspname, modID, name, "appeng.integration.modules." + name ) );
	}

	public IntegrationRegistry() {
		instance = this;
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
