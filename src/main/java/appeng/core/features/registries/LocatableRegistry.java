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

package appeng.core.features.registries;

import java.util.HashMap;

import net.minecraftforge.common.MinecraftForge;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.features.ILocatable;
import appeng.api.features.ILocatableRegistry;
import appeng.util.Platform;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class LocatableRegistry implements ILocatableRegistry
{

	private final HashMap<Long, ILocatable> set;

	@SubscribeEvent
	public void updateLocatable(LocatableEventAnnounce e)
	{
		if ( Platform.isClient() )
			return; // IGNORE!

		if ( e.change == LocatableEvent.Register )
		{
			set.put( e.target.getLocatableSerial(), e.target );
		}
		else if ( e.change == LocatableEvent.Unregister )
		{
			set.remove( e.target.getLocatableSerial() );
		}
	}

	public LocatableRegistry() {
		set = new HashMap<Long, ILocatable>();
		MinecraftForge.EVENT_BUS.register( this );
	}

	/**
	 * Find a locate-able object by its serial.
	 */
	@Override
	public Object findLocatableBySerial(long ser)
	{
		return set.get( ser );
	}

}
