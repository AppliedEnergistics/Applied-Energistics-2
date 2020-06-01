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

package appeng.block.storage;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.tesr.SkyChestTESR;


public class SkyChestRenderingCustomizer extends BlockRenderingCustomizer
{
	private final BlockSkyChest.SkyChestType type;

	public SkyChestRenderingCustomizer( BlockSkyChest.SkyChestType type )
	{
		this.type = type;
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		// Register a custom non-tesr item model
		// FIXME: This should not be required anymore!
		// FIXME String modelName = this.getModelFromType();
		// FIXME ModelResourceLocation model = new ModelResourceLocation( "appliedenergistics2:" + modelName, "inventory" );
		// FIXME itemRendering.model( model ).variants( model );
	}

	private String getModelFromType()
	{
		final String modelName;
		switch( this.type )
		{
			default:
			case STONE:
				modelName = "sky_stone_chest";
				break;
			case BLOCK:
				modelName = "smooth_sky_stone_chest";
				break;
		}
		return modelName;
	}
}
