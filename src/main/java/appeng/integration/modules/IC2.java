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
import appeng.integration.abstraction.IIC2;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.recipe.RecipeInputItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;


public class IC2 implements IIC2, IIntegrationModule
{
	@Reflected
	public static IC2 instance;

	@Reflected
	public IC2()
	{
		IntegrationHelper.testClassExistence( this, ic2.api.energy.tile.IEnergyTile.class );
		IntegrationHelper.testClassExistence( this, ic2.api.recipe.RecipeInputItemStack.class );
	}

	@Override
	public void init()
	{
		final IAppEngApi api = AEApi.instance();
		final IPartHelper partHelper = api.partHelper();

		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.IC2 ) )
		{
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergySink", "ic2.api.energy.tile.IEnergySink" );
			partHelper.registerNewLayer( "appeng.parts.layers.LayerIEnergySource", "ic2.api.energy.tile.IEnergySource" );
		}
	}

	@Override
	public void postInit()
	{
		final IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( this.getItem( "copperCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "insulatedCopperCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "goldCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "insulatedGoldCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "ironCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "insulatedIronCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "insulatedTinCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "glassFiberCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "tinCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "detectorCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( this.getItem( "splitterCableItem" ), TunnelType.IC2_POWER );

		// this is gone?
		// AEApi.INSTANCE().registries().matterCannon().registerAmmo( getItem( "uraniumDrop" ), 238.0289 );
	}

	@Override
	public void addToEnergyNet( final TileEntity appEngTile )
	{
		MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileLoadEvent( (IEnergyTile) appEngTile ) );
	}

	@Override
	public void removeFromEnergyNet( final TileEntity appEngTile )
	{
		MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileUnloadEvent( (IEnergyTile) appEngTile ) );
	}

	@Override
	public ItemStack getItem( final String name )
	{
		return ic2.api.item.IC2Items.getItem( name );
	}

	@Override
	public void maceratorRecipe( final ItemStack in, final ItemStack out )
	{
		ic2.api.recipe.Recipes.macerator.addRecipe( new RecipeInputItemStack( in, in.stackSize ), null, out );
	}
}
