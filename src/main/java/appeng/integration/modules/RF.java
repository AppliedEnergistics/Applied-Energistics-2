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


import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;

import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.integration.BaseModule;


public class RF extends BaseModule
{

	public static RF instance;

	public RF()
	{
		this.testClassExistence( cofh.api.energy.IEnergyReceiver.class );
		this.testClassExistence( cofh.api.energy.IEnergyProvider.class );
		this.testClassExistence( cofh.api.energy.IEnergyHandler.class );
		this.testClassExistence( cofh.api.energy.IEnergyConnection.class );
	}

	@Override
	public void preInit()
	{

	}


	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		this.RFStack( "ExtraUtilities", "extractor_base", 12 );
		this.RFStack( "ExtraUtilities", "pipes", 11 );
		this.RFStack( "ExtraUtilities", "pipes", 14 );
		this.RFStack( "ExtraUtilities", "generator", OreDictionary.WILDCARD_VALUE );

		this.RFStack( "ThermalExpansion", "Cell", OreDictionary.WILDCARD_VALUE );
		this.RFStack( "ThermalExpansion", "Dynamo", OreDictionary.WILDCARD_VALUE );

		this.RFStack( "EnderIO", "itemPowerConduit", OreDictionary.WILDCARD_VALUE );
		this.RFStack( "EnderIO", "blockCapacitorBank", 0 );
		this.RFStack( "EnderIO", "blockPowerMonitor", 0 );
	}

	void RFStack( String mod, String name, int dmg )
	{
		ItemStack modItem = GameRegistry.findItemStack( mod, name, 1 );
		if( modItem != null )
		{
			modItem.setItemDamage( dmg );
			AEApi.instance().registries().p2pTunnel().addNewAttunement( modItem, TunnelType.RF_POWER );
		}
	}
}
