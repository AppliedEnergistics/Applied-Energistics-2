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

package appeng.container;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.parts.IPart;


public class ContainerOpenContext
{

	final public boolean isItem;
	public World w;
	public int x;
	public int y;
	public int z;
	public ForgeDirection side;

	public ContainerOpenContext( Object myItem )
	{
		boolean isWorld = myItem instanceof IPart || myItem instanceof TileEntity;
		this.isItem = !isWorld;
	}

	public TileEntity getTile()
	{
		if( this.isItem )
			return null;
		return this.w.getTileEntity( this.x, this.y, this.z );
	}
}
