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

package appeng.helpers;


import appeng.api.util.IOrientable;
import net.minecraftforge.common.util.ForgeDirection;


public class NullRotation implements IOrientable
{

	public NullRotation()
	{

	}

	@Override
	public boolean canBeRotated()
	{
		return false;
	}

	@Override
	public ForgeDirection getForward()
	{
		return ForgeDirection.SOUTH;
	}

	@Override
	public ForgeDirection getUp()
	{
		return ForgeDirection.UP;
	}

	@Override
	public void setOrientation( final ForgeDirection forward, final ForgeDirection up )
	{

	}
}
