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

package appeng.block;


import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

import com.google.common.base.Optional;

import appeng.core.features.AEFeature;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.StairBlockFeatureHandler;


public abstract class AEBaseStairBlock extends BlockStairs implements IAEFeature
{
	private final IFeatureHandler features;

	protected AEBaseStairBlock( Block block, int meta, EnumSet<AEFeature> features )
	{
		super( block, meta );

		this.features = new StairBlockFeatureHandler( features, this, Optional.<String>absent() );
		this.setBlockName( block.getUnlocalizedName() );

		this.setLightOpacity( 0 );
	}

	@Override
	public IFeatureHandler handler()
	{
		return this.features;
	}

	@Override
	public void postInit()
	{
		// Override to do stuff
	}
}
