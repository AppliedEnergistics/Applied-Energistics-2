/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.capabilities;


import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.storage.ISpatialDimension;


class NullSpatialDimension implements ISpatialDimension
{
	@Override
	public int createNewCellDimension( BlockPos size, int owner )
	{
		return -1;
	}

	@Override
	public void deleteCellDimension( int cellStorageId )
	{
	}

	@Override
	public int getCellDimensionOwner( int cellStorageId )
	{
		return -1;
	}

	@Override
	public BlockPos getCellDimensionOrigin( int cellStorageId )
	{
		return null;
	}

	@Override
	public World getWorld()
	{
		return null;
	}

	@Override
	public boolean isCellDimension( int cellDimID )
	{
		return false;
	}

	@Override
	public BlockPos getCellContentSize( int cellDimId )
	{
		return null;
	}
}
