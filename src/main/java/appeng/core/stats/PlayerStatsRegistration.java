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


import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import cpw.mods.fml.common.eventhandler.EventBus;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

import java.util.ArrayList;


/**
 * Registers any items a player is picking up or is crafting.
 * Registered items are added to the player stats.
 * This will only happen if the {@link AEFeature#Achievements} feature is enabled.
 */
public class PlayerStatsRegistration
{
	/**
	 * {@link cpw.mods.fml.common.eventhandler.EventBus} to which the handlers might get posted to depending if the
	 * feature is enabled
	 */
	private final EventBus bus;

	/**
	 * is true if the {@link appeng.core.features.AEFeature#Achievements} is enabled in the
	 */
	private final boolean isAchievementFeatureEnabled;

	/**
	 * Constructs this with an {@link cpw.mods.fml.common.eventhandler.EventBus} and {@link appeng.core.AEConfig}.
	 *
	 * @param bus    {@see #bus}
	 * @param config {@link appeng.core.AEConfig} which is used to determine if the
	 *               {@link appeng.core.features.AEFeature#Achievements} is enabled
	 */
	public PlayerStatsRegistration( final EventBus bus, final AEConfig config )
	{
		this.bus = bus;
		this.isAchievementFeatureEnabled = config.isFeatureEnabled( AEFeature.Achievements );
	}

	/**
	 * Registers the {@link appeng.core.stats.AchievementCraftingHandler} and
	 * {@link appeng.core.stats.AchievementPickupHandler} to the {@link #bus} if {@link #isAchievementFeatureEnabled} is
	 * true.
	 */
	public void registerAchievementHandlers()
	{
		if( this.isAchievementFeatureEnabled )
		{
			final PlayerDifferentiator differentiator = new PlayerDifferentiator();
			final AchievementCraftingHandler craftingHandler = new AchievementCraftingHandler( differentiator );
			final AchievementPickupHandler pickupHandler = new AchievementPickupHandler( differentiator );

			this.bus.register( craftingHandler );
			this.bus.register( pickupHandler );
		}
	}

	/**
	 * Registers the {@link appeng.core.stats.AchievementHierarchy} and adds all {@link appeng.core.stats.Achievements}
	 * to a new {@link net.minecraftforge.common.AchievementPage}.
	 */
	public void registerAchievements()
	{
		if( this.isAchievementFeatureEnabled )
		{
			final AchievementHierarchy hierarchy = new AchievementHierarchy();
			hierarchy.registerAchievementHierarchy();

			for( final Stats s : Stats.values() )
			{
				s.getStat();
			}

			/**
			 * register
			 */
			final ArrayList<Achievement> list = new ArrayList<Achievement>();

			for( final Achievements a : Achievements.values() )
			{
				final Achievement ach = a.getAchievement();
				if( ach != null )
				{
					list.add( ach );
				}
			}

			final AchievementPage ae2AchievementPage = new AchievementPage( "Applied Energistics 2", list.toArray( new Achievement[list.size()] ) );
			AchievementPage.registerAchievementPage( ae2AchievementPage );
		}
	}
}
