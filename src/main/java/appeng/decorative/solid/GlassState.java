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

import net.minecraft.util.EnumFacing;


/**
 * Immutable (and thus thread-safe) class that encapsulates the rendering state required for a connected texture
 * glass block.
 */
public final class GlassState
{

	private final int x;
	private final int y;
	private final int z;

	private final EnumSet<EnumFacing> flushWith = EnumSet.noneOf( EnumFacing.class );

	public GlassState( int x, int y, int z, EnumSet<EnumFacing> flushWith )
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.flushWith.addAll( flushWith );
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public boolean isFlushWith( EnumFacing side )
	{
		return flushWith.contains( side );
	}

}
