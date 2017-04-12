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

package appeng.parts;


import appeng.api.parts.IPartCollisionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;


public class BusCollisionHelper implements IPartCollisionHelper
{

	private final List<AxisAlignedBB> boxes;

	private final ForgeDirection x;
	private final ForgeDirection y;
	private final ForgeDirection z;

	private final Entity entity;
	private final boolean isVisual;

	public BusCollisionHelper( final List<AxisAlignedBB> boxes, final ForgeDirection x, final ForgeDirection y, final ForgeDirection z, final Entity e, final boolean visual )
	{
		this.boxes = boxes;
		this.x = x;
		this.y = y;
		this.z = z;
		this.entity = e;
		this.isVisual = visual;
	}

	public BusCollisionHelper( final List<AxisAlignedBB> boxes, final ForgeDirection s, final Entity e, final boolean visual )
	{
		this.boxes = boxes;
		this.entity = e;
		this.isVisual = visual;

		switch( s )
		{
			case DOWN:
				this.x = ForgeDirection.EAST;
				this.y = ForgeDirection.NORTH;
				this.z = ForgeDirection.DOWN;
				break;
			case UP:
				this.x = ForgeDirection.EAST;
				this.y = ForgeDirection.SOUTH;
				this.z = ForgeDirection.UP;
				break;
			case EAST:
				this.x = ForgeDirection.SOUTH;
				this.y = ForgeDirection.UP;
				this.z = ForgeDirection.EAST;
				break;
			case WEST:
				this.x = ForgeDirection.NORTH;
				this.y = ForgeDirection.UP;
				this.z = ForgeDirection.WEST;
				break;
			case NORTH:
				this.x = ForgeDirection.WEST;
				this.y = ForgeDirection.UP;
				this.z = ForgeDirection.NORTH;
				break;
			case SOUTH:
				this.x = ForgeDirection.EAST;
				this.y = ForgeDirection.UP;
				this.z = ForgeDirection.SOUTH;
				break;
			case UNKNOWN:
			default:
				this.x = ForgeDirection.EAST;
				this.y = ForgeDirection.UP;
				this.z = ForgeDirection.SOUTH;
				break;
		}
	}

	/**
	 * pretty much useless...
	 */
	public Entity getEntity()
	{
		return this.entity;
	}

	@Override
	public void addBox( double minX, double minY, double minZ, double maxX, double maxY, double maxZ )
	{
		minX /= 16.0;
		minY /= 16.0;
		minZ /= 16.0;
		maxX /= 16.0;
		maxY /= 16.0;
		maxZ /= 16.0;

		double aX = minX * this.x.offsetX + minY * this.y.offsetX + minZ * this.z.offsetX;
		double aY = minX * this.x.offsetY + minY * this.y.offsetY + minZ * this.z.offsetY;
		double aZ = minX * this.x.offsetZ + minY * this.y.offsetZ + minZ * this.z.offsetZ;

		double bX = maxX * this.x.offsetX + maxY * this.y.offsetX + maxZ * this.z.offsetX;
		double bY = maxX * this.x.offsetY + maxY * this.y.offsetY + maxZ * this.z.offsetY;
		double bZ = maxX * this.x.offsetZ + maxY * this.y.offsetZ + maxZ * this.z.offsetZ;

		if( this.x.offsetX + this.y.offsetX + this.z.offsetX < 0 )
		{
			aX += 1;
			bX += 1;
		}

		if( this.x.offsetY + this.y.offsetY + this.z.offsetY < 0 )
		{
			aY += 1;
			bY += 1;
		}

		if( this.x.offsetZ + this.y.offsetZ + this.z.offsetZ < 0 )
		{
			aZ += 1;
			bZ += 1;
		}

		minX = Math.min( aX, bX );
		minY = Math.min( aY, bY );
		minZ = Math.min( aZ, bZ );
		maxX = Math.max( aX, bX );
		maxY = Math.max( aY, bY );
		maxZ = Math.max( aZ, bZ );

		this.boxes.add( AxisAlignedBB.getBoundingBox( minX, minY, minZ, maxX, maxY, maxZ ) );
	}

	@Override
	public ForgeDirection getWorldX()
	{
		return this.x;
	}

	@Override
	public ForgeDirection getWorldY()
	{
		return this.y;
	}

	@Override
	public ForgeDirection getWorldZ()
	{
		return this.z;
	}

	@Override
	public boolean isBBCollision()
	{
		return !this.isVisual;
	}
}
