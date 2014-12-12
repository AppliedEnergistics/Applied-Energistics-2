/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.integration;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import appeng.api.exceptions.ModNotInstalled;
import appeng.core.AELog;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;


public enum IntegrationRegistry
{
	INSTANCE;

	private final LinkedList<IntegrationNode> modules = new LinkedList<IntegrationNode>();

	public void add( IntegrationType type )
	{
		if ( type.side == IntegrationSide.CLIENT && FMLLaunchHandler.side() == Side.SERVER )
			return;

		if ( type.side == IntegrationSide.SERVER && FMLLaunchHandler.side() == Side.CLIENT )
			return;

		modules.add( new IntegrationNode( type.dspName, type.modID, type, "appeng.integration.modules." + type.name() ) );
	}

	public void init()
	{
		for ( IntegrationNode node : modules )
			node.Call( IntegrationStage.PRE_INIT );

		for ( IntegrationNode node : modules )
			node.Call( IntegrationStage.INIT );
	}

	public void postInit()
	{
		for ( IntegrationNode node : modules )
			node.Call( IntegrationStage.POST_INIT );

		// Log status of all integrations
		// (This should all be display logic - the work was in Call())
		StringBuilder sb;
		List<IntegrationNode> enabled = Lists.newArrayList();
		List<IntegrationNode> disabled = Lists.newArrayList();
		List<IntegrationNode> errored = Lists.newArrayList();

		for ( IntegrationNode node : modules )
		{
			switch (node.state)
			{
			case FAILED:

				Throwable exception = node.exception;
				if ( exception instanceof ModNotInstalled )
					disabled.add( node );
				else
					errored.add( node );
				break;

			case READY:
				enabled.add( node );
				break;
			}
		}

		AELog.info( "=== Applied Energistics Integrations Report ===" );
		AELog.info( "Enabled: %d  Disabled: %d  Errored: %d", enabled.size(), disabled.size(), errored.size() );
		if ( !enabled.isEmpty() )
		{
			AELog.info("Enabled integrations: ");
			for ( String line : joinIntegrationNames( enabled ) )
				AELog.info( line );
		}
		if ( !disabled.isEmpty() )
		{
			AELog.info("Disabled integrations: ");
			for ( String line : joinIntegrationNames( disabled ) )
				AELog.info( line );
		}
		if ( !errored.isEmpty() )
		{
			AELog.warning( "[!] Some integrations failed." );
			for ( IntegrationNode node : errored )
			{
				AELog.warning( "[!] Integration %s failed with %s", node.displayName, node.exception.getClass().getName() );
				AELog.error( node.exception );
			}
		}
		AELog.info("----------- end report");
	}

	// Small helper to not put a semicolon on the end
	private static String[] joinIntegrationNames(List<IntegrationNode> integrations)
	{
		StringBuilder out = new StringBuilder("    ");
		int lastNewline = 0;

		Iterator<IntegrationNode> iter = integrations.iterator();
		while ( iter.hasNext() )
		{
			if ( (out.length() - lastNewline) > 70 )
			{
				lastNewline = out.length();
				out.append( "\n    " );
			}

			out.append( iter.next().displayName );

			if ( iter.hasNext() )
				out.append( "; " );
		}

		return out.toString().split("\n");
	}

	public String getStatus()
	{
		String out = null;

		for ( IntegrationNode node : modules )
		{
			String str = node.shortName + ":" + ( node.state == IntegrationStage.FAILED ? "OFF" : "ON" );

			if ( out == null )
				out = str;
			else
				out += ", " + str;
		}

		return out;
	}

	public boolean isEnabled( IntegrationType name )
	{
		for ( IntegrationNode node : modules )
		{
			if ( node.shortName == name )
				return node.isActive();
		}
		return false;
	}

	public Object getInstance( IntegrationType name )
	{
		for ( IntegrationNode node : modules )
		{
			if ( node.shortName.equals( name ) && node.isActive() )
			{
				return node.instance;
			}
		}
		throw new RuntimeException( "integration with " + name.name() + " is disabled." );
	}

}
