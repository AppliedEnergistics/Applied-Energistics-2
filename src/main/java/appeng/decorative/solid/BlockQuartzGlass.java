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


import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.block.AEBaseBlock;


public class BlockQuartzGlass extends AEBaseBlock
{

	public static final PropertyBool[] props = { PropertyBool.create( "down" ), PropertyBool.create( "up" ), PropertyBool.create( "north" ), PropertyBool.create( "south" ), PropertyBool.create( "west" ), PropertyBool.create( "east" ) };

	private static boolean isGlassBlock( IBlockAccess world, BlockPos pos, EnumFacing facing )
	{
		return world.getBlockState( pos.offset( facing ) ).getBlock() instanceof BlockQuartzGlass;
	}

	public BlockQuartzGlass()
	{
		super( Material.GLASS );
		this.setLightOpacity( 0 );
		this.setOpaque( false );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return props;
	}

	@Override
	public int getMetaFromState( IBlockState state )
	{
		return 0;
	}

	@Override
	public IBlockState getExtendedState( IBlockState state, IBlockAccess world, BlockPos pos )
	{
		for( EnumFacing facing : EnumFacing.values() )
		{
			state = state.withProperty( props[facing.ordinal()], isGlassBlock( world, pos, facing ) );
		}
		return state;
	}

	@Override
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean shouldSideBeRendered( final IBlockState state, final IBlockAccess w, final BlockPos pos, final EnumFacing side )
	{
		return !isGlassBlock( w, pos, side ) && super.shouldSideBeRendered( state, w, pos, side );
	}

	@Override
	public boolean isFullCube( IBlockState state )
	{
		return false;
	}

}
