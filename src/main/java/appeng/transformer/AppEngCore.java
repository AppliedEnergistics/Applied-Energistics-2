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

package appeng.transformer;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;

import appeng.core.AEConfig;


public final class AppEngCore extends DummyModContainer
{
	private final ModMetadata metadata = new ModMetadata();

	public AppEngCore()
	{
		FMLRelaunchLog.info( "[AppEng] Core Init" );
		this.metadata.autogenerated = false;
		this.metadata.authorList.add( "AlgorithmX2" );
		this.metadata.credits = "AlgorithmX2";
		this.metadata.modId = this.getModId();
		this.metadata.version = this.getVersion();
		this.metadata.name = this.getName();
		this.metadata.url = "http://ae2.ae-mod.info";
		this.metadata.logoFile = "assets/appliedenergistics2/meta/logo.png";
		this.metadata.description = "Embedded Coremod for Applied Energistics 2";
	}

	@EventHandler
	public void load( final FMLInitializationEvent event )
	{
	}


	@Override
	public ModMetadata getMetadata()
	{
		return this.metadata;
	}

	@Override
	public String getModId()
	{
		return "appliedenergistics2-core";
	}

	@Override
	public String getName()
	{
		return "Applied Energistics 2 Core";
	}

	@Override
	public String getVersion()
	{
		return AEConfig.VERSION;
	}

	@Override
	public boolean registerBus( final EventBus bus, final LoadController controller )
	{
		return true;
	}

	@Override
	public String getDisplayVersion()
	{
		return this.getVersion();
	}

}
