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

package appeng.items.misc;


import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;


public class ItemCrystalSeedRendering extends ItemRenderingCustomizer
{

	private static final ModelResourceLocation[] MODELS_CERTUS = {
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_certus" ),
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_certus2" ),
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_certus3" )
	};
	private static final ModelResourceLocation[] MODELS_FLUIX = {
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_fluix" ),
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_fluix2" ),
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_fluix3" )
	};
	private static final ModelResourceLocation[] MODELS_NETHER = {
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_nether" ),
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_nether2" ),
			new ModelResourceLocation( "appliedenergistics2:crystal_seed_nether3" )
	};

	@Override
	@OnlyIn( Dist.CLIENT )
	public void customize( IItemRendering rendering )
	{
		rendering.variants( ImmutableList.<ResourceLocation>builder().add( MODELS_CERTUS ).add( MODELS_FLUIX ).add( MODELS_NETHER ).build() );
		// FIXME rendering.meshDefinition( this.getItemMeshDefinition() );
	}

	// FIXME private ItemMeshDefinition getItemMeshDefinition()
	// FIXME {
	// FIXME 	return is ->
	// FIXME 	{
	// FIXME 		int damage = ItemCrystalSeed.getProgress( is );
// FIXME
	// FIXME 		// Split the damage value into crystal type and growth level
	// FIXME 		int type = damage / ItemCrystalSeed.SINGLE_OFFSET;
	// FIXME 		int level = ( damage % ItemCrystalSeed.SINGLE_OFFSET ) / ItemCrystalSeed.LEVEL_OFFSET;
// FIXME
	// FIXME 		// Determine which list of models to use based on the type of crystal
	// FIXME 		ModelResourceLocation[] models;
	// FIXME 		switch( type )
	// FIXME 		{
	// FIXME 			case 0:
	// FIXME 				models = MODELS_CERTUS;
	// FIXME 				break;
	// FIXME 			case 1:
	// FIXME 				models = MODELS_NETHER;
	// FIXME 				break;
	// FIXME 			case 2:
	// FIXME 				models = MODELS_FLUIX;
	// FIXME 				break;
	// FIXME 			default:
	// FIXME 				// We use this as the fallback for broken items
	// FIXME 				models = MODELS_CERTUS;
	// FIXME 				break;
	// FIXME 		}
// FIXME
	// FIXME 		// Return one of the 3 models based on the level
	// FIXME 		if( level < 0 )
	// FIXME 		{
	// FIXME 			level = 0;
	// FIXME 		}
	// FIXME 		else if( level >= models.length )
	// FIXME 		{
	// FIXME 			level = models.length - 1;
	// FIXME 		}
// FIXME
	// FIXME 		return models[level];
	// FIXME 	};
	// FIXME }
}
