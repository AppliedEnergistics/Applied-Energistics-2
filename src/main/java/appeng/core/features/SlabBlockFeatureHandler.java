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

package appeng.core.features;


import java.util.EnumSet;

import com.google.common.base.Optional;

import cpw.mods.fml.common.registry.GameRegistry;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseItemBlockSlab;
import appeng.block.AEBaseSlabBlock;
import appeng.core.CreativeTab;


public class SlabBlockFeatureHandler implements IFeatureHandler
{
	private final AEBaseSlabBlock slabs;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final BlockDefinition definition;

	public SlabBlockFeatureHandler( EnumSet<AEFeature> features, AEBaseSlabBlock slabs )
	{
		final ActivityState state = new FeaturedActiveChecker( features ).getActivityState();
		this.slabs = slabs;
		this.extractor = new FeatureNameExtractor( slabs.getClass(), Optional.<String>absent() );
		this.enabled = state == ActivityState.Enabled;
		this.definition = new BlockDefinition( slabs, state );
	}

	@Override
	public final boolean isFeatureAvailable()
	{
		return this.enabled;
	}

	@Override
	public final IBlockDefinition getDefinition()
	{
		return this.definition;
	}

	@Override
	public final void register()
	{
		if( this.enabled )
		{
			this.slabs.setCreativeTab( CreativeTab.instance );
			GameRegistry.registerBlock( slabs, AEBaseItemBlockSlab.class, "tile." + slabs.name(), slabs, slabs.doubleSlabs(), false );
			GameRegistry.registerBlock( slabs.doubleSlabs(), AEBaseItemBlockSlab.class, "tile." + slabs.name() + ".double", slabs, slabs.doubleSlabs(), true );
		}
	}
}
