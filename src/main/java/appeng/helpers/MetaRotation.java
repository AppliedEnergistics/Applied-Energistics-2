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


import appeng.api.util.IOrientable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;


public class MetaRotation implements IOrientable
{

	private final IBlockAccess w;
	private final int x;
	private final int y;
	private final int z;

	public MetaRotation( final IBlockAccess world, final int x, final int y, final int z )
	{
		this.w = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public boolean canBeRotated()
	{
		return true;
	}

	@Override
	public ForgeDirection getForward()
	{
		if( this.getUp().offsetY == 0 )
		{
			return ForgeDirection.UP;
		}
		return ForgeDirection.SOUTH;
	}

	@Override
	public ForgeDirection getUp()
	{
		return ForgeDirection.getOrientation( this.w.getBlockMetadata( this.x, this.y, this.z ) );
	}

	@Override
	public void setOrientation( final ForgeDirection forward, final ForgeDirection up )
	{
		if( this.w instanceof World )
		{
			( (World) this.w ).setBlockMetadataWithNotify( this.x, this.y, this.z, up.ordinal(), 1 + 2 );
		}
		else
		{
			throw new IllegalStateException( this.w.getClass().getName() + " received, expected World" );
		}
	}
}
