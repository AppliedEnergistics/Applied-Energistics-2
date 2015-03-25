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


import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.event.FMLInterModComms;

import appeng.api.config.TunnelType;
import appeng.core.api.IIMCProcessor;
import appeng.core.api.imc.IMCBlackListSpatial;
import appeng.core.api.imc.IMCGrinder;
import appeng.core.api.imc.IMCMatterCannon;
import appeng.core.api.imc.IMCP2PAttunement;
import appeng.core.api.imc.IMCSpatial;


/**
 * Handles the delegation of the corresponding IMC messages to the suitable IMC processors
 */
public class IMCHandler
{
	/**
	 * Contains the processors,
	 *
	 * is mutable,
	 * but write access only by the constructor
	 */
	private final Map<String, IIMCProcessor> processors;

	/**
	 * Initializes the processors
	 */
	public IMCHandler()
	{
		this.processors = new HashMap<String, IIMCProcessor>();

		this.processors.put( "blacklist-block-spatial", new IMCBlackListSpatial() );
		this.processors.put( "whitelist-spatial", new IMCSpatial() );
		this.processors.put( "add-grindable", new IMCGrinder() );
		this.processors.put( "add-mattercannon-ammo", new IMCMatterCannon() );

		for( TunnelType type : TunnelType.values() )
		{
			this.processors.put( "add-p2p-attunement-" + type.name().replace( '_', '-' ).toLowerCase(), new IMCP2PAttunement() );
		}
	}

	/**
	 * Tries to find every message matching the internal IMC keys.
	 * When found the corresponding handler will process the attached message.
	 *
	 * @param event Event carrying the identifier and message for the handlers
	 */
	public void handleIMCEvent( FMLInterModComms.IMCEvent event )
	{
		for( FMLInterModComms.IMCMessage message : event.getMessages() )
		{
			final String key = message.key;

			try
			{
				IIMCProcessor handler = this.processors.get( key );
				if( handler != null )
				{
					handler.process( message );
				}
				else
				{
					throw new RuntimeException( "Invalid IMC Called: " + key );
				}
			}
			catch( Throwable t )
			{
				AELog.warning( "Problem detected when processing IMC " + key + " from " + message.getSender() );
				AELog.error( t );
			}
		}
	}
}
