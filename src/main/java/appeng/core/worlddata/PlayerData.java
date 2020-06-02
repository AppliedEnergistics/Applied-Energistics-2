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

package appeng.core.worlddata;


import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.PlayerEntity;

import appeng.core.AppEng;


/**
 * Handles the matching between UUIDs and internal IDs for security systems.
 * This whole system could be replaced by storing directly the UUID,
 * using a lot more traffic though
 *
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
final class PlayerData implements IWorldPlayerData, IOnWorldStartable, IOnWorldStoppable
{
	private static final String LAST_PLAYER_CATEGORY = "Counters";
	private static final String LAST_PLAYER_KEY = "lastPlayer";
	private static final int LAST_PLAYER_DEFAULT = 0;

	private final CommentedFileConfig config;
	// FIXME  private final IWorldPlayerMapping playerMapping;

	private int lastPlayerID;

	public PlayerData( @Nonnull final CommentedFileConfig configFile )
	{
		Preconditions.checkNotNull( configFile );

		this.config = configFile;

		// FIXME  this.playerMapping = new PlayerMapping( config, "players." );
	}

	@Nullable
	@Override
	public PlayerEntity getPlayerFromID( final int playerID )
	{
// FIXME  		final Optional<UUID> maybe = this.playerMapping.get( playerID );
// FIXME
// FIXME  		if( maybe.isPresent() )
// FIXME  		{
// FIXME  			final UUID uuid = maybe.get();
// FIXME  			for( final PlayerEntity player : AppEng.proxy.getPlayers() )
// FIXME  			{
// FIXME  				if( player.getUniqueID().equals( uuid ) )
// FIXME  				{
// FIXME  					return player;
// FIXME  				}
// FIXME  			}
// FIXME  		}

		return null;
	}

	@Override
	public int getPlayerID( @Nonnull final GameProfile profile )
	{
		return -1;
// FIXME		Preconditions.checkNotNull( profile );
// FIXME		Preconditions.checkNotNull( this.config.getCategory( "players" ) );
// FIXME		Preconditions.checkState( profile.isComplete() );
// FIXME
// FIXME		final ConfigCategory players = this.config.getCategory( "players" );
// FIXME		final String uuid = profile.getId().toString();
// FIXME		final Property maybePlayerID = players.get( uuid );
// FIXME
// FIXME		if( maybePlayerID != null && maybePlayerID.isIntValue() )
// FIXME		{
// FIXME			return maybePlayerID.getInt();
// FIXME		}
// FIXME		else
// FIXME		{
// FIXME			final int newPlayerID = this.nextPlayer();
// FIXME			final Property newPlayer = new Property( uuid, String.valueOf( newPlayerID ), Property.Type.INTEGER );
// FIXME			players.put( uuid, newPlayer );
// FIXME			this.playerMapping.put( newPlayerID, profile.getId() ); // add to reverse map
// FIXME			this.config.save();
// FIXME
// FIXME			return newPlayerID;
// FIXME		}
	}

	private int nextPlayer()
	{
// FIXME		final int r = this.lastPlayerID;
// FIXME		this.lastPlayerID++;
// FIXME		this.config.get( LAST_PLAYER_CATEGORY, LAST_PLAYER_KEY, this.lastPlayerID ).set( this.lastPlayerID );
// FIXME		return r;
		return 0;
	}

	@Override
	public void onWorldStart()
	{
		// FIXME this.lastPlayerID = this.config.get( LAST_PLAYER_CATEGORY, LAST_PLAYER_KEY, LAST_PLAYER_DEFAULT ).getInt( LAST_PLAYER_DEFAULT );

		this.config.save();
	}

	@Override
	public void onWorldStop()
	{
		this.config.save();

		this.lastPlayerID = 0;
	}
}
