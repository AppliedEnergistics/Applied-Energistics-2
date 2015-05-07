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

import li.cil.oc.api.Items;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.integration.BaseModule;

public class OpenComputers extends BaseModule
{
	public static OpenComputers instance;

	public OpenComputers()
	{
		this.testClassExistence( li.cil.oc.api.Items.class );
		this.testClassExistence( li.cil.oc.api.Network.class );
		this.testClassExistence( li.cil.oc.api.network.Environment.class );
		this.testClassExistence( li.cil.oc.api.network.SidedEnvironment.class );
		this.testClassExistence( li.cil.oc.api.network.Node.class );
		this.testClassExistence( li.cil.oc.api.network.Message.class );
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		final IP2PTunnelRegistry registry = AEApi.instance().registries().p2pTunnel();

		registry.addNewAttunement( Items.get( "cable" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		registry.addNewAttunement( Items.get( "adapter" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		registry.addNewAttunement( Items.get( "switch" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		registry.addNewAttunement( Items.get( "accessPoint" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		registry.addNewAttunement( Items.get( "lanCard" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		registry.addNewAttunement( Items.get( "linkedCard" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		registry.addNewAttunement( Items.get( "wlanCard" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		registry.addNewAttunement( Items.get( "analyzer" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
	}
}
