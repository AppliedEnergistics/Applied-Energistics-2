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

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.CreativeTab;


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
		// TODO use real identifier
		this.definition = new BlockDefinition( featured.getClass().getSimpleName(), featured, state );
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
	public void register( final Side side )
	{
		if( this.enabled )
		{
			final String name = this.extractor.get();
			this.featured.setRegistryName( AppEng.MOD_ID, name );
			this.featured.setCreativeTab( CreativeTab.instance );
			this.featured.setUnlocalizedName( "appliedenergistics2." + name );
			this.featured.setBlockTextureName( name );

			GameRegistry.register( this.featured );

			// register the block/item conversion...
			if( this.definition.maybeItem().isPresent() )
			{
				final Item featuredItem = this.definition.maybeItem().get();
				featuredItem.setRegistryName( AppEng.MOD_ID, name );
				GameRegistry.register( featuredItem );
			}
		}
	}
}
