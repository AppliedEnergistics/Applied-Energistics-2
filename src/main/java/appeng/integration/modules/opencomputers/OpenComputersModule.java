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

package appeng.integration.modules.opencomputers;


import li.cil.oc.api.Items;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;



public class OpenComputersModule implements IIntegrationModule
{
	public OpenComputersModule()
	{
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.Items.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.Network.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.Environment.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.SidedEnvironment.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.Node.class );
		IntegrationHelper.testClassExistence( this, li.cil.oc.api.network.Message.class );
	}

	@Override
	public void postInit()
	{
		final IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();

		reg.addNewAttunement( Items.get( "cable" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		reg.addNewAttunement( Items.get( "adapter" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		reg.addNewAttunement( Items.get( "relay" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		reg.addNewAttunement( Items.get( "lancard" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		reg.addNewAttunement( Items.get( "linkedcard" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		reg.addNewAttunement( Items.get( "wlancard" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
		reg.addNewAttunement( Items.get( "analyzer" ).createItemStack( 1 ), TunnelType.COMPUTER_MESSAGE );
	}
}
