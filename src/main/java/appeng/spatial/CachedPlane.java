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
import appeng.api.movable.IMovableHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.core.WorldSettings;
import appeng.util.Platform;

public class CachedPlane
{

	class Column
	{

		private final int x;
		private final int z;
		private final Chunk c;
		private final Object ch[] = { 0, 0, 0 };
		private List<Integer> skipThese = null;

		private ExtendedBlockStorage[] storage;

		public Column(Chunk _c, int _x, int _z, int cy, int y_clen) {
			x = _x;
			z = _z;
			c = _c;
			storage = c.getBlockStorageArray();

			// make sure storage exists before hand...
			for (int ay = 0; ay < y_clen; ay++)
			{
				int by = (ay + cy);
				ExtendedBlockStorage extendedblockstorage = storage[by];
				if ( extendedblockstorage == null )
					extendedblockstorage = storage[by] = new ExtendedBlockStorage( by << 4, !c.worldObj.provider.hasNoSky );
			}
		}

		public void setBlockIDWithMetadata(int y, Object[] blk)
		{
			if ( blk[0] == matrixFrame )
				blk[0] = Platform.air;

			ExtendedBlockStorage extendedblockstorage = storage[y >> 4];
			extendedblockstorage.func_150818_a( x, y & 15, z, (Block) blk[0] );
			// extendedblockstorage.setExtBlockID( x, y & 15, z, blk[0] );
			extendedblockstorage.setExtBlockMetadata( x, y & 15, z, (Integer) blk[1] );
			extendedblockstorage.setExtBlocklightValue( x, y & 15, z, (Integer) blk[2] );
		}

		public Object[] getDetails(int y)
		{
			ExtendedBlockStorage extendedblockstorage = storage[y >> 4];
			ch[0] = extendedblockstorage.getBlockByExtId( x, y & 15, z );
			ch[1] = extendedblockstorage.getExtBlockMetadata( x, y & 15, z );
			ch[2] = extendedblockstorage.getExtBlocklightValue( x, y & 15, z );
			return ch;
		}

		public boolean dontSkip(int y)
		{
			ExtendedBlockStorage extendedblockstorage = storage[y >> 4];
			if ( reg.isBlacklisted( extendedblockstorage.getBlockByExtId( x, y & 15, z ) ) )
				return false;

			return skipThese == null ? true : !skipThese.contains( y );
		}

		public void setSkip(int yCoord)
		{
			if ( skipThese == null )
				skipThese = new LinkedList<Integer>();
			skipThese.add( yCoord );
		}

	};

	int verticalBits;

	int x_size;
	int z_size;

	int cx_size;
	int cz_size;

	int x_offset;
	int y_offset;
	int z_offset;

	int y_size;

	Chunk myChunks[][];
	Column myColumns[][];

	LinkedList<TileEntity> tiles = new LinkedList<TileEntity>();
	LinkedList<NextTickListEntry> ticks = new LinkedList<NextTickListEntry>();

	World world;
	Block matrixFrame = AEApi.instance().blocks().blockMatrixFrame.block();
	IMovableRegistry reg = AEApi.instance().registries().movable();

	LinkedList<WorldCoord> updates = new LinkedList<WorldCoord>();

	public CachedPlane(World w, int minx, int miny, int minz, int maxx, int maxy, int maxz) {

		world = w;

		x_size = maxx - minx + 1;
		y_size = maxy - miny + 1;
		z_size = maxz - minz + 1;

		x_offset = minx;
		y_offset = miny;
		z_offset = minz;

		int minCX = minx >> 4;
		int minCY = miny >> 4;
		int minCZ = minz >> 4;
		int maxCX = maxx >> 4;
		int maxCY = maxy >> 4;
		int maxCZ = maxz >> 4;

		cx_size = maxCX - minCX + 1;
		int cy_size = maxCY - minCY + 1;
		cz_size = maxCZ - minCZ + 1;

		myChunks = new Chunk[cx_size][cz_size];
		myColumns = new Column[x_size][z_size];

		verticalBits = 0;
		for (int cy = 0; cy < cy_size; cy++)
		{
			verticalBits |= 1 << (minCY + cy);
		}

		for (int x = 0; x < x_size; x++)
			for (int z = 0; z < z_size; z++)
			{
				myColumns[x][z] = new Column( w.getChunkFromChunkCoords( (minx + x) >> 4, (minz + z) >> 4 ), (minx + x) & 0xF, (minz + z) & 0xF, minCY, cy_size );
			}

		IMovableRegistry mr = AEApi.instance().registries().movable();

		for (int cx = 0; cx < cx_size; cx++)
			for (int cz = 0; cz < cz_size; cz++)
			{
				LinkedList<Entry<ChunkPosition, TileEntity>> rwarTiles = new LinkedList();
				LinkedList<ChunkPosition> deadTiles = new LinkedList<ChunkPosition>();

				Chunk c = w.getChunkFromChunkCoords( minCX + cx, minCZ + cz );
				myChunks[cx][cz] = c;

				rwarTiles.addAll( ((HashMap<ChunkPosition, TileEntity>) c.chunkTileEntityMap).entrySet() );
				for (Entry<ChunkPosition, TileEntity> tx : rwarTiles)
				{
					ChunkPosition cp = tx.getKey();
					TileEntity te = tx.getValue();
					if ( te.xCoord >= minx && te.xCoord <= maxx && te.yCoord >= miny && te.yCoord <= maxy && te.zCoord >= minz && te.zCoord <= maxz )
					{
						if ( mr.askToMove( te ) )
						{
							tiles.add( te );
							deadTiles.add( cp );
						}
						else
						{
							Object[] details = myColumns[te.xCoord - minx][te.zCoord - minz].getDetails( te.yCoord );
							Block blk = (Block) details[0];

							// don't skip air, juset let the code replace it...
							if ( blk != null && blk.isAir( c.worldObj, te.xCoord, te.yCoord, te.zCoord )
									&& blk.isReplaceable( c.worldObj, te.xCoord, te.yCoord, te.zCoord ) )
							{
								c.worldObj.setBlock( te.xCoord, te.yCoord, te.zCoord, Platform.air );
								c.worldObj.notifyBlocksOfNeighborChange( te.xCoord, te.yCoord, te.zCoord, Platform.air );
							}
							else
								myColumns[te.xCoord - minx][te.zCoord - minz].setSkip( te.yCoord );
						}
					}
				}

				for (ChunkPosition cp : deadTiles)
				{
					c.chunkTileEntityMap.remove( cp );
				}

				long k = world.getTotalWorldTime();
				List list = world.getPendingBlockUpdates( c, false );
				if ( list != null )
				{
					for (Object o : list)
					{
						NextTickListEntry ntle = (NextTickListEntry) o;
						if ( ntle.xCoord >= minx && ntle.xCoord <= maxx && ntle.yCoord >= miny && ntle.yCoord <= maxy && ntle.zCoord >= minz
								&& ntle.zCoord <= maxz )
						{
							NextTickListEntry newEntry = new NextTickListEntry( ntle.xCoord, ntle.yCoord, ntle.zCoord, ntle.func_151351_a() );
							newEntry.scheduledTime = ntle.scheduledTime - k;
							ticks.add( newEntry );
						}
					}
				}

			}

		for (TileEntity te : tiles)
		{
			try
			{
				world.loadedTileEntityList.remove( te );
			}
			catch (Exception e)
			{
				AELog.error( e );
			}
		}
	}

	private IMovableHandler getHandler(TileEntity te)
	{
		IMovableRegistry mr = AEApi.instance().registries().movable();
		return mr.getHandler( te );
	}

	void Swap(CachedPlane dst)
	{
		IMovableRegistry mr = AEApi.instance().registries().movable();

		if ( dst.x_size == x_size && dst.y_size == y_size && dst.z_size == z_size )
		{
			AELog.info( "Block Copy Scale: " + x_size + ", " + y_size + ", " + z_size );

			long startTime = System.nanoTime();

			for (int x = 0; x < x_size; x++)
			{
				for (int z = 0; z < z_size; z++)
				{
					Column a = myColumns[x][z];
					Column b = dst.myColumns[x][z];

					for (int y = 0; y < y_size; y++)
					{
						int src_y = y + y_offset;
						int dst_y = y + dst.y_offset;

						if ( a.dontSkip( src_y ) && b.dontSkip( dst_y ) )
						{
							Object[] aD = a.getDetails( src_y );
							Object[] bD = b.getDetails( dst_y );

							a.setBlockIDWithMetadata( src_y, bD );
							b.setBlockIDWithMetadata( dst_y, aD );
						}
						else
						{
							markForUpdate( x + x_offset, src_y, z + z_offset );
							dst.markForUpdate( x + dst.x_offset, dst_y, z + dst.z_offset );
						}
					}

				}
			}

			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			AELog.info( "Block Copy Time: " + duration );

			for (TileEntity te : tiles)
			{
				dst.addTile( te.xCoord - x_offset, te.yCoord - y_offset, te.zCoord - z_offset, te, this, mr );
			}

			for (TileEntity te : dst.tiles)
			{
				addTile( te.xCoord - dst.x_offset, te.yCoord - dst.y_offset, te.zCoord - dst.z_offset, te, dst, mr );
			}

			for (NextTickListEntry ntle : ticks)
			{
				dst.addTick( ntle.xCoord - x_offset, ntle.yCoord - y_offset, ntle.zCoord - z_offset, ntle );
			}

			for (NextTickListEntry ntle : dst.ticks)
			{
				addTick( ntle.xCoord - dst.x_offset, ntle.yCoord - dst.y_offset, ntle.zCoord - dst.z_offset, ntle );
			}

			startTime = System.nanoTime();
			updateChunks();
			dst.updateChunks();
			endTime = System.nanoTime();

			duration = endTime - startTime;
			AELog.info( "Update Time: " + duration );
		}
	}

	private void markForUpdate(int src_x, int src_y, int src_z)
	{
		updates.add( new WorldCoord( src_x, src_y, src_z ) );
		for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS)
			updates.add( new WorldCoord( src_x + d.offsetX, src_y + d.offsetY, src_z + d.offsetZ ) );
	}

	private void addTick(int x, int y, int z, NextTickListEntry ntle)
	{
		world.scheduleBlockUpdate( x + x_offset, y + y_offset, z + z_offset, ntle.func_151351_a(), (int) ntle.scheduledTime );
	}

	private void addTile(int x, int y, int z, TileEntity te, CachedPlane alternateDest, IMovableRegistry mr)
	{
		try
		{
			Column c = myColumns[x][z];

			if ( c.dontSkip( y + y_offset ) || alternateDest == null )
			{
				IMovableHandler handler = getHandler( te );

				try
				{
					handler.moveTile( te, world, x + x_offset, y + y_offset, z + z_offset );
				}
				catch (Throwable e)
				{
					AELog.error( e );

					// attempt recovery...
					te.setWorldObj( world );
					te.xCoord = x;
					te.yCoord = y;
					te.zCoord = z;

					c.c.func_150812_a( c.x, y + y, c.z, te );
					// c.c.setChunkTileEntity( c.x, y + y, c.z, te );

					if ( c.c.isChunkLoaded )
					{
						world.addTileEntity( te );
						world.markBlockForUpdate( x, y, z );
					}
				}

				mr.doneMoving( te );
			}
			else
			{
				alternateDest.addTile( x, y, z, te, null, mr );
			}
		}
		catch (Throwable e)
		{
			AELog.error( e );
		}
	}

	private void updateChunks()
	{

		// update shit..
		for (int x = 0; x < cx_size; x++)
			for (int z = 0; z < cz_size; z++)
			{
				Chunk c = myChunks[x][z];
				c.resetRelightChecks();
				c.generateSkylightMap();
				c.isModified = true;
			}

		// send shit...
		for (int x = 0; x < cx_size; x++)
			for (int z = 0; z < cz_size; z++)
			{

				Chunk c = myChunks[x][z];

				for (int y = 1; y < 255; y += 32)
					WorldSettings.getInstance().getCompass().updateArea( world, c.xPosition << 4, y, c.zPosition << 4 );

				Platform.sendChunk( c, verticalBits );

			}

	}

}
