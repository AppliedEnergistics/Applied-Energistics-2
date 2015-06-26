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

import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.core.CommonHelper;
import appeng.core.CreativeTab;


public final class AEBlockFeatureHandler implements IFeatureHandler
{
	private final AEBaseBlock featured;
	private final FeatureNameExtractor extractor;
	private final boolean enabled;
	private final BlockDefinition definition;

	public AEBlockFeatureHandler( EnumSet<AEFeature> features, AEBaseBlock featured, Optional<String> subName )
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
	public void register(Side side)
	{
		if( this.enabled )
		{
			String name = this.extractor.get();
			this.featured.setCreativeTab( CreativeTab.instance );
			this.featured.setUnlocalizedName( /* "tile." */"appliedenergistics2." + name );
			this.featured.setBlockTextureName( name );

			// Bypass the forge magic with null to register our own itemblock later.
			GameRegistry.registerBlock( this.featured, null, name );
			GameRegistry.registerItem( this.definition.maybeItem().get(), name );
			
			// register the block/item conversion...
            if ( this.definition.maybeItem().isPresent() )
            	GameData.getBlockItemMap().put( this.featured, this.definition.maybeItem().get() );
            
            if ( side == Side.CLIENT)
            	CommonHelper.proxy.configureIcon( this.featured, name );
		}
	}
}
