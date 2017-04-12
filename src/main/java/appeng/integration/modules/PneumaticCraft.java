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


import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.parts.IPartHelper;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;


public class PneumaticCraft implements IIntegrationModule
{
	@Reflected
	public static PneumaticCraft instance;

	private static final String PNEUMATIC_CRAFT_MOD_ID = "PneumaticCraft";

	@Reflected
	public PneumaticCraft()
	{
		IntegrationHelper.testClassExistence( this, pneumaticCraft.api.block.BlockSupplier.class );
		IntegrationHelper.testClassExistence( this, pneumaticCraft.api.tileentity.ISidedPneumaticMachine.class );
		IntegrationHelper.testClassExistence( this, pneumaticCraft.api.tileentity.AirHandlerSupplier.class );
		IntegrationHelper.testClassExistence( this, pneumaticCraft.api.tileentity.IAirHandler.class );
	}

	@Override
	public void init()
	{
		final IAppEngApi api = AEApi.instance();
		final IPartHelper partHelper = api.partHelper();

		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.PneumaticCraft ) )
		{
			partHelper.registerNewLayer( appeng.parts.layers.LayerPressure.class.getName(), pneumaticCraft.api.tileentity.ISidedPneumaticMachine.class.getName() );
		}
	}

	@Override
	public void postInit()
	{
		this.registerPressureAttunement( "pressureTube" );
		this.registerPressureAttunement( "advancedPressureTube" );
	}

	private void registerPressureAttunement( final String itemID )
	{
		final IP2PTunnelRegistry registry = AEApi.instance().registries().p2pTunnel();
		final ItemStack modItem = GameRegistry.findItemStack( PNEUMATIC_CRAFT_MOD_ID, itemID, 1 );

		if( modItem != null )
		{
			modItem.setItemDamage( OreDictionary.WILDCARD_VALUE );
			registry.addNewAttunement( modItem, TunnelType.PRESSURE );
		}
	}
}
