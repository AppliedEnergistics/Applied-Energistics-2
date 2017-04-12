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
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.exceptions.FailedConnection;
import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IStorageHelper;
import appeng.core.api.ApiPart;
import appeng.core.api.ApiStorage;
import appeng.core.features.registries.RegistryContainer;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.util.Platform;
import net.minecraftforge.common.util.ForgeDirection;


public final class Api implements IAppEngApi
{
	public static final Api INSTANCE = new Api();

	private final ApiPart partHelper;

	// private MovableTileRegistry MovableRegistry = new MovableTileRegistry();
	private final IRegistryContainer registryContainer;
	private final IStorageHelper storageHelper;
	private final Materials materials;
	private final Items items;
	private final Blocks blocks;
	private final Parts parts;
	private final ApiDefinitions definitions;

	private Api()
	{
		this.parts = new Parts();
		this.blocks = new Blocks();
		this.items = new Items();
		this.materials = new Materials();
		this.storageHelper = new ApiStorage();
		this.registryContainer = new RegistryContainer();
		this.partHelper = new ApiPart();
		this.definitions = new ApiDefinitions( this.partHelper );
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
	public ApiPart partHelper()
	{
		return this.partHelper;
	}

	@Override
	public Items items()
	{
		return this.items;
	}

	@Override
	public Materials materials()
	{
		return this.materials;
	}

	@Override
	public Blocks blocks()
	{
		return this.blocks;
	}

	@Override
	public Parts parts()
	{
		return this.parts;
	}

	@Override
	public ApiDefinitions definitions()
	{
		return this.definitions;
	}

	@Override
	public IGridNode createGridNode( final IGridBlock blk )
	{
		if( Platform.isClient() )
		{
			throw new IllegalStateException( "Grid features for " + blk + " are server side only." );
		}

		return new GridNode( blk );
	}

	@Override
	public IGridConnection createGridConnection( final IGridNode a, final IGridNode b ) throws FailedConnection
	{
		return new GridConnection( a, b, ForgeDirection.UNKNOWN );
	}
}
