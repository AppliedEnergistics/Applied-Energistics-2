/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.helpers;


import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;


public class MetaRotation implements IOrientable
{

	final boolean useFacing;
	final IBlockAccess w;
	final BlockPos pos;

	public MetaRotation( final IBlockAccess world, final BlockPos pos, final boolean FullFacing )
	{
		this.w = world;
		this.pos = pos;
		this.useFacing = FullFacing;
	}

	@Override
	public boolean canBeRotated()
	{
		return true;
	}

	@Override
	public EnumFacing getForward()
	{
		if( this.getUp().getFrontOffsetY() == 0 )
		{
			return EnumFacing.UP;
		}
		return EnumFacing.SOUTH;
	}

	@Override
	public EnumFacing getUp()
	{
		final IBlockState state = this.w.getBlockState( this.pos );
		
		if ( this.useFacing )
		{
			final EnumFacing f = state == null ? EnumFacing.UP : (EnumFacing) state.getValue( BlockTorch.FACING );
			return f;
		}
		
		Axis a = state == null ? null : (Axis) state.getValue( AEBaseBlock.AXIS_ORIENTATION );
		
		if ( a == null )
			a = Axis.Y;
		
		switch( a )
		{
			case X:
				return EnumFacing.EAST;
			case Z:
				return EnumFacing.SOUTH;
			default:
			case Y:
				return EnumFacing.UP;
		}
	}

	@Override
	public void setOrientation( final EnumFacing forward, final EnumFacing up )
	{
		if( this.w instanceof World )
		{
			if ( this.useFacing )
				( (World) this.w ).setBlockState( this.pos, this.w.getBlockState( this.pos ).withProperty( BlockTorch.FACING, up ) );
			else
				( (World) this.w ).setBlockState( this.pos, this.w.getBlockState( this.pos ).withProperty( AEBaseBlock.AXIS_ORIENTATION, up.getAxis() ) );
		}
		else
		{
			throw new IllegalStateException( this.w.getClass().getName() + " received, expected World" );
		}
	}
}
