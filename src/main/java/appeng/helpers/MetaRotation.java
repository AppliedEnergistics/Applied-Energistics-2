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


import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import appeng.api.util.IOrientable;
import appeng.decorative.solid.BlockQuartzPillar;


public class MetaRotation implements IOrientable
{

	private final IProperty<Direction> facingProp;
	private final IBlockReader w;
	private final BlockPos pos;

	public MetaRotation( final IBlockReader world, final BlockPos pos, final IProperty<Direction> facingProp )
	{
		this.w = world;
		this.pos = pos;
		this.facingProp = facingProp;
	}

	@Override
	public boolean canBeRotated()
	{
		return true;
	}

	@Override
	public Direction getForward()
	{
		if( this.getUp().getFrontOffsetY() == 0 )
		{
			return Direction.UP;
		}
		return Direction.SOUTH;
	}

	@Override
	public Direction getUp()
	{
		final BlockState state = this.w.getBlockState( this.pos );

		if( this.facingProp != null )
		{
			return state.getValue( this.facingProp );
		}

		// TODO 1.10.2-R - Temp
		Axis a = state.getValue( BlockQuartzPillar.AXIS_ORIENTATION );

		if( a == null )
		{
			a = Axis.Y;
		}

		switch( a )
		{
			case X:
				return Direction.EAST;
			case Z:
				return Direction.SOUTH;
			default:
			case Y:
				return Direction.UP;
		}
	}

	@Override
	public void setOrientation( final Direction forward, final Direction up )
	{
		if( this.w instanceof World )
		{
			if( this.facingProp != null )
			{
				( (World) this.w ).setBlockState( this.pos, this.w.getBlockState( this.pos ).withProperty( this.facingProp, up ) );
			}
			else
			{
				// TODO 1.10.2-R - Temp
				( (World) this.w ).setBlockState( this.pos, this.w.getBlockState( this.pos ).withProperty( BlockQuartzPillar.AXIS_ORIENTATION, up.getAxis() ) );
			}
		}
		else
		{
			throw new IllegalStateException( this.w.getClass().getName() + " received, expected World" );
		}
	}
}
