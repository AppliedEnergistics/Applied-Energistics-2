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

package appeng.spatial;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.WorldSettings;
import appeng.util.Platform;


public class CachedPlane
{
	final int x_size;
	final int z_size;
	final int cx_size;
	final int cz_size;
	final int x_offset;
	final int y_offset;
	final int z_offset;
	final int y_size;
	final Chunk[][] myChunks;
	final Column[][] myColumns;
	final LinkedList<TileEntity> tiles = new LinkedList<TileEntity>();
	final LinkedList<NextTickListEntry> ticks = new LinkedList<NextTickListEntry>();
	final World world;
	final IMovableRegistry reg = AEApi.instance().registries().movable();
	final LinkedList<WorldCoord> updates = new LinkedList<WorldCoord>();
	private final IBlockDefinition matrixFrame = AEApi.instance().definitions().blocks().matrixFrame();
	int verticalBits;

	public CachedPlane( World w, int minX, int minY, int minZ, int maxX, int maxY, int maxZ )
	{

		this.world = w;

		this.x_size = maxX - minX + 1;
		this.y_size = maxY - minY + 1;
		this.z_size = maxZ - minZ + 1;

		this.x_offset = minX;
		this.y_offset = minY;
		this.z_offset = minZ;

		int minCX = minX >> 4;
		int minCY = minY >> 4;
		int minCZ = minZ >> 4;
		int maxCX = maxX >> 4;
		int maxCY = maxY >> 4;
		int maxCZ = maxZ >> 4;

		this.cx_size = maxCX - minCX + 1;
		int cy_size = maxCY - minCY + 1;
		this.cz_size = maxCZ - minCZ + 1;

		this.myChunks = new Chunk[this.cx_size][this.cz_size];
		this.myColumns = new Column[this.x_size][this.z_size];

		this.verticalBits = 0;
		for( int cy = 0; cy < cy_size; cy++ )
		{
			this.verticalBits |= 1 << ( minCY + cy );
		}

		for( int x = 0; x < this.x_size; x++ )
		{
			for( int z = 0; z < this.z_size; z++ )
			{
				this.myColumns[x][z] = new Column( w.getChunkFromChunkCoords( ( minX + x ) >> 4, ( minZ + z ) >> 4 ), ( minX + x ) & 0xF, ( minZ + z ) & 0xF, minCY, cy_size );
			}
		}

		IMovableRegistry mr = AEApi.instance().registries().movable();

		for( int cx = 0; cx < this.cx_size; cx++ )
		{
			for( int cz = 0; cz < this.cz_size; cz++ )
			{
				LinkedList<Entry<ChunkPosition, TileEntity>> rawTiles = new LinkedList<Entry<ChunkPosition, TileEntity>>();
				LinkedList<ChunkPosition> deadTiles = new LinkedList<ChunkPosition>();

				Chunk c = w.getChunkFromChunkCoords( minCX + cx, minCZ + cz );
				this.myChunks[cx][cz] = c;

				rawTiles.addAll( ( (HashMap<ChunkPosition, TileEntity>) c.chunkTileEntityMap ).entrySet() );
				for( Entry<ChunkPosition, TileEntity> tx : rawTiles )
				{
					ChunkPosition cp = tx.getKey();
					TileEntity te = tx.getValue();
					if( te.xCoord >= minX && te.xCoord <= maxX && te.yCoord >= minY && te.yCoord <= maxY && te.zCoord >= minZ && te.zCoord <= maxZ )
					{
						if( mr.askToMove( te ) )
						{
							this.tiles.add( te );
							deadTiles.add( cp );
						}
						else
						{
							Object[] details = this.myColumns[te.xCoord - minX][te.zCoord - minZ].getDetails( te.yCoord );
							Block blk = (Block) details[0];

							// don't skip air, just let the code replace it...
							if( blk != null && blk.isAir( c.worldObj, te.xCoord, te.yCoord, te.zCoord ) && blk.isReplaceable( c.worldObj, te.xCoord, te.yCoord, te.zCoord ) )
							{
								c.worldObj.setBlock( te.xCoord, te.yCoord, te.zCoord, Platform.AIR_BLOCK );
								c.worldObj.notifyBlocksOfNeighborChange( te.xCoord, te.yCoord, te.zCoord, Platform.AIR_BLOCK );
							}
							else
							{
								this.myColumns[te.xCoord - minX][te.zCoord - minZ].setSkip( te.yCoord );
							}
						}
					}
				}

				for( ChunkPosition cp : deadTiles )
				{
					c.chunkTileEntityMap.remove( cp );
				}

				long k = this.world.getTotalWorldTime();
				List list = this.world.getPendingBlockUpdates( c, false );
				if( list != null )
				{
					for( Object o : list )
					{
						NextTickListEntry entry = (NextTickListEntry) o;
						if( entry.xCoord >= minX && entry.xCoord <= maxX && entry.yCoord >= minY && entry.yCoord <= maxY && entry.zCoord >= minZ && entry.zCoord <= maxZ )
						{
							NextTickListEntry newEntry = new NextTickListEntry( entry.xCoord, entry.yCoord, entry.zCoord, entry.func_151351_a() );
							newEntry.scheduledTime = entry.scheduledTime - k;
							this.ticks.add( newEntry );
						}
					}
				}
			}
		}

		for( TileEntity te : this.tiles )
		{
			try
			{
				this.world.loadedTileEntityList.remove( te );
			}
			catch( Exception e )
			{
				AELog.error( e );
			}
		}
	}

	private IMovableHandler getHandler( TileEntity te )
	{
		IMovableRegistry mr = AEApi.instance().registries().movable();
		return mr.getHandler( te );
	}

	void Swap( CachedPlane dst )
	{
		IMovableRegistry mr = AEApi.instance().registries().movable();

		if( dst.x_size == this.x_size && dst.y_size == this.y_size && dst.z_size == this.z_size )
		{
			AELog.info( "Block Copy Scale: " + this.x_size + ", " + this.y_size + ", " + this.z_size );

			long startTime = System.nanoTime();

			for( int x = 0; x < this.x_size; x++ )
			{
				for( int z = 0; z < this.z_size; z++ )
				{
					Column a = this.myColumns[x][z];
					Column b = dst.myColumns[x][z];

					for( int y = 0; y < this.y_size; y++ )
					{
						int src_y = y + this.y_offset;
						int dst_y = y + dst.y_offset;

						if( a.doNotSkip( src_y ) && b.doNotSkip( dst_y ) )
						{
							Object[] aD = a.getDetails( src_y );
							Object[] bD = b.getDetails( dst_y );

							a.setBlockIDWithMetadata( src_y, bD );
							b.setBlockIDWithMetadata( dst_y, aD );
						}
						else
						{
							this.markForUpdate( x + this.x_offset, src_y, z + this.z_offset );
							dst.markForUpdate( x + dst.x_offset, dst_y, z + dst.z_offset );
						}
					}
				}
			}

			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			AELog.info( "Block Copy Time: " + duration );

			for( TileEntity te : this.tiles )
			{
				dst.addTile( te.xCoord - this.x_offset, te.yCoord - this.y_offset, te.zCoord - this.z_offset, te, this, mr );
			}

			for( TileEntity te : dst.tiles )
			{
				this.addTile( te.xCoord - dst.x_offset, te.yCoord - dst.y_offset, te.zCoord - dst.z_offset, te, dst, mr );
			}

			for( NextTickListEntry entry : this.ticks )
			{
				dst.addTick( entry.xCoord - this.x_offset, entry.yCoord - this.y_offset, entry.zCoord - this.z_offset, entry );
			}

			for( NextTickListEntry entry : dst.ticks )
			{
				this.addTick( entry.xCoord - dst.x_offset, entry.yCoord - dst.y_offset, entry.zCoord - dst.z_offset, entry );
			}

			startTime = System.nanoTime();
			this.updateChunks();
			dst.updateChunks();
			endTime = System.nanoTime();

			duration = endTime - startTime;
			AELog.info( "Update Time: " + duration );
		}
	}

	private void markForUpdate( int x, int y, int z )
	{
		this.updates.add( new WorldCoord( x, y, z ) );
		for( ForgeDirection d : ForgeDirection.VALID_DIRECTIONS )
		{
			this.updates.add( new WorldCoord( x + d.offsetX, y + d.offsetY, z + d.offsetZ ) );
		}
	}

	private void addTick( int x, int y, int z, NextTickListEntry entry )
	{
		this.world.scheduleBlockUpdate( x + this.x_offset, y + this.y_offset, z + this.z_offset, entry.func_151351_a(), (int) entry.scheduledTime );
	}

	private void addTile( int x, int y, int z, TileEntity te, CachedPlane alternateDestination, IMovableRegistry mr )
	{
		try
		{
			Column c = this.myColumns[x][z];

			if( c.doNotSkip( y + this.y_offset ) || alternateDestination == null )
			{
				IMovableHandler handler = this.getHandler( te );

				try
				{
					handler.moveTile( te, this.world, x + this.x_offset, y + this.y_offset, z + this.z_offset );
				}
				catch( Throwable e )
				{
					AELog.error( e );

					// attempt recovery...
					te.setWorldObj( this.world );
					te.xCoord = x;
					te.yCoord = y;
					te.zCoord = z;

					c.c.func_150812_a( c.x, y + y, c.z, te );
					// c.c.setChunkTileEntity( c.x, y + y, c.z, te );

					if( c.c.isChunkLoaded )
					{
						this.world.addTileEntity( te );
						this.world.markBlockForUpdate( x, y, z );
					}
				}

				mr.doneMoving( te );
			}
			else
			{
				alternateDestination.addTile( x, y, z, te, null, mr );
			}
		}
		catch( Throwable e )
		{
			AELog.error( e );
		}
	}

	private void updateChunks()
	{

		// update shit..
		for( int x = 0; x < this.cx_size; x++ )
		{
			for( int z = 0; z < this.cz_size; z++ )
			{
				Chunk c = this.myChunks[x][z];
				c.resetRelightChecks();
				c.generateSkylightMap();
				c.isModified = true;
			}
		}

		// send shit...
		for( int x = 0; x < this.cx_size; x++ )
		{
			for( int z = 0; z < this.cz_size; z++ )
			{

				Chunk c = this.myChunks[x][z];

				for( int y = 1; y < 255; y += 32 )
				{
					WorldSettings.getInstance().getCompass().updateArea( this.world, c.xPosition << 4, y, c.zPosition << 4 );
				}

				Platform.sendChunk( c, this.verticalBits );
			}
		}
	}

	class Column
	{

		private final int x;
		private final int z;
		private final Chunk c;
		private final Object[] ch = { 0, 0, 0 };
		private final ExtendedBlockStorage[] storage;
		private List<Integer> skipThese = null;

		public Column( Chunk chunk, int x, int z, int chunkY, int chunkHeight )
		{
			this.x = x;
			this.z = z;
			this.c = chunk;
			this.storage = this.c.getBlockStorageArray();

			// make sure storage exists before hand...
			for( int ay = 0; ay < chunkHeight; ay++ )
			{
				int by = ( ay + chunkY );
				ExtendedBlockStorage extendedblockstorage = this.storage[by];
				if( extendedblockstorage == null )
				{
					extendedblockstorage = this.storage[by] = new ExtendedBlockStorage( by << 4, !this.c.worldObj.provider.hasNoSky );
				}
			}
		}

		public void setBlockIDWithMetadata( int y, Object[] blk )
		{
			for( Block matrixFrameBlock : CachedPlane.this.matrixFrame.maybeBlock().asSet() )
			{
				if( blk[0] == matrixFrameBlock )
				{
					blk[0] = Platform.AIR_BLOCK;
				}
			}

			ExtendedBlockStorage extendedBlockStorage = this.storage[y >> 4];
			extendedBlockStorage.func_150818_a( this.x, y & 15, this.z, (Block) blk[0] );
			// extendedBlockStorage.setExtBlockID( x, y & 15, z, blk[0] );
			extendedBlockStorage.setExtBlockMetadata( this.x, y & 15, this.z, (Integer) blk[1] );
			extendedBlockStorage.setExtBlocklightValue( this.x, y & 15, this.z, (Integer) blk[2] );
		}

		public Object[] getDetails( int y )
		{
			ExtendedBlockStorage extendedblockstorage = this.storage[y >> 4];
			this.ch[0] = extendedblockstorage.getBlockByExtId( this.x, y & 15, this.z );
			this.ch[1] = extendedblockstorage.getExtBlockMetadata( this.x, y & 15, this.z );
			this.ch[2] = extendedblockstorage.getExtBlocklightValue( this.x, y & 15, this.z );
			return this.ch;
		}

		public boolean doNotSkip( int y )
		{
			ExtendedBlockStorage extendedblockstorage = this.storage[y >> 4];
			if( CachedPlane.this.reg.isBlacklisted( extendedblockstorage.getBlockByExtId( this.x, y & 15, this.z ) ) )
			{
				return false;
			}

			return this.skipThese == null || !this.skipThese.contains( y );
		}

		public void setSkip( int yCoord )
		{
			if( this.skipThese == null )
			{
				this.skipThese = new LinkedList<Integer>();
			}
			this.skipThese.add( yCoord );
		}
	}
}
