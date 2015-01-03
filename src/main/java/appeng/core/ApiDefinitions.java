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

package appeng.core;


import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.definitions.IParts;
import appeng.api.parts.IPartHelper;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.api.definitions.ApiMaterials;
import appeng.core.api.definitions.ApiParts;


/**
 * Internal implementation of the definitions for the API
 */
public final class ApiDefinitions implements IDefinitions
{
	private final IBlocks blocks;
	private final IItems items;
	private final IMaterials materials;
	private final IParts parts;
	private final FeatureHandlerRegistry handlers;
	private final FeatureRegistry features;

	public ApiDefinitions( IPartHelper partHelper )
	{
		this.features = new FeatureRegistry();
		this.handlers = new FeatureHandlerRegistry();

		this.blocks = new ApiBlocks( this.features, this.handlers );
		this.items = new ApiItems( this.features, this.handlers );
		this.materials = new ApiMaterials( this.features, this.handlers );
		this.parts = new ApiParts( this.features, this.handlers, partHelper );
	}

	public FeatureHandlerRegistry getFeatureHandlerRegistry()
	{
		return this.handlers;
	}

	public FeatureRegistry getFeatureRegistry()
	{
		return this.features;
	}

	@Override
	public IBlocks blocks()
	{
		return this.blocks;
	}

	@Override
	public IItems items()
	{
		return this.items;
	}

	@Override
	public IMaterials materials()
	{
		return this.materials;
	}

	@Override
	public IParts parts()
	{
		return this.parts;
	}
}
