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

package appeng.tile.events;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.tile.AEBaseTile;


public class AETileEventHandler
{

	private final Method method;

	public AETileEventHandler( Method m, TileEventType which )
	{
		this.method = m;
	}

	// TICK
	public void Tick( AEBaseTile tile )
	{
		try
		{
			this.method.invoke( tile );
		}
		catch ( IllegalAccessException e )
		{
			throw new RuntimeException( e );
		}
		catch ( IllegalArgumentException e )
		{
			throw new RuntimeException( e );
		}
		catch ( InvocationTargetException e )
		{
			throw new RuntimeException( e );
		}
	}

	// WORLD_NBT
	public void writeToNBT( AEBaseTile tile, NBTTagCompound data )
	{
		try
		{
			this.method.invoke( tile, data );
		}
		catch ( IllegalAccessException e )
		{
			throw new RuntimeException( e );
		}
		catch ( IllegalArgumentException e )
		{
			throw new RuntimeException( e );
		}
		catch ( InvocationTargetException e )
		{
			throw new RuntimeException( e );
		}
	}

	// WORLD NBT
	public void readFromNBT( AEBaseTile tile, NBTTagCompound data )
	{
		try
		{
			this.method.invoke( tile, data );
		}
		catch ( IllegalAccessException e )
		{
			throw new RuntimeException( e );
		}
		catch ( IllegalArgumentException e )
		{
			throw new RuntimeException( e );
		}
		catch ( InvocationTargetException e )
		{
			throw new RuntimeException( e );
		}
	}

	// NETWORK
	public void writeToStream( AEBaseTile tile, ByteBuf data )
	{
		try
		{
			this.method.invoke( tile, data );
		}
		catch ( IllegalAccessException e )
		{
			throw new RuntimeException( e );
		}
		catch ( IllegalArgumentException e )
		{
			throw new RuntimeException( e );
		}
		catch ( InvocationTargetException e )
		{
			throw new RuntimeException( e );
		}
	}

	// NETWORK
	/**
	 * returning true from this method, will update the block's render
	 *
	 * @param data data of stream
	 * @return true of method could be invoked
	 */
	@SideOnly( Side.CLIENT )
	public boolean readFromStream( AEBaseTile tile, ByteBuf data )
	{
		try
		{
			return ( Boolean ) this.method.invoke( tile, data );
		}
		catch ( IllegalAccessException e )
		{
			throw new RuntimeException( e );
		}
		catch ( IllegalArgumentException e )
		{
			throw new RuntimeException( e );
		}
		catch ( InvocationTargetException e )
		{
			throw new RuntimeException( e );
		}
	}

}
