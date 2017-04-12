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


import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.core.CreativeTab;
import com.google.common.base.Optional;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.EnumSet;


public final class AEBlockFeatureHandler implements IFeatureHandler
{
	private final AEBaseBlock featured;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final BlockDefinition definition;

	public AEBlockFeatureHandler( final EnumSet<AEFeature> features, final AEBaseBlock featured, final Optional<String> subName )
	{
		final ActivityState state = new FeaturedActiveChecker( features ).getActivityState();

		this.featured = featured;
		this.extractor = new FeatureNameExtractor( featured.getClass(), subName );
		this.enabled = state == ActivityState.Enabled;
		this.definition = new BlockDefinition( featured, state );
	}

	@Override
	public boolean isFeatureAvailable()
	{
		return this.enabled;
	}

	@Override
	public IBlockDefinition getDefinition()
	{
		return this.definition;
	}

	@Override
	public void register()
	{
		if( this.enabled )
		{
			final String name = this.extractor.get();
			this.featured.setCreativeTab( CreativeTab.instance );
			this.featured.setBlockName( /* "tile." */"appliedenergistics2." + name );
			this.featured.setBlockTextureName( "appliedenergistics2:" + name );

			final String registryName = "tile." + name;

			// Bypass the forge magic with null to register our own itemblock later.
			GameRegistry.registerBlock( this.featured, null, registryName );
			GameRegistry.registerItem( this.definition.maybeItem().get(), registryName );
		}
	}
}
