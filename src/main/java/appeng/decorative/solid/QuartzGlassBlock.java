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

package appeng.decorative.solid;


import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQuartzGlass;
import appeng.core.features.AEFeature;
import appeng.helpers.AEGlassMaterial;


public class QuartzGlassBlock extends AEBaseBlock
{
	public QuartzGlassBlock()
	{
		super( Material.glass );
		this.setLightOpacity( 0 );
		this.setOpaque( false );
		this.setFeature( EnumSet.of( AEFeature.DecorativeQuartzBlocks ) );
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQuartzGlass.class;
	}

	@Override
	public boolean shouldSideBeRendered( final IBlockAccess w, final BlockPos pos, final EnumFacing side )
	{
		final Material mat = w.getBlockState( pos ).getBlock().getMaterial();
		if( mat == Material.glass || mat == AEGlassMaterial.INSTANCE )
		{
			if( w.getBlockState( pos ).getBlock().getRenderType() == this.getRenderType() )
			{
				return false;
			}
		}
		return super.shouldSideBeRendered( w, pos, side );
	}
}
