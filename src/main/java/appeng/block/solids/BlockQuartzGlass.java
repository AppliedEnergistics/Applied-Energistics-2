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

package appeng.block.solids;


import appeng.block.AEBaseBlock;
import appeng.client.render.blocks.RenderQuartzGlass;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

import java.util.EnumSet;


public class BlockQuartzGlass extends AEBaseBlock
{
	public BlockQuartzGlass()
	{
		super( Material.glass );
		this.setLightOpacity( 0 );
		this.isOpaque = false;
		this.setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public RenderQuartzGlass getRenderer()
	{
		return new RenderQuartzGlass();
	}

	@Override
	public boolean shouldSideBeRendered( final IBlockAccess w, final int x, final int y, final int z, final int side )
	{
		final Material mat = w.getBlock( x, y, z ).getMaterial();
		if( mat == Material.glass || mat == AEGlassMaterial.INSTANCE )
		{
			if( w.getBlock( x, y, z ).getRenderType() == this.getRenderType() )
			{
				return false;
			}
		}
		return super.shouldSideBeRendered( w, x, y, z, side );
	}
}
