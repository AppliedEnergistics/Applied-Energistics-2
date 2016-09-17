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


import net.minecraft.util.EnumFacing;


/**
 * Stand in for previous Forge Direction, Several uses of this class are simply legacy where as some uses of this class
 * are intended.
 */
public enum AEPartLocation
{
	/**
	 * Negative Y
	 */
	DOWN( 0, -1, 0 ),

	/**
	 * Positive Y
	 */
	UP( 0, 1, 0 ),

	/**
	 * Negative Z
	 */
	NORTH( 0, 0, -1 ),

	/**
	 * Positive Z
	 */
	SOUTH( 0, 0, 1 ),

	/**
	 * Negative X
	 */
	WEST( -1, 0, 0 ),

	/**
	 * Posative X
	 */
	EAST( 1, 0, 0 ),

	/**
	 * Center or inside of the block.
	 */
	INTERNAL( 0, 0, 0 );

	public final int xOffset;
	public final int yOffset;
	public final int zOffset;

	private static final EnumFacing[] facings = { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, null };
	private static final int[] OPPOSITES = { 1, 0, 3, 2, 5, 4, 6 };

	public static final AEPartLocation[] SIDE_LOCATIONS = { DOWN, UP, NORTH, SOUTH, WEST, EAST };

	private AEPartLocation( final int x, final int y, final int z )
	{
		this.xOffset = x;
		this.yOffset = y;
		this.zOffset = z;
	}

	/**
	 * @return Part Location
	 */
	public static AEPartLocation fromOrdinal( final int id )
	{
		if( id >= 0 && id < SIDE_LOCATIONS.length )
		{
			return SIDE_LOCATIONS[id];
		}

		return INTERNAL;
	}

	/**
	 * 100% chance of success.
	 *
	 * @param side
	 * @return proper Part Location for a facing enum.
	 */
	public static AEPartLocation fromFacing( final EnumFacing side )
	{
		if( side == null )
		{
			return INTERNAL;
		}
		return values()[side.ordinal()];
	}

	/**
	 * @return Opposite Part Location, INTERNAL remains INTERNAL.
	 */
	public AEPartLocation getOpposite()
	{
		return fromOrdinal( OPPOSITES[this.ordinal()] );
	}

	/**
	 * @return EnumFacing Equivalence, if Center returns null.
	 */
	public EnumFacing getFacing()
	{
		return facings[this.ordinal()];
	}

}
