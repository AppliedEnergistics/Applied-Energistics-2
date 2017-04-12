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


import appeng.api.parts.IPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;


public class ContainerOpenContext
{

	private final boolean isItem;
	private World w;
	private int x;
	private int y;
	private int z;
	private ForgeDirection side;

	public ContainerOpenContext( final Object myItem )
	{
		final boolean isWorld = myItem instanceof IPart || myItem instanceof TileEntity;
		this.isItem = !isWorld;
	}

	public TileEntity getTile()
	{
		if( this.isItem )
		{
			return null;
		}
		return this.getWorld().getTileEntity( this.getX(), this.getY(), this.getZ() );
	}

	public ForgeDirection getSide()
	{
		return this.side;
	}

	public void setSide( final ForgeDirection side )
	{
		this.side = side;
	}

	private int getZ()
	{
		return this.z;
	}

	public void setZ( final int z )
	{
		this.z = z;
	}

	private int getY()
	{
		return this.y;
	}

	public void setY( final int y )
	{
		this.y = y;
	}

	private int getX()
	{
		return this.x;
	}

	public void setX( final int x )
	{
		this.x = x;
	}

	private World getWorld()
	{
		return this.w;
	}

	public void setWorld( final World w )
	{
		this.w = w;
	}
}
