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

package appeng.core.features.registries;


import appeng.api.features.IGrinderRegistry;
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


public class RegistryContainer implements IRegistryContainer
{

	private final GrinderRecipeManager GrinderRecipes = new GrinderRecipeManager();
	private final ExternalStorageRegistry ExternalStorageHandlers = new ExternalStorageRegistry();
	private final CellRegistry CellRegistry = new CellRegistry();
	private final LocatableRegistry LocatableRegistry = new LocatableRegistry();
	private final SpecialComparisonRegistry SpecialComparisonRegistry = new SpecialComparisonRegistry();
	private final WirelessRegistry WirelessRegistry = new WirelessRegistry();
	private final GridCacheRegistry GridCacheRegistry = new GridCacheRegistry();
	private final P2PTunnelRegistry P2PRegistry = new P2PTunnelRegistry();
	private final MovableTileRegistry MovableReg = new MovableTileRegistry();
	private final MatterCannonAmmoRegistry matterCannonReg = new MatterCannonAmmoRegistry();
	private final PlayerRegistry playerRegistry = new PlayerRegistry();
	private final IRecipeHandlerRegistry recipeReg = new RecipeHandlerRegistry();

	@Override
	public IMovableRegistry movable()
	{
		return this.MovableReg;
	}

	@Override
	public IGridCacheRegistry gridCache()
	{
		return this.GridCacheRegistry;
	}

	@Override
	public IExternalStorageRegistry externalStorage()
	{
		return this.ExternalStorageHandlers;
	}

	@Override
	public ISpecialComparisonRegistry specialComparison()
	{
		return this.SpecialComparisonRegistry;
	}

	@Override
	public IWirelessTermRegistry wireless()
	{
		return this.WirelessRegistry;
	}

	@Override
	public ICellRegistry cell()
	{
		return this.CellRegistry;
	}

	@Override
	public IGrinderRegistry grinder()
	{
		return this.GrinderRecipes;
	}

	@Override
	public ILocatableRegistry locatable()
	{
		return this.LocatableRegistry;
	}

	@Override
	public IP2PTunnelRegistry p2pTunnel()
	{
		return this.P2PRegistry;
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
