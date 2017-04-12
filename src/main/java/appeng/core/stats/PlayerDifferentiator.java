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
import net.minecraftforge.common.util.FakePlayer;


/**
 * Can differentiate if a {@link net.minecraft.entity.player.EntityPlayer} is a real player or not
 *
 * @author thatsIch
 * @since rv2
 */
public class PlayerDifferentiator
{
	/**
	 * Can determine if an {@link net.minecraft.entity.player.EntityPlayer} is not a real player.
	 * This is based on if the {@param player} is:
	 * - null
	 * - dead
	 * - fake
	 *
	 * @param player to be checked player
	 * @return true if {@param player} is not a real player
	 */
	boolean isNoPlayer( final EntityPlayer player )
	{
		return player == null || player.isDead || player instanceof FakePlayer;
	}
}
