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


import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import appeng.api.AEApi;
import appeng.api.util.WorldCoord;
import appeng.core.stats.Achievements;
import appeng.util.Platform;


public class StorageHelper
{

	private static StorageHelper instance;
	Method onEntityRemoved;

	public static StorageHelper getInstance()
	{
		if( instance == null )
			instance = new StorageHelper();
		return instance;
	}

	/**
	 * Mostly from dimensional doors.. which mostly got it form X-Comp.
	 *
	 * @param entity to be teleported entity
	 * @param link   destination
	 *
	 * @return teleported entity
	 */
	public Entity teleportEntity( Entity entity, TelDestination link )
	{
		WorldServer oldWorld;
		WorldServer newWorld;
		EntityPlayerMP player;

		try
		{
			oldWorld = (WorldServer) entity.worldObj;
			newWorld = (WorldServer) link.dim;
			player = ( entity instanceof EntityPlayerMP ) ? (EntityPlayerMP) entity : null;
		}
		catch( Throwable e )
		{
			return entity;
		}

		if( oldWorld == null )
			return entity;
		if( newWorld == null )
			return entity;

		// Is something riding? Handle it first.
		if( entity.riddenByEntity != null )
		{
			return this.teleportEntity( entity.riddenByEntity, link );
		}
		// Are we riding something? Dismount and tell the mount to go first.
		Entity cart = entity.ridingEntity;
		if( cart != null )
		{
			entity.mountEntity( null );
			cart = this.teleportEntity( cart, link );
			// We keep track of both so we can remount them on the other side.
		}

		// load the chunk!
		WorldServer.class.cast( newWorld ).getChunkProvider().loadChunk( MathHelper.floor_double( link.x ) >> 4, MathHelper.floor_double( link.z ) >> 4 );

		boolean diffDestination = newWorld != oldWorld;
		if( diffDestination )
		{
			if( player != null )
			{
				if( link.dim.provider instanceof StorageWorldProvider )
					Achievements.SpatialIOExplorer.addToPlayer( player );

				player.mcServer.getConfigurationManager().transferPlayerToDimension( player, link.dim.provider.dimensionId, new METeleporter( newWorld, link ) );
			}
			else
			{
				int entX = entity.chunkCoordX;
				int entZ = entity.chunkCoordZ;

				if( ( entity.addedToChunk ) && ( oldWorld.getChunkProvider().chunkExists( entX, entZ ) ) )
				{
					oldWorld.getChunkFromChunkCoords( entX, entZ ).removeEntity( entity );
					oldWorld.getChunkFromChunkCoords( entX, entZ ).isModified = true;
				}

				Entity newEntity = EntityList.createEntityByName( EntityList.getEntityString( entity ), newWorld );
				if( newEntity != null )
				{
					entity.lastTickPosX = entity.prevPosX = entity.posX = link.x;
					entity.lastTickPosY = entity.prevPosY = entity.posY = link.y;
					entity.lastTickPosZ = entity.prevPosZ = entity.posZ = link.z;

					if( entity instanceof EntityHanging )
					{
						EntityHanging h = (EntityHanging) entity;
						h.field_146063_b += link.xOff;
						h.field_146064_c += link.yOff;
						h.field_146062_d += link.zOff;
					}

					newEntity.copyDataFrom( entity, true );
					newEntity.dimension = newWorld.provider.dimensionId;
					newEntity.forceSpawn = true;

					entity.isDead = true;
					entity = newEntity;
				}
				else
					return null;

				// myChunk.addEntity( entity );
				// newWorld.loadedEntityList.add( entity );
				// newWorld.onEntityAdded( entity );
				newWorld.spawnEntityInWorld( entity );
			}
		}

		entity.worldObj.updateEntityWithOptionalForce( entity, false );

		if( cart != null )
		{
			if( player != null )
				entity.worldObj.updateEntityWithOptionalForce( entity, true );

			entity.mountEntity( cart );
		}

		return entity;
	}

	public void transverseEdges( int minX, int minY, int minZ, int maxX, int maxY, int maxZ, ISpatialVisitor visitor )
	{
		for( int y = minY; y < maxY; y++ )
			for( int z = minZ; z < maxZ; z++ )
			{
				visitor.visit( minX, y, z );
				visitor.visit( maxX, y, z );
			}

		for( int x = minX; x < maxX; x++ )
			for( int z = minZ; z < maxZ; z++ )
			{
				visitor.visit( x, minY, z );
				visitor.visit( x, maxY, z );
			}

		for( int x = minX; x < maxX; x++ )
			for( int y = minY; y < maxY; y++ )
			{
				visitor.visit( x, y, minZ );
				visitor.visit( x, y, maxZ );
			}
	}

	public void swapRegions( World src /** over world **/, World dst /** storage cell **/, int x, int y, int z, int i, int j, int k, int scaleX, int scaleY, int scaleZ )
	{
		for( Block matrixFrameBlock : AEApi.instance().definitions().blocks().matrixFrame().maybeBlock().asSet() )
		{
			this.transverseEdges( i - 1, j - 1, k - 1, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1, new WrapInMatrixFrame( matrixFrameBlock, 0, dst ) );
		}

		AxisAlignedBB srcBox = AxisAlignedBB.getBoundingBox( x, y, z, x + scaleX + 1, y + scaleY + 1, z + scaleZ + 1 );

		AxisAlignedBB dstBox = AxisAlignedBB.getBoundingBox( i, j, k, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1 );

		CachedPlane cDst = new CachedPlane( dst, i, j, k, i + scaleX, j + scaleY, k + scaleZ );
		CachedPlane cSrc = new CachedPlane( src, x, y, z, x + scaleX, y + scaleY, z + scaleZ );

		// do nearly all the work... swaps blocks, tiles, and block ticks
		cSrc.Swap( cDst );

		List<Entity> srcE = src.getEntitiesWithinAABB( Entity.class, srcBox );
		List<Entity> dstE = dst.getEntitiesWithinAABB( Entity.class, dstBox );

		for( Entity e : dstE )
		{
			this.teleportEntity( e, new TelDestination( src, srcBox, e.posX, e.posY, e.posZ, -i + x, -j + y, -k + z ) );
		}

		for( Entity e : srcE )
		{
			this.teleportEntity( e, new TelDestination( dst, dstBox, e.posX, e.posY, e.posZ, -x + i, -y + j, -z + k ) );
		}

		for( WorldCoord wc : cDst.updates )
			cDst.world.notifyBlockOfNeighborChange( wc.x, wc.y, wc.z, Platform.AIR );

		for( WorldCoord wc : cSrc.updates )
			cSrc.world.notifyBlockOfNeighborChange( wc.x, wc.y, wc.z, Platform.AIR );

		this.transverseEdges( x - 1, y - 1, z - 1, x + scaleX + 1, y + scaleY + 1, z + scaleZ + 1, new TriggerUpdates( src ) );
		this.transverseEdges( i - 1, j - 1, k - 1, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1, new TriggerUpdates( dst ) );

		this.transverseEdges( x, y, z, x + scaleX, y + scaleY, z + scaleZ, new TriggerUpdates( src ) );
		this.transverseEdges( i, j, k, i + scaleX, j + scaleY, k + scaleZ, new TriggerUpdates( dst ) );

		/*
		 * IChunkProvider cp = destination.getChunkProvider(); if ( cp instanceof ChunkProviderServer ) { ChunkProviderServer
		 * srv = (ChunkProviderServer) cp; srv.unloadAllChunks(); }
		 *
		 * cp.unloadQueuedChunks();
		 */

	}

	static class TriggerUpdates implements ISpatialVisitor
	{

		final World dst;

		public TriggerUpdates( World dst2 )
		{
			this.dst = dst2;
		}

		@Override
		public void visit( int x, int y, int z )
		{
			Block blk = this.dst.getBlock( x, y, z );
			blk.onNeighborBlockChange( this.dst, x, y, z, Platform.AIR );
		}
	}


	static class WrapInMatrixFrame implements ISpatialVisitor
	{

		final World dst;
		final Block blkID;
		final int Meta;

		public WrapInMatrixFrame( Block blockID, int metaData, World dst2 )
		{
			this.dst = dst2;
			this.blkID = blockID;
			this.Meta = metaData;
		}

		@Override
		public void visit( int x, int y, int z )
		{
			this.dst.setBlock( x, y, z, this.blkID, this.Meta, 3 );
		}
	}


	static class TelDestination
	{

		final World dim;
		final double x;
		final double y;
		final double z;
		final int xOff;
		final int yOff;
		final int zOff;

		TelDestination( World _dim, AxisAlignedBB srcBox, double _x, double _y, double _z, int tileX, int tileY, int tileZ )
		{
			this.dim = _dim;
			this.x = Math.min( srcBox.maxX - 0.5, Math.max( srcBox.minX + 0.5, _x + tileX ) );
			this.y = Math.min( srcBox.maxY - 0.5, Math.max( srcBox.minY + 0.5, _y + tileY ) );
			this.z = Math.min( srcBox.maxZ - 0.5, Math.max( srcBox.minZ + 0.5, _z + tileZ ) );
			this.xOff = tileX;
			this.yOff = tileY;
			this.zOff = tileZ;
		}
	}


	static class METeleporter extends Teleporter
	{

		final TelDestination destination;

		public METeleporter( WorldServer par1WorldServer, TelDestination d )
		{
			super( par1WorldServer );
			this.destination = d;
		}

		@Override
		public void placeInPortal( Entity par1Entity, double par2, double par4, double par6, float par8 )
		{
			par1Entity.setLocationAndAngles( this.destination.x, this.destination.y, this.destination.z, par1Entity.rotationYaw, 0.0F );
			par1Entity.motionX = par1Entity.motionY = par1Entity.motionZ = 0.0D;
		}

		@Override
		public boolean placeInExistingPortal( Entity par1Entity, double par2, double par4, double par6, float par8 )
		{
			return false;
		}

		@Override
		public boolean makePortal( Entity par1Entity )
		{
			return false;
		}

		@Override
		public void removeStalePortalLocations( long par1 )
		{

		}
	}
}
