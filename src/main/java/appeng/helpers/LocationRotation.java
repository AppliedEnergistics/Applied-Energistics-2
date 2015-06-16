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


import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import appeng.api.util.IOrientable;


public class LocationRotation implements IOrientable
{

	final IBlockAccess w;
	final int x;
	final int y;
	final int z;

	public LocationRotation( IBlockAccess world, int x, int y, int z )
	{
		this.w = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean canBeRotated()
	{
		return false;
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
		int num = Math.abs( this.x + this.y + this.z ) % 6;
		return EnumFacing.VALUES[ num ];
	}

	@Override
	public void setOrientation( EnumFacing forward, EnumFacing up )
	{

	}
}
