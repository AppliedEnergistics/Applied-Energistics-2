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


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.electronwill.nightconfig.core.Config;

import appeng.core.AELog;


/**
 * Initializes a map of ID to UUID from the player list in the settings.cfg
 */
class PlayerMappingsInitializer
{
	/**
	 * Internal immutable mapping
	 */
	// FIXME private final Map<Integer, UUID> playerMappings;

	/**
	 * Creates the initializer for the player mappings.
	 * The map will be filled upon construction
	 * and will only be filled with valid entries.
	 * If an invalid entry is found, an warning is printed,
	 * mostly due to migration problems from 1.7.2 to 1.7.10
	 * where the UUIDs were introduced.
	 *
	 * @param playerList the category for the player list, generally extracted using the "players" tag
	 */
	PlayerMappingsInitializer(final Config config, String prefix )
	{
// FIXME		// Matcher for UUIDs
// FIXME		final UUIDMatcher matcher = new UUIDMatcher();
// FIXME
// FIXME		// Initial capacity for mappings
// FIXME		final int capacity = playerList.size();
// FIXME
// FIXME		// Mappings for the IDs is a regular HashMap
// FIXME		this.playerMappings = new HashMap<>( capacity );
// FIXME
// FIXME		// Iterates through every pair of UUID to ID
// FIXME		for( final Map.Entry<String, Property> entry : playerList.getValues().entrySet() )
// FIXME		{
// FIXME			final String maybeUUID = entry.getKey();
// FIXME			final int id = entry.getValue().getInt();
// FIXME
// FIXME			if( matcher.isUUID( maybeUUID ) )
// FIXME			{
// FIXME				final UUID uuidString = UUID.fromString( maybeUUID );
// FIXME
// FIXME				this.playerMappings.put( id, uuidString );
// FIXME			}
// FIXME			else
// FIXME			{
// FIXME				AELog.warn(
// FIXME						"The configuration for players contained an outdated entry instead an expected UUID " + maybeUUID + " for the player " + id + ". Please clean this up." );
// FIXME			}
// FIXME		}
	}

	/**
	 * Getter
	 *
	 * @return Immutable map of the players mappings of their ID to their UUID
	 */
	public Map<Integer, UUID> getPlayerMappings()
	{
		return this.playerMappings;
	}
}
