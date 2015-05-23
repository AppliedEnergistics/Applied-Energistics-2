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

package appeng.core.features.registries;


import appeng.api.features.IGrinderRegistry;
import appeng.api.features.IInscriberRegistry;
import appeng.api.features.ILocatableRegistry;
import appeng.api.features.IMatterCannonAmmoRegistry;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.features.IPlayerRegistry;
import appeng.api.features.IRecipeHandlerRegistry;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.ISpecialComparisonRegistry;
import appeng.api.features.IWirelessTermRegistry;
import appeng.api.features.IWorldGen;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IExternalStorageRegistry;


/**
 * represents all registries
 *
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class RegistryContainer implements IRegistryContainer
{
	private final IGrinderRegistry grinder = new GrinderRecipeManager();
	private final IInscriberRegistry inscriber = new InscriberRegistry();
	private final IExternalStorageRegistry storage = new ExternalStorageRegistry();
	private final ICellRegistry cell = new CellRegistry();
	private final ILocatableRegistry locatable = new LocatableRegistry();
	private final ISpecialComparisonRegistry comparison = new SpecialComparisonRegistry();
	private final IWirelessTermRegistry wireless = new WirelessRegistry();
	private final IGridCacheRegistry gridCache = new GridCacheRegistry();
	private final IP2PTunnelRegistry p2pTunnel = new P2PTunnelRegistry();
	private final IMovableRegistry movable = new MovableTileRegistry();
	private final IMatterCannonAmmoRegistry matterCannonReg = new MatterCannonAmmoRegistry();
	private final IPlayerRegistry playerRegistry = new PlayerRegistry();
	private final IRecipeHandlerRegistry recipeReg = new RecipeHandlerRegistry();

	@Override
	public IMovableRegistry movable()
	{
		return this.movable;
	}

	@Override
	public IGridCacheRegistry gridCache()
	{
		return this.gridCache;
	}

	@Override
	public IExternalStorageRegistry externalStorage()
	{
		return this.storage;
	}

	@Override
	public ISpecialComparisonRegistry specialComparison()
	{
		return this.comparison;
	}

	@Override
	public IWirelessTermRegistry wireless()
	{
		return this.wireless;
	}

	@Override
	public ICellRegistry cell()
	{
		return this.cell;
	}

	@Override
	public IGrinderRegistry grinder()
	{
		return this.grinder;
	}

	@Override
	public IInscriberRegistry inscriber()
	{
		return this.inscriber;
	}

	@Override
	public ILocatableRegistry locatable()
	{
		return this.locatable;
	}

	@Override
	public IP2PTunnelRegistry p2pTunnel()
	{
		return this.p2pTunnel;
	}

	@Override
	public IMatterCannonAmmoRegistry matterCannon()
	{
		return this.matterCannonReg;
	}

	@Override
	public IPlayerRegistry players()
	{
		return this.playerRegistry;
	}

	@Override
	public IRecipeHandlerRegistry recipes()
	{
		return this.recipeReg;
	}

	@Override
	public IWorldGen worldgen()
	{
		return WorldGenRegistry.INSTANCE;
	}
}
