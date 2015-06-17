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


import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockQuartzAccelerator;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import appeng.util.Platform;


public class BlockQuartzGrowthAccelerator extends AEBaseTileBlock
{
	public BlockQuartzGrowthAccelerator()
	{
		super( Material.rock );
		this.setStepSound( Block.soundTypeMetal );
		this.setTileEntity( TileQuartzGrowthAccelerator.class );
		this.setFeature( EnumSet.of( AEFeature.Core ) );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockQuartzAccelerator.class;
	}

	@Override
	public void randomDisplayTick(
			World w,
			BlockPos pos,
			IBlockState state,
			Random r )
	{
		if( !AEConfig.instance.enableEffects )
		{
			return;
		}

		TileQuartzGrowthAccelerator cga = this.getTileEntity( w, pos );

		if( cga != null && cga.hasPower && CommonHelper.proxy.shouldAddParticles( r ) )
		{
			double d0 = r.nextFloat() - 0.5F;
			double d1 = r.nextFloat() - 0.5F;

			EnumFacing up = cga.getUp();
			EnumFacing forward = cga.getForward();
			EnumFacing west = Platform.crossProduct( forward, up );

			double rx = 0.5 + pos.getX();
			double ry = 0.5 + pos.getY();
			double rz = 0.5 + pos.getZ();

			double dx = 0;
			double dz = 0;

			rx += up.getFrontOffsetX() * d0;
			ry += up.getFrontOffsetY() * d0;
			rz += up.getFrontOffsetZ() * d0;

			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			
			switch( r.nextInt( 4 ) )
			{
				case 0:
					dx = 0.6;
					dz = d1;
					BlockPos pt = new BlockPos( x + west.getFrontOffsetX(), y + west.getFrontOffsetY(), z + west.getFrontOffsetZ() );					 
					if( !w.getBlockState(pt).getBlock().isAir( w, pt) )
					{
						return;
					}
					break;
				case 1:
					dx = d1;
					dz += 0.6;
					pt = new BlockPos( x + forward.getFrontOffsetX(), y + forward.getFrontOffsetY(), z + forward.getFrontOffsetZ() );					 
					if( !w.getBlockState(pt).getBlock().isAir( w, pt) )
					{
						return;
					}
					break;
				case 2:
					dx = d1;
					dz = -0.6;
					pt = new BlockPos( x - forward.getFrontOffsetX(), y - forward.getFrontOffsetY(), z - forward.getFrontOffsetZ() );					 
					if( !w.getBlockState(pt).getBlock().isAir( w, pt) )
					{
						return;
					}
					break;
				case 3:
					dx = -0.6;
					dz = d1;
					pt = new BlockPos( x - west.getFrontOffsetX(), y - west.getFrontOffsetY(), z - west.getFrontOffsetZ() );					 
					if( !w.getBlockState(pt).getBlock().isAir( w, pt) )
					{
						return;
					}
					break;
			}

			rx += dx * west.getFrontOffsetX();
			ry += dx * west.getFrontOffsetY();
			rz += dx * west.getFrontOffsetZ();

			rx += dz * forward.getFrontOffsetX();
			ry += dz * forward.getFrontOffsetY();
			rz += dz * forward.getFrontOffsetZ();

			LightningFX fx = new LightningFX( w, rx, ry, rz, 0.0D, 0.0D, 0.0D );
			Minecraft.getMinecraft().effectRenderer.addEffect( fx );
		}
	}
}
