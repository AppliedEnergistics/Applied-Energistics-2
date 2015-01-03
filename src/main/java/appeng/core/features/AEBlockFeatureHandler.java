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

import appeng.api.util.AEItemDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.core.CommonHelper;
import appeng.core.CreativeTab;
import appeng.util.Platform;


public class AEBlockFeatureHandler implements IFeatureHandler
{
	private final EnumSet<AEFeature> features;
	private final AEBaseBlock featured;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final AEBlockDefinition definition;

	public AEBlockFeatureHandler( EnumSet<AEFeature> features, AEBaseBlock featured, Optional<String> subName )
	{
		this.features = features;
		this.featured = featured;
		this.extractor = new FeatureNameExtractor( featured.getClass(), subName );
		this.enabled = new FeaturedActiveChecker( features ).isFeatureActive();
		this.definition = new AEBlockDefinition( featured, this.enabled );
	}

	@Override
	public boolean isFeatureAvailable()
	{
		return this.enabled;
	}

	@Override
	public EnumSet<AEFeature> getFeatures()
	{
		return this.features;
	}

	@Override
	public AEItemDefinition getDefinition()
	{
		return this.definition;
	}

	@Override
	public void register()
	{
		if ( this.enabled )
		{
			String name = this.extractor.get();
			this.featured.setCreativeTab( CreativeTab.instance );
			this.featured.setBlockName( /* "tile." */"appliedenergistics2." + name );
			this.featured.setBlockTextureName( "appliedenergistics2:" + name );

			if ( Platform.isClient() )
			{
				CommonHelper.proxy.bindTileEntitySpecialRenderer( this.featured.getTileEntityClass(), this.featured );
			}

			Class<? extends AEBaseItemBlock> itemBlockClass = this.featured.getItemBlockClass();

			GameRegistry.registerBlock( this.featured, itemBlockClass, "tile." + name );
		}
	}
}
