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

package appeng.core;


import appeng.api.IAppEngApi;
import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridHelper;
import appeng.api.parts.IPartHelper;
import appeng.api.storage.IStorageHelper;
import appeng.api.util.IClientHelper;
import appeng.core.features.registries.PartModels;


public final class Api implements IAppEngApi
{
	public static final Api INSTANCE = new Api();

// FIXME	private final ApiPart partHelper;

	// private MovableTileRegistry MovableRegistry = new MovableTileRegistry();
// FIXME	private final IRegistryContainer registryContainer;
// FIXME	private final IStorageHelper storageHelper;
// FIXME	private final IGridHelper networkHelper;
	private final ApiDefinitions definitions;
// FIXME	private final IClientHelper client;

	private Api()
	{
// FIXME		this.storageHelper = new ApiStorage();
// FIXME		this.networkHelper = new ApiGrid();
// FIXME		this.registryContainer = new RegistryContainer();
// FIXME		this.partHelper = new ApiPart();
		PartModels partModels = new PartModels();
		this.definitions = new ApiDefinitions( partModels /* FIXME (PartModels) this.registryContainer.partModels() */ );
// FIXME		this.client = new ApiClientHelper();
	}

// FIXME	public PartModels getPartModels()
// FIXME	{
// FIXME		return (PartModels) this.registryContainer.partModels();
// FIXME	}
// FIXME
// FIXME	@Override
// FIXME	public IRegistryContainer registries()
// FIXME	{
// FIXME		return this.registryContainer;
// FIXME	}
// FIXME
// FIXME	@Override
// FIXME	public IStorageHelper storage()
// FIXME	{
// FIXME		return this.storageHelper;
// FIXME	}
// FIXME
// FIXME	@Override
// FIXME	public IGridHelper grid()
// FIXME	{
// FIXME		return this.networkHelper;
// FIXME	}
// FIXME
// FIXME	@Override
// FIXME	public ApiPart partHelper()
// FIXME	{
// FIXME		return this.partHelper;
// FIXME	}

	@Override
	public ApiDefinitions definitions()
	{
		return this.definitions;
	}

// FIXME	@Override
// FIXME	public IClientHelper client()
// FIXME	{
// FIXME		return this.client;
// FIXME	}

	// FIXME
	@Override
	public IRegistryContainer registries() {
		return null;
	}
	// FIXME
	@Override
	public IStorageHelper storage() {
		return null;
	}
	// FIXME
	@Override
	public IGridHelper grid() {
		return null;
	}
	// FIXME
	@Override
	public IPartHelper partHelper() {
		return null;
	}
	// FIXME
	@Override
	public IClientHelper client() {
		return null;
	}
}
