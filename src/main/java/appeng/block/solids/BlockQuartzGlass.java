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

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQuartzGlass;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockQuartzGlass extends AEBaseBlock
{

	public BlockQuartzGlass() {
		this( BlockQuartzGlass.class );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQuartzGlass.class;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess w, int x, int y, int z, int side)
	{
		Material mat = w.getBlock( x, y, z ).getMaterial();
		if ( mat == Material.glass || mat == AEGlassMaterial.instance )
		{
			if ( w.getBlock( x, y, z ).getRenderType() == this.getRenderType() )
				return false;
		}
		return super.shouldSideBeRendered( w, x, y, z, side );
	}

	public BlockQuartzGlass(Class c) {
		super( c, Material.glass );
		setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
		setLightOpacity( 0 );
		isOpaque = false;
	}

}
