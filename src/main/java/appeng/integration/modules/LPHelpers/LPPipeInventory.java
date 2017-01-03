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

package appeng.integration.modules.LPHelpers;


import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.integration.abstraction.ILogisticsPipes;
import appeng.tile.grid.AENetworkInvTile;
import appeng.util.item.AEItemStack;

import logisticspipes.api.ILPPipe;
import logisticspipes.api.ILPPipeTile;
import logisticspipes.api.IRequestAPI;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class LPPipeInventory implements IMEInventory<IAEItemStack>
{
	private final TileEntity te;
	private final ForgeDirection direction;

	public LPPipeInventory( final TileEntity te, final ForgeDirection direction )
	{
		this.te = te;
		this.direction = direction;
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack input, Actionable type, BaseActionSource src )
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.LogisticsPipes ) )
		{
			ILogisticsPipes registry = (ILogisticsPipes) IntegrationRegistry.INSTANCE.getInstance( IntegrationType.LogisticsPipes );

			if( type == Actionable.SIMULATE )
			{
				if( registry.canAddItemsToPipe( this.te, input.getItemStack(), this.direction ) )
				{
					return null;
				}
				return input;
			}

			if( registry.addItemsToPipe( this.te, input.getItemStack(), this.direction ) )
			{
				return null;
			}
		}

		return input;
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable type, BaseActionSource src )
	{
		if( IntegrationRegistry.INSTANCE.isEnabled( IntegrationType.LogisticsPipes ) )
		{
			ILPPipe pipe = ( (ILPPipeTile) this.te ).getLPPipe();
			if( pipe instanceof IRequestAPI )
			{
				IRequestAPI requestAPI = (IRequestAPI) pipe;
				if( type == Actionable.SIMULATE )
				{
					IRequestAPI.SimulationResult simulation = requestAPI.simulateRequest( request.getItemStack() );
					if( simulation.used.size() == 0 )
					{
						return null;
					}

					return AEItemStack.create( simulation.used.get( 0 ) );
				}

				List<ItemStack> returned = requestAPI.performRequest( request.getItemStack() );
				if( returned.size() == 0 )
				{
					return null;
				}
				return AEItemStack.create( returned.get( 0 ) );
			}
		}

		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
	{
		ILPPipe pipe = ( (ILPPipeTile) this.te ).getLPPipe();
		if( pipe instanceof IRequestAPI )
		{
			List<ItemStack> provided = this.getLPItems( pipe );
			for( ItemStack is : provided )
			{
				out.add( AEItemStack.create( is ) );
			}
		}
		return out;
	}

	/**
	 * Get items from Logistics Pipes network _excluding_ AE2 network items. Brilliant!
	 * (basically rewrite of CoreRoutedPipe.getProvidedItems() (implementation of IRequestAPI.getProvidedItems())
	 *
	 * @param pipe IRequestAPI pipe
	 * @return list of items in shared networks
	 */
	public List<ItemStack> getLPItems( ILPPipe pipe )
	{
		CoreRoutedPipe coreRoutedPipeCast = (CoreRoutedPipe) pipe;

		if( coreRoutedPipeCast.stillNeedReplace() )
		{
			return new ArrayList<ItemStack>();
		}
		else
		{
			List<ExitRoute> exitRoutes = coreRoutedPipeCast.getRouter().getIRoutersByCost();
			ArrayList<ExitRoute> exitRoutesProcessed = new ArrayList<ExitRoute>();
			for( ExitRoute exitRoute : exitRoutes )
			{
				if( !isExitToAE( exitRoute ) )
				{
					exitRoutesProcessed.add( exitRoute );
				}
			}
			Map items = SimpleServiceLocator.logisticsManager.getAvailableItems( exitRoutesProcessed );
			ArrayList list = new ArrayList( items.size() );

			for( Object o : items.entrySet() )
			{
				Map.Entry item = (Map.Entry) o;
				ItemStack is = ( (ItemIdentifier) item.getKey() ).unsafeMakeNormalStack( ( (Integer) item.getValue() ).intValue() );
				list.add( is );
			}

			return list;
		}
	}

	/**
	 * Checks ExitRoute for connected AENetworkInvTiles
	 *
	 * @param exitRoute Logistics Pipes exit route to check
	 * @return true if AENetworkInvTiles is connected, otherwise false
	 */
	private boolean isExitToAE( ExitRoute exitRoute )
	{
		LinkedList<AdjacentTile> connectedEntities = exitRoute.destination.getPipe().getConnectedEntities();
		for( AdjacentTile connectedEntity : connectedEntities )
		{
			if( connectedEntity.tile instanceof AENetworkInvTile )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}
