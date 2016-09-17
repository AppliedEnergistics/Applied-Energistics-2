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

package appeng.block.misc;


import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.tesr.SkyCompassTESR;


public class SkyCompassRendering extends BlockRenderingCustomizer
{

	private static final ModelResourceLocation ITEM_MODEL = new ModelResourceLocation( "appliedenergistics2:sky_compass", "normal" );

	@Override
	@SideOnly( Side.CLIENT )
	public void customize( IBlockRendering rendering, IItemRendering itemRendering )
	{
		rendering.tesr( new SkyCompassTESR() );
		// This disables the default smart-rotating model
		rendering.modelCustomizer( ( loc, model ) -> model );
		itemRendering.model( ITEM_MODEL );
		itemRendering.builtInModel( "models/block/builtin/sky_compass", new SkyCompassModel() );
	}

}
