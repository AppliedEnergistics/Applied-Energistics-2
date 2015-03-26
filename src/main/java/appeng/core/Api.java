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

import net.minecraftforge.common.util.ForgeDirection;

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
import appeng.api.parts.IPartHelper;
import appeng.api.storage.IStorageHelper;
import appeng.core.api.ApiPart;
import appeng.core.api.ApiStorage;
import appeng.core.features.registries.RegistryContainer;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.util.Platform;

public final class Api implements IAppEngApi
{

	public static final Api INSTANCE = new Api();

	private Api() {

	}

	// private MovableTileRegistry MovableRegistry = new MovableTileRegistry();
	private final RegistryContainer rc = new RegistryContainer();
	private final ApiStorage storageHelper = new ApiStorage();

	public final ApiPart partHelper = new ApiPart();

	private final Materials materials = new Materials();
	private final Items items = new Items();
	private final Blocks blocks = new Blocks();
	private final Parts parts = new Parts();

	@Override
	public IRegistryContainer registries()
	{
		return this.rc;
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
	public IStorageHelper storage()
	{
		return this.storageHelper;
	}

	@Override
	public IPartHelper partHelper()
	{
		return this.partHelper;
	}

	@Override
	public IGridNode createGridNode(IGridBlock blk)
	{
		if ( Platform.isClient() )
			throw new RuntimeException( "Grid Features are Server Side Only." );
		return new GridNode( blk );
	}

	@Override
	public IGridConnection createGridConnection(IGridNode a, IGridNode b) throws FailedConnection
	{
		return new GridConnection( a, b, ForgeDirection.UNKNOWN );
	}

}
