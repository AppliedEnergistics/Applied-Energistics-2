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

package appeng.integration.modules;


import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.TunnelType;
import appeng.api.parts.IPartHelper;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;


public final class RF implements IIntegrationModule
{
	@Reflected
	public static RF instance;

	@Reflected
	public RF()
	{
		IntegrationHelper.testClassExistence( this, cofh.api.energy.IEnergyReceiver.class );
		IntegrationHelper.testClassExistence( this, cofh.api.energy.IEnergyProvider.class );
		IntegrationHelper.testClassExistence( this, cofh.api.energy.IEnergyHandler.class );
		IntegrationHelper.testClassExistence( this, cofh.api.energy.IEnergyConnection.class );
	}

	@Override
	public void init()
	{
		final IAppEngApi api = AEApi.instance();
		final IPartHelper partHelper = api.partHelper();

		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.RF ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergyHandler", "cofh.api.energy.IEnergyReceiver" );
		}
	}

	@Override
	public void postInit()
	{
		this.registerRFAttunement( "ExtraUtilities", "extractor_base", 12 );
		this.registerRFAttunement( "ExtraUtilities", "pipes", 11 );
		this.registerRFAttunement( "ExtraUtilities", "pipes", 14 );
		this.registerRFAttunement( "ExtraUtilities", "generator", OreDictionary.WILDCARD_VALUE );

		this.registerRFAttunement( "ThermalExpansion", "Cell", OreDictionary.WILDCARD_VALUE );
		this.registerRFAttunement( "ThermalExpansion", "Dynamo", OreDictionary.WILDCARD_VALUE );

		// Fluxduct
		this.registerRFAttunement( "ThermalDynamics", "ThermalDynamics_0", 0 );

		this.registerRFAttunement( "EnderIO", "itemPowerConduit", OreDictionary.WILDCARD_VALUE );
		this.registerRFAttunement( "EnderIO", "blockCapacitorBank", 0 );
		this.registerRFAttunement( "EnderIO", "blockPowerMonitor", 0 );
	}

	private void registerRFAttunement( final String mod, final String name, final int dmg )
	{
		assert mod != null;
		assert !mod.isEmpty();
		assert name != null;
		assert !name.isEmpty();
		assert dmg >= 0;

		final ItemStack modItem = GameRegistry.findItemStack( mod, name, 1 );
		if( modItem != null )
		{
			modItem.setItemDamage( dmg );
			AEApi.instance().registries().p2pTunnel().addNewAttunement( modItem, TunnelType.RF_POWER );
		}
	}
}
