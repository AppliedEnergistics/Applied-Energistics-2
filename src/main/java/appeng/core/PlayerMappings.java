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

package appeng.core;


import java.util.Map;
import java.util.UUID;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;

import com.google.common.base.Optional;


/**
 * Wrapper class for the player mappings.
 * Will grant access to a pre initialized player map
 * based on the "players" category in the settings.cfg
 */
public class PlayerMappings
{
	/**
	 * View of player mappings, is not immutable,
	 * since it needs to be edited upon runtime,
	 * cause new players can join
	 */
	private final Map<Integer, UUID> mappings;

	public PlayerMappings( ConfigCategory category, FMLRelaunchLog log )
	{
		final PlayerMappingsInitializer init = new PlayerMappingsInitializer( category, log );

		this.mappings = init.getPlayerMappings();
	}

	/**
	 * Tries to retrieve the UUID of a player.
	 * Might not be stored inside of the map.
	 * Should not happen though.
	 *
	 * @param id ID of the to be searched player
	 *
	 * @return maybe the UUID of the searched player
	 */
	public Optional<UUID> get( int id )
	{
		final UUID maybe = this.mappings.get( id );

		return Optional.fromNullable( maybe );
	}

	/**
	 * Put in new players when they join the server
	 *
	 * @param id   id of new player
	 * @param uuid UUID of new player
	 */
	public void put( int id, UUID uuid )
	{
		this.mappings.put( id, uuid );
	}
}
