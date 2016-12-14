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

package appeng.integration;


import appeng.integration.modules.ic2.IC2Module;
import appeng.integration.modules.jei.JEIModule;
import appeng.integration.modules.theoneprobe.TheOneProbeModule;
import appeng.integration.modules.waila.WailaModule;


public enum IntegrationType
{
	IC2( IntegrationSide.BOTH, "Industrial Craft 2", "IC2" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setIc2( new IC2Module() );
		}
	},

	RC( IntegrationSide.BOTH, "Railcraft", "Railcraft" ),

	RF( IntegrationSide.BOTH, "RedstoneFlux Power - Tiles", "CoFHAPI" ),

	RFItem( IntegrationSide.BOTH, "RedstoneFlux Power - Items", "CoFHAPI" ),

	MFR( IntegrationSide.BOTH, "Mine Factory Reloaded", "MineFactoryReloaded" ),

	Waila( IntegrationSide.BOTH, "Waila", "Waila" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return new WailaModule();
		}
	},

	InvTweaks( IntegrationSide.CLIENT, "Inventory Tweaks", "inventorytweaks" ),

	JEI( IntegrationSide.CLIENT, "Just Enough Items", "JEI" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return Integrations.setJei( new JEIModule() );
		}
	},

	Mekanism( IntegrationSide.BOTH, "Mekanism", "Mekanism" ),

	OpenComputers( IntegrationSide.BOTH, "OpenComputers", "OpenComputers" ),

	THE_ONE_PROBE( IntegrationSide.BOTH, "TheOneProbe", "theoneprobe" )
	{
		@Override
		public IIntegrationModule createInstance()
		{
			return new TheOneProbeModule();
		}
	};

	public final IntegrationSide side;
	public final String dspName;
	public final String modID;

	IntegrationType( final IntegrationSide side, final String name, final String modid )
	{
		this.side = side;
		this.dspName = name;
		this.modID = modid;
	}

	public IIntegrationModule createInstance()
	{
		throw new UnsupportedOperationException();
	}

}
