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

package appeng.api.util;


import net.minecraft.util.math.AxisAlignedBB;


/**
 * Mutable stand in for Axis Aligned BB, this was used to prevent GC Thrashing.. Related code could also be re-written.
 *
 * TODO: Replace with interface and maybe factory should it ever be needed for addons.
 */
public class AEAxisAlignedBB
{
	public double minX;
	public double minY;
	public double minZ;
	public double maxX;
	public double maxY;
	public double maxZ;

	public AxisAlignedBB getBoundingBox()
	{
		return new AxisAlignedBB( this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ );
	}

	public AEAxisAlignedBB( final double a, final double b, final double c, final double d, final double e, final double f )
	{
		this.minX = a;
		this.minY = b;
		this.minZ = c;
		this.maxX = d;
		this.maxY = e;
		this.maxZ = f;
	}

	public static AEAxisAlignedBB fromBounds( final double a, final double b, final double c, final double d, final double e, final double f )
	{
		return new AEAxisAlignedBB( a, b, c, d, e, f );
	}

	public static AEAxisAlignedBB fromBounds( final AxisAlignedBB bb )
	{
		return new AEAxisAlignedBB( bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ );
	}
}
