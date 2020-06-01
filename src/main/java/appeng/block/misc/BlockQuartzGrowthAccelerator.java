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


import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.BooleanProperty;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import appeng.util.Platform;


public class BlockQuartzGrowthAccelerator extends AEBaseTileBlock implements IOrientableBlock
{

	private static final BooleanProperty POWERED = BooleanProperty.create( "powered" );

	public BlockQuartzGrowthAccelerator()
	{
		super( Material.ROCK );
		this.setSoundType( SoundType.METAL );
		this.setDefaultState( this.getDefaultState().with( POWERED, false ) );
	}

	@Override
	public BlockState getActualState( BlockState state, IBlockReader world, BlockPos pos )
	{
		TileQuartzGrowthAccelerator te = this.getTileEntity( world, pos );
		boolean powered = te != null && te.isPowered();

		return super.getActualState( state, world, pos )
				.with( POWERED, powered );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { POWERED };
	}

	@OnlyIn( Dist.CLIENT )
	@Override
	public void animateTick( final BlockState state, final World w, final BlockPos pos, final Random r )
	{
		if( !AEConfig.instance().isEnableEffects() )
		{
			return;
		}

		final TileQuartzGrowthAccelerator cga = this.getTileEntity( w, pos );

		if( cga != null && cga.isPowered() && AppEng.proxy.shouldAddParticles( r ) )
		{
			final double d0 = r.nextFloat() - 0.5F;
			final double d1 = r.nextFloat() - 0.5F;

			final Direction up = cga.getUp();
			final Direction forward = cga.getForward();
			final Direction west = Platform.crossProduct( forward, up );

			double rx = 0.5 + pos.getX();
			double ry = 0.5 + pos.getY();
			double rz = 0.5 + pos.getZ();

			rx += up.getFrontOffsetX() * d0;
			ry += up.getFrontOffsetY() * d0;
			rz += up.getFrontOffsetZ() * d0;

			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			double dz = 0;
			double dx = 0;
			BlockPos pt = null;

			switch( r.nextInt( 4 ) )
			{
				case 0:
					dx = 0.6;
					dz = d1;
					pt = new BlockPos( x + west.getFrontOffsetX(), y + west.getFrontOffsetY(), z + west.getFrontOffsetZ() );

					break;
				case 1:
					dx = d1;
					dz += 0.6;
					pt = new BlockPos( x + forward.getFrontOffsetX(), y + forward.getFrontOffsetY(), z + forward.getFrontOffsetZ() );

					break;
				case 2:
					dx = d1;
					dz = -0.6;
					pt = new BlockPos( x - forward.getFrontOffsetX(), y - forward.getFrontOffsetY(), z - forward.getFrontOffsetZ() );

					break;
				case 3:
					dx = -0.6;
					dz = d1;
					pt = new BlockPos( x - west.getFrontOffsetX(), y - west.getFrontOffsetY(), z - west.getFrontOffsetZ() );

					break;
			}

			if( !w.getBlockState( pt ).getBlock().isAir( w.getBlockState( pt ), w, pt ) )
			{
				return;
			}

			rx += dx * west.getFrontOffsetX();
			ry += dx * west.getFrontOffsetY();
			rz += dx * west.getFrontOffsetZ();

			rx += dz * forward.getFrontOffsetX();
			ry += dz * forward.getFrontOffsetY();
			rz += dz * forward.getFrontOffsetZ();

			final LightningFX fx = new LightningFX( w, rx, ry, rz, 0.0D, 0.0D, 0.0D );
			Minecraft.getInstance().effectRenderer.addEffect( fx );
		}
	}

	@Override
	public boolean usesMetadata()
	{
		return false;
	}

}
