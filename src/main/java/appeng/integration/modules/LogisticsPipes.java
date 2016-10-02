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


import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import logisticspipes.api.ILPPipe;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.api.IRequestAPI;
import logisticspipes.api.IRequestAPI.SimulationResult;
import logisticspipes.api.IRoutedPowerProvider;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.storage.IMEInventory;
import appeng.helpers.Reflected;
import appeng.integration.IIntegrationModule;
import appeng.integration.IntegrationHelper;
import appeng.integration.abstraction.ILP;
import appeng.integration.modules.helpers.LPInventory;
import appeng.integration.modules.helpers.LPStorageHandler;


public final class LogisticsPipes implements ILP, IIntegrationModule
{
	@Reflected
	public static LogisticsPipes instance;

	public LogisticsPipes()
	{
		IntegrationHelper.testClassExistence( this, logisticspipes.api.ILPPipe.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.api.ILPPipeTile.class );
		IntegrationHelper.testClassExistence( this, logisticspipes.api.IRequestAPI.class );
	}

	@Override
	public void init() throws Throwable
	{
		AEApi.instance().registries().externalStorage().addInterfaceExternalStorage( new LPStorageHandler() );
	}

	@Override
	public void postInit()
	{
	}

	@Override
	public List<ItemStack> getCraftedItems( final TileEntity te )
	{
		if( isRequestPipe( te ) )
		{
			final IRequestAPI pipe = (IRequestAPI) ( (ILPPipeTile) te ).getLPPipe();
			return pipe.getCraftedItems();
		}

		return new ArrayList<ItemStack>();
	}

	@Override
	public List<ItemStack> getProvidedItems( final TileEntity te )
	{
		if( isRequestPipe( te ) )
		{
			final IRequestAPI pipe = (IRequestAPI) ( (ILPPipeTile) te ).getLPPipe();
			return pipe.getProvidedItems();
		}

		return new ArrayList<ItemStack>();
	}

	@Override
	public boolean isRequestPipe( final TileEntity te )
	{
		return te instanceof ILPPipeTile && ( (ILPPipeTile) te ).getLPPipe() instanceof IRequestAPI;
	}

	@Override
	public List<ItemStack> performRequest( final TileEntity te, final ItemStack wanted, final Actionable mode )
	{
		if( isRequestPipe( te ) )
		{
			final IRequestAPI pipe = (IRequestAPI) ( (ILPPipeTile) te ).getLPPipe();
			if( mode == Actionable.MODULATE )
			{
				return pipe.performRequest( wanted );
			}
			else if( mode == Actionable.SIMULATE )
			{
				final SimulationResult sim = pipe.simulateRequest( wanted );
				return sim.missing;
			}
		}

		return null;
	}

	@Override
	public IMEInventory getInv( final TileEntity te )
	{
		return new LPInventory( te );
	}

	@Override
	public Object getGetPowerPipe( final TileEntity te )
	{
		if( te instanceof ILPPipeTile )
		{
			final ILPPipe pipe = ( (ILPPipeTile) te ).getLPPipe();
			if( pipe instanceof IRoutedPowerProvider )
			{
				return (IRoutedPowerProvider) pipe;
			}
		}
		return null;
	}

	@Override
	public boolean isPowerSource( final TileEntity tt )
	{
		return tt instanceof IRoutedPowerProvider;
	}

	@Override
	public boolean canUseEnergy( final Object pp, final int ceil, final List<Object> providersToIgnore )
	{
		return ( (IRoutedPowerProvider) pp ).canUseEnergy( ceil, providersToIgnore );
	}

	@Override
	public boolean useEnergy( final Object pp, final int ceil, final List<Object> providersToIgnore )
	{
		return ( (IRoutedPowerProvider) pp ).useEnergy( ceil, providersToIgnore );
	}

}
