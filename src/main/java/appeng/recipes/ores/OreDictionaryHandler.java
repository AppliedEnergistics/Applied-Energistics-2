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

package appeng.recipes.ores;


import appeng.core.AELog;
import appeng.recipes.game.IRecipeBakeable;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;


public class OreDictionaryHandler
{

	public static final OreDictionaryHandler INSTANCE = new OreDictionaryHandler();

	private final List<IOreListener> oreListeners = new ArrayList<IOreListener>();

	private boolean enableRebaking = false;

	@SubscribeEvent
	public void onOreDictionaryRegister( final OreDictionary.OreRegisterEvent event )
	{
		if( event.Name == null || event.Ore == null )
		{
			return;
		}

		if( this.shouldCare( event.Name ) )
		{
			for( final IOreListener v : this.oreListeners )
			{
				v.oreRegistered( event.Name, event.Ore );
			}
		}

		if( this.enableRebaking )
		{
			this.bakeRecipes();
		}
	}

	/**
	 * Just limit what items are sent to the final listeners, I got sick of strange items showing up...
	 *
	 * @param name name about cared item
	 * @return true if it should care
	 */
	private boolean shouldCare( final String name )
	{
		return true;
	}

	public void bakeRecipes()
	{
		this.enableRebaking = true;

		for( final Object o : CraftingManager.getInstance().getRecipeList() )
		{
			if( o instanceof IRecipeBakeable )
			{
				try
				{
					( (IRecipeBakeable) o ).bake();
				}
				catch( final Throwable e )
				{
					AELog.debug( e );
				}
			}
		}
	}

	/**
	 * Adds a new IOreListener and immediately notifies it of any previous ores, any ores added latter will be added at
	 * that point.
	 *
	 * @param n to be added ore listener
	 */
	public void observe( final IOreListener n )
	{
		this.oreListeners.add( n );

		// notify the listener of any ore already in existence.
		for( final String name : OreDictionary.getOreNames() )
		{
			if( name != null && this.shouldCare( name ) )
			{
				for( final ItemStack item : OreDictionary.getOres( name ) )
				{
					if( item != null )
					{
						n.oreRegistered( name, item );
					}
				}
			}
		}
	}
}
