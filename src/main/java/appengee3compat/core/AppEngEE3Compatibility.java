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

package appengee3compat.core;


import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.AEConfig;
import appengee3compat.recipes.*;
import com.google.common.base.Stopwatch;
import com.pahimar.ee3.api.exchange.EnergyValueRegistryProxy;
import com.pahimar.ee3.api.knowledge.AbilityRegistryProxy;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import java.util.concurrent.TimeUnit;


@Mod( modid = AppEngEE3Compatibility.MOD_ID, acceptedMinecraftVersions = "[1.7.10]", name = AppEngEE3Compatibility.MOD_NAME, version = AEConfig.VERSION, dependencies = AppEngEE3Compatibility.MOD_DEPENDENCIES ) public final class AppEngEE3Compatibility
{
	public static final String MOD_ID = "appliedenergistics2ee3compatibility";
	public static final String MOD_NAME = "Applied Energistics 2 EE3 Compatibility";
	public static final String MOD_DEPENDENCIES = "after:appliedenergistics2;";

	public static AppEngEE3Compatibility instance;

	public AppEngEE3Compatibility()
	{
		instance = this;
	}

	@Mod.EventHandler void preInit( FMLPreInitializationEvent event )
	{
		if ( Loader.isModLoaded( "EE3" ) )
		{
			Stopwatch watch = Stopwatch.createStarted();
			AELog.info( "Pre Initialization ( started )" );

			final IDefinitions definitions = AEApi.instance().definitions();
			final IMaterials materials = definitions.materials();
			final IItems items = definitions.items();
			final IBlocks blocks = definitions.blocks();

			// Register Base AE Materials
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( blocks.skyStone().maybeBlock().get(), 64 );                         // Set the same as obsidian
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( materials.certusQuartzCrystal().maybeStack( 1 ).get(), 256 );         //
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( materials.certusQuartzCrystalCharged().maybeStack( 1 ).get(), 256 );  //
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( materials.matterBall().maybeStack( 1 ).get(), 256 );
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( materials.singularity().maybeStack( 1 ).get(), 256000 );

			// Non Learnable AE Materials
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( blocks.quartzOre().maybeBlock().get(), 256 );
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( blocks.quartzOreCharged().maybeBlock().get(), 256 );
			EnergyValueRegistryProxy.addPreAssignedEnergyValue( items.cellCreative().maybeStack( 1 ).get(), 1725 );
			AbilityRegistryProxy.setAsNotLearnable( blocks.quartzOre().maybeBlock().get() );
			AbilityRegistryProxy.setAsNotLearnable( blocks.quartzOreCharged().maybeBlock().get() );
			AbilityRegistryProxy.setAsNotLearnable( items.cellCreative().maybeStack( 1 ).get() );

			AELog.info( "Pre Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
		}
	}

	@Mod.EventHandler void postInit( FMLPostInitializationEvent event )
	{
		if ( Loader.isModLoaded( "EE3" ) )
		{
			Stopwatch watch = Stopwatch.createStarted();
			AELog.info( "Post Initialization ( started )" );

			RegisterCrafting.initRecipes();
			RegisterFurnace.initRecipes();
			RegisterFacade.initRecipes();
			RegisterGrinder.initRecipes();
			RegisterInscriber.initRecipes();
			RegisterWorld.initRecipes();

			//RecipeRegistryProxy.dumpRecipeRegistryToLog();

			AELog.info( "Post Initialization ( ended after " + watch.elapsed( TimeUnit.MILLISECONDS ) + "ms )" );
		}
	}
}
