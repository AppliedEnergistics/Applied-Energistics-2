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


import appeng.util.Platform;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;


/**
 * handles the achievement when an {@link net.minecraft.item.Item} is crafted by a player.
 * The achievement is only added if its a real {@link net.minecraft.entity.player.EntityPlayer}.
 *
 * @author thatsIch
 * @since rv2
 */
public class AchievementCraftingHandler
{
	private final PlayerDifferentiator differentiator;

	public AchievementCraftingHandler( final PlayerDifferentiator differentiator )
	{
		this.differentiator = differentiator;
	}

	@SubscribeEvent
	public void onPlayerCraftingEvent( final PlayerEvent.ItemCraftedEvent event )
	{
		if( this.differentiator.isNoPlayer( event.player ) || event.crafting == null )
		{
			return;
		}

		for( final Achievements achievement : Achievements.values() )
		{
			switch( achievement.getType() )
			{
				case Craft:
					if( Platform.isSameItemPrecise( achievement.getStack(), event.crafting ) )
					{
						achievement.addToPlayer( event.player );
						return;
					}
					break;
				case CraftItem:
					if( achievement.getStack() != null && achievement.getStack().getItem().getClass() == event.crafting.getItem().getClass() )
					{
						achievement.addToPlayer( event.player );
						return;
					}
				default:
			}
		}
	}
}
