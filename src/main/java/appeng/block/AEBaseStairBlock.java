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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

import appeng.core.features.AEBlockFeatureHandler;
import appeng.core.features.AEFeature;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;


public abstract class AEBaseStairBlock extends BlockStairs implements IAEFeature, IHasSpecialItemModel
{
	private final IFeatureHandler features;

	protected AEBaseStairBlock( final Block block, final EnumSet<AEFeature> features, final String type )
	{
		super( block.getDefaultState() );

		Preconditions.checkNotNull( block );
		Preconditions.checkNotNull( block.getUnlocalizedName() );
		Preconditions.checkArgument( block.getUnlocalizedName().length() > 0 );

		this.features = new AEBlockFeatureHandler( features, this, Optional.of( type ) );

		this.setUnlocalizedName( "stair." + type );
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
