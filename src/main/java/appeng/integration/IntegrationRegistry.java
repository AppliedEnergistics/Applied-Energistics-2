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


import java.util.LinkedList;

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

		this.modules.add( new IntegrationNode( type.dspName, type.modID, type, "appeng.integration.modules." + type.name() ) );
	}

	public void init()
	{
		for ( IntegrationNode node : this.modules )
			node.Call( IntegrationStage.PRE_INIT );

		for ( IntegrationNode node : this.modules )
			node.Call( IntegrationStage.INIT );
	}

	public void postInit()
	{
		for ( IntegrationNode node : this.modules )
			node.Call( IntegrationStage.POST_INIT );
	}

	public String getStatus()
	{
		String out = null;

		for ( IntegrationNode node : this.modules )
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
		for ( IntegrationNode node : this.modules )
		{
			if ( node.shortName == name )
				return node.isActive();
		}
		return false;
	}

	public Object getInstance( IntegrationType name )
	{
		for ( IntegrationNode node : this.modules )
		{
			if ( node.shortName == name && node.isActive() )
			{
				return node.instance;
			}
		}
		throw new RuntimeException( "integration with " + name.name() + " is disabled." );
	}

}
