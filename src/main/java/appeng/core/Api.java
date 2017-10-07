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
import appeng.api.networking.INetworkHelper;
import appeng.api.storage.IStorageHelper;
import appeng.core.api.ApiNetwork;
import appeng.core.api.ApiPart;
import appeng.core.api.ApiStorage;
import appeng.core.features.registries.PartModels;
import appeng.core.features.registries.RegistryContainer;


public final class Api implements IAppEngApi
{
	public static final Api INSTANCE = new Api();

	private final ApiPart partHelper;

	// private MovableTileRegistry MovableRegistry = new MovableTileRegistry();
	private final IRegistryContainer registryContainer;
	private final IStorageHelper storageHelper;
	private final INetworkHelper networkHelper;
	private final ApiDefinitions definitions;

	private Api()
	{
		this.storageHelper = new ApiStorage();
		this.networkHelper = new ApiNetwork();
		this.registryContainer = new RegistryContainer();
		this.partHelper = new ApiPart();
		this.definitions = new ApiDefinitions( (PartModels) this.registryContainer.partModels() );
	}

	public PartModels getPartModels()
	{
		return (PartModels) this.registryContainer.partModels();
	}

	@Override
	public IRegistryContainer registries()
	{
		return this.registryContainer;
	}

	@Override
	public IStorageHelper storage()
	{
		return this.storageHelper;
	}

	@Override
	public INetworkHelper network()
	{
		return this.networkHelper;
	}

	@Override
	public ApiPart partHelper()
	{
		return this.partHelper;
	}

	@Override
	public ApiDefinitions definitions()
	{
		return this.definitions;
	}
}
