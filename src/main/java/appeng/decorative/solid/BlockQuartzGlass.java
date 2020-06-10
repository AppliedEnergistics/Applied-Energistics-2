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
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.block.AEBaseBlock;
import appeng.helpers.AEGlassMaterial;


public class BlockQuartzGlass extends AEBaseBlock
{

	// This unlisted property is used to determine the actual block that should be rendered
	public static final UnlistedGlassStateProperty GLASS_STATE = new UnlistedGlassStateProperty();

	public BlockQuartzGlass()
	{
		super( Material.GLASS );
		this.setLightOpacity( 0 );
		this.setOpaque( false );
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		IProperty[] listedProperties = new IProperty[0];
		IUnlistedProperty[] unlistedProperties = new IUnlistedProperty[] { GLASS_STATE };
		return new ExtendedBlockState( this, listedProperties, unlistedProperties );
	}

	@Override
	public BlockState getExtendedState( BlockState state, IBlockReader world, BlockPos pos )
	{

		EnumSet<Direction> flushWith = EnumSet.noneOf( Direction.class );
		// Test every direction for another glass block
		for( Direction facing : Direction.values() )
		{
			if( isGlassBlock( world, pos, facing ) )
			{
				flushWith.add( facing );
			}
		}

		GlassState glassState = new GlassState( pos.getX(), pos.getY(), pos.getZ(), flushWith );

		IExtendedBlockState extState = (IExtendedBlockState) state;

		return extState.withProperty( GLASS_STATE, glassState );
	}

	private static boolean isGlassBlock( IBlockReader world, BlockPos pos, Direction facing )
	{
		return world.getBlockState( pos.offset( facing ) ).getBlock() instanceof BlockQuartzGlass;
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean shouldSideBeRendered( final BlockState state, final IBlockReader w, final BlockPos pos, final Direction side )
	{
		BlockPos adjacentPos = pos.offset( side );

		final Material mat = w.getBlockState( adjacentPos ).getMaterial();

		if( mat == Material.GLASS || mat == AEGlassMaterial.INSTANCE )
		{
			if( w.getBlockState( adjacentPos ).getRenderType() == this.getRenderType( state ) )
			{
				return false;
			}
		}

		return super.shouldSideBeRendered( state, w, pos, side );
	}

	@Override
	public boolean isFullCube( BlockState state )
	{
		return false;
	}
}
