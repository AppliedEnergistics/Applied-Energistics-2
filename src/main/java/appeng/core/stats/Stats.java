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

package appeng.core.stats;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.ChatComponentTranslation;


public enum Stats
{

	// done
	ItemsInserted,

	// done
	ItemsExtracted,

	// done
	TurnedCranks;

	private StatBasic stat;

	Stats()
	{
	}

	public void addToPlayer( final EntityPlayer player, final int howMany )
	{
		player.addStat( this.getStat(), howMany );
	}

	StatBasic getStat()
	{
		if( this.stat == null )
		{
			this.stat = new StatBasic( "stat.ae2." + this.name(), new ChatComponentTranslation( "stat.ae2." + this.name() ) );
			this.stat.registerStat();
		}

		return this.stat;
	}

}
