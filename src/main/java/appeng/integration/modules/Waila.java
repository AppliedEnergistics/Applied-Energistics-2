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

package appeng.integration.modules;


import net.minecraftforge.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import appeng.integration.BaseModule;
import appeng.integration.modules.waila.PartWailaDataProvider;
import appeng.integration.modules.waila.TileWailaDataProvider;
import appeng.tile.AEBaseTile;


public class Waila extends BaseModule
{
	public static Waila instance;

	public static void register( IWailaRegistrar registrar )
	{
		final IWailaDataProvider partHost = new PartWailaDataProvider();

		registrar.registerStackProvider( partHost, AEBaseTile.class );
		registrar.registerBodyProvider( partHost, AEBaseTile.class );
		registrar.registerNBTProvider( partHost, AEBaseTile.class );

		final IWailaDataProvider tile = new TileWailaDataProvider();

		registrar.registerBodyProvider( tile, AEBaseTile.class );
		registrar.registerNBTProvider( tile, AEBaseTile.class );
	}

	@Override
	public void init() throws Throwable
	{
		this.testClassExistence( IWailaDataProvider.class );
		this.testClassExistence( IWailaRegistrar.class );

		FMLInterModComms.sendMessage( "Waila", "register", this.getClass().getName() + ".register" );
	}

	@Override
	public void postInit()
	{
		// :P
	}
}
