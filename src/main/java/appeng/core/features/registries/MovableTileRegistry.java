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

package appeng.core.features.registries;


import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import appeng.api.exceptions.AppEngException;
import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.movable.IMovableTile;
import appeng.spatial.DefaultSpatialHandler;


public class MovableTileRegistry implements IMovableRegistry
{

	private final HashSet<Block> blacklisted = new HashSet<Block>();

	private final HashMap<Class<? extends TileEntity>, IMovableHandler> Valid = new HashMap<Class<? extends TileEntity>, IMovableHandler>();
	private final LinkedList<Class<? extends TileEntity>> test = new LinkedList<Class<? extends TileEntity>>();
	private final LinkedList<IMovableHandler> handlers = new LinkedList<IMovableHandler>();
	private final DefaultSpatialHandler dsh = new DefaultSpatialHandler();

	private final IMovableHandler nullHandler = new DefaultSpatialHandler();

	@Override
	public void blacklistBlock( Block blk )
	{
		this.blacklisted.add( blk );
	}

	@Override
	public void whiteListTileEntity( Class<? extends TileEntity> c )
	{

		if( c.getName().equals( TileEntity.class.getName() ) )
		{
			throw new RuntimeException( new AppEngException( "Someone tried to make all tiles movable, this is a clear violation of the purpose of the white list." ) );
		}

		this.test.add( c );
	}

	@Override
	public boolean askToMove( TileEntity te )
	{
		Class myClass = te.getClass();
		IMovableHandler canMove = this.Valid.get( myClass );

		if( canMove == null )
			canMove = this.testClass( myClass, te );

		if( canMove != this.nullHandler )
		{
			if( te instanceof IMovableTile )
				( (IMovableTile) te ).prepareToMove();

			te.invalidate();
			return true;
		}

		return false;
	}

	private IMovableHandler testClass( Class myClass, TileEntity te )
	{
		IMovableHandler handler = null;

		// ask handlers...
		for( IMovableHandler han : this.handlers )
		{
			if( han.canHandle( myClass, te ) )
			{
				handler = han;
				break;
			}
		}

		// if you have a handler your opted in
		if( handler != null )
		{
			this.Valid.put( myClass, handler );
			return handler;
		}

		// if your movable our opted in
		if( te instanceof IMovableTile )
		{
			this.Valid.put( myClass, this.dsh );
			return this.dsh;
		}

		// if you are on the white list your opted in.
		for( Class<? extends TileEntity> testClass : this.test )
		{
			if( testClass.isAssignableFrom( myClass ) )
			{
				this.Valid.put( myClass, this.dsh );
				return this.dsh;
			}
		}

		this.Valid.put( myClass, this.nullHandler );
		return this.nullHandler;
	}

	@Override
	public void doneMoving( TileEntity te )
	{
		if( te instanceof IMovableTile )
		{
			IMovableTile mt = (IMovableTile) te;
			mt.doneMoving();
		}
	}

	@Override
	public void addHandler( IMovableHandler han )
	{
		this.handlers.add( han );
	}

	@Override
	public IMovableHandler getHandler( TileEntity te )
	{
		Class myClass = te.getClass();
		IMovableHandler h = this.Valid.get( myClass );
		return h == null ? this.dsh : h;
	}

	@Override
	public IMovableHandler getDefaultHandler()
	{
		return this.dsh;
	}

	@Override
	public boolean isBlacklisted( Block blk )
	{
		return this.blacklisted.contains( blk );
	}
}
