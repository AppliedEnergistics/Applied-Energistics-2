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


import java.util.function.BiFunction;

import net.minecraft.tileentity.TileEntity;

import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.modules.ic2.IC2PowerSink;
import appeng.integration.modules.ic2.IC2PowerSinkAdapter;
import appeng.integration.modules.ic2.IC2PowerSinkStub;
import appeng.tile.powersink.IExternalPowerSink;


public class IC2 implements IIntegrationModule
{

	@Reflected
	public static IC2 instance;

	private BiFunction<TileEntity, IExternalPowerSink, IC2PowerSink> powerSinkFactory = ( ( te, sink ) -> IC2PowerSinkStub.INSTANCE );

	@Override
	public void init() throws Throwable
	{
		powerSinkFactory = IC2PowerSinkAdapter::new;
	}

	@Override
	public void postInit()
	{
	}

	/**
	 * Create an IC2 power sink for the given external sink.
	 */
	public static IC2PowerSink createPowerSink( TileEntity tileEntity, IExternalPowerSink externalSink )
	{
		return instance.powerSinkFactory.apply( tileEntity, externalSink );
	}
}
