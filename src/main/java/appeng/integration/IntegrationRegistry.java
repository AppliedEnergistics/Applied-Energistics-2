/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedList;


public enum IntegrationRegistry
{
	INSTANCE;

	private static final String PACKAGE_PREFIX = "appeng.integration.modules.";

	private final Collection<IntegrationNode> modules = new LinkedList<IntegrationNode>();

	public void add( final IntegrationType type )
	{
		if( type.side == IntegrationSide.CLIENT && FMLLaunchHandler.side() == Side.SERVER )
		{
			return;
		}

		if( type.side == IntegrationSide.SERVER && FMLLaunchHandler.side() == Side.CLIENT )
		{
			return;
		}

		this.modules.add( new IntegrationNode( type.dspName, type.modID, type, PACKAGE_PREFIX + type.name() ) );
	}

	public void init()
	{
		for( final IntegrationNode node : this.modules )
		{
			node.call( IntegrationStage.PRE_INIT );
		}

		for( final IntegrationNode node : this.modules )
		{
			node.call( IntegrationStage.INIT );
		}
	}

	public void postInit()
	{
		for( final IntegrationNode node : this.modules )
		{
			node.call( IntegrationStage.POST_INIT );
		}
	}

	public String getStatus()
	{
		final StringBuilder builder = new StringBuilder( this.modules.size() * 3 );

		for( final IntegrationNode node : this.modules )
		{
			if( builder.length() != 0 )
			{
				builder.append( ", " );
			}

			final String integrationState = node.getShortName() + ":" + ( node.getState() == IntegrationStage.FAILED ? "OFF" : "ON" );
			builder.append( integrationState );
		}

		return builder.toString();
	}

	public boolean isEnabled( final IntegrationType name )
	{
		for( final IntegrationNode node : this.modules )
		{
			if( node.getShortName() == name )
			{
				return node.isActive();
			}
		}
		return false;
	}

	@Nonnull
	public Object getInstance( final IntegrationType name )
	{
		for( final IntegrationNode node : this.modules )
		{
			if( node.getShortName() == name && node.isActive() )
			{
				return node.getInstance();
			}
		}

		throw new IllegalStateException( "integration with " + name.name() + " is disabled." );
	}

}
