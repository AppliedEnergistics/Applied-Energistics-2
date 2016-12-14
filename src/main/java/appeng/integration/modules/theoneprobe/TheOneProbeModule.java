/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.theoneprobe;


import com.google.common.base.Function;

import net.minecraftforge.fml.common.event.FMLInterModComms;

import mcjty.theoneprobe.api.ITheOneProbe;

import appeng.integration.IIntegrationModule;
import appeng.integration.modules.theoneprobe.config.AEConfigProvider;


public class TheOneProbeModule implements IIntegrationModule, Function<ITheOneProbe, Void>
{
	@Override
	public void preInit() throws Throwable
	{
		FMLInterModComms.sendFunctionMessage( "theoneprobe", "getTheOneProbe", this.getClass().getName() );
	}

	@Override
	public Void apply( ITheOneProbe input )
	{
		input.registerProbeConfigProvider( new AEConfigProvider() );

		input.registerProvider( new TileInfoProvider() );

		input.registerProvider( new PartInfoProvider() );

		return null;
	}
}
