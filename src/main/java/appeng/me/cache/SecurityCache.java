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

package appeng.me.cache;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkSecurityChange;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.ISecurityProvider;
import appeng.core.WorldSettings;
import appeng.me.GridNode;


public class SecurityCache implements ISecurityGrid
{

	public final IGrid myGrid;
	private final List<ISecurityProvider> securityProvider = new ArrayList<ISecurityProvider>();
	private final HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms = new HashMap<Integer, EnumSet<SecurityPermissions>>();
	private long securityKey = -1;

	public SecurityCache( IGrid g )
	{
		this.myGrid = g;
	}

	@MENetworkEventSubscribe
	public void updatePermissions( MENetworkSecurityChange ev )
	{
		this.playerPerms.clear();
		if( this.securityProvider.isEmpty() )
			return;

		this.securityProvider.get( 0 ).readPermissions( this.playerPerms );
	}

	public long getSecurityKey()
	{
		return this.securityKey;
	}

	@Override
	public void onUpdateTick()
	{

	}

	@Override
	public void removeNode( IGridNode gridNode, IGridHost machine )
	{
		if( machine instanceof ISecurityProvider )
		{
			this.securityProvider.remove( machine );
			this.updateSecurityKey();
		}
	}

	private void updateSecurityKey()
	{
		long lastCode = this.securityKey;

		if( this.securityProvider.size() == 1 )
			this.securityKey = this.securityProvider.get( 0 ).getSecurityKey();
		else
			this.securityKey = -1;

		if( lastCode != this.securityKey )
		{
			this.myGrid.postEvent( new MENetworkSecurityChange() );
			for( IGridNode n : this.myGrid.getNodes() )
				( (GridNode) n ).lastSecurityKey = this.securityKey;
		}
	}

	@Override
	public void addNode( IGridNode gridNode, IGridHost machine )
	{
		if( machine instanceof ISecurityProvider )
		{
			this.securityProvider.add( (ISecurityProvider) machine );
			this.updateSecurityKey();
		}
		else
			( (GridNode) gridNode ).lastSecurityKey = this.securityKey;
	}

	@Override
	public void onSplit( IGridStorage destinationStorage )
	{

	}

	@Override
	public void onJoin( IGridStorage sourceStorage )
	{

	}

	@Override
	public void populateGridStorage( IGridStorage destinationStorage )
	{

	}	@Override
	public boolean isAvailable()
	{
		return this.securityProvider.size() == 1 && this.securityProvider.get( 0 ).isSecurityEnabled();
	}



	@Override
	public boolean hasPermission( EntityPlayer player, SecurityPermissions perm )
	{
		return this.hasPermission( player == null ? -1 : WorldSettings.getInstance().getPlayerID( player.getGameProfile() ), perm );
	}

	@Override
	public boolean hasPermission( int playerID, SecurityPermissions perm )
	{
		if( this.isAvailable() )
		{
			EnumSet<SecurityPermissions> perms = this.playerPerms.get( playerID );

			if( perms == null )
			{
				if( playerID == -1 ) // no default?
					return false;
				else
					return this.hasPermission( -1, perm );
			}

			return perms.contains( perm );
		}
		return true;
	}

	@Override
	public int getOwner()
	{
		if( this.isAvailable() )
			return this.securityProvider.get( 0 ).getOwner();
		return -1;
	}
}
