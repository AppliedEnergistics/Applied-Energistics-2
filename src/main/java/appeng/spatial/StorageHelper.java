/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import appeng.api.AEApi;
import appeng.api.util.WorldCoord;
import appeng.core.stats.Achievements;
import appeng.util.Platform;
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

import java.util.List;


public class StorageHelper
{

	private static StorageHelper instance;

	public static StorageHelper getInstance()
	{
		if( instance == null )
		{
			instance = new StorageHelper();
		}
		return instance;
	}

	/**
	 * Mostly from dimensional doors.. which mostly got it form X-Comp.
	 *
	 * @param entity to be teleported entity
	 * @param link   destination
	 * @return teleported entity
	 */
	private Entity teleportEntity( Entity entity, final TelDestination link )
	{
		final WorldServer oldWorld;
		final WorldServer newWorld;
		final EntityPlayerMP player;

		try
		{
			oldWorld = (WorldServer) entity.worldObj;
			newWorld = (WorldServer) link.dim;
			player = ( entity instanceof EntityPlayerMP ) ? (EntityPlayerMP) entity : null;
		}
		catch( final Throwable e )
		{
			return entity;
		}

		if( oldWorld == null )
		{
			return entity;
		}
		if( newWorld == null )
		{
			return entity;
		}

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

		final boolean diffDestination = newWorld != oldWorld;
		if( diffDestination )
		{
			if( player != null )
			{
				if( link.dim.provider instanceof StorageWorldProvider )
				{
					Achievements.SpatialIOExplorer.addToPlayer( player );
				}

				player.mcServer.getConfigurationManager().transferPlayerToDimension( player, link.dim.provider.dimensionId, new METeleporter( newWorld, link ) );
			}
			else
			{
				final int entX = entity.chunkCoordX;
				final int entZ = entity.chunkCoordZ;

				if( ( entity.addedToChunk ) && ( oldWorld.getChunkProvider().chunkExists( entX, entZ ) ) )
				{
					oldWorld.getChunkFromChunkCoords( entX, entZ ).removeEntity( entity );
					oldWorld.getChunkFromChunkCoords( entX, entZ ).isModified = true;
				}

				final Entity newEntity = EntityList.createEntityByName( EntityList.getEntityString( entity ), newWorld );
				if( newEntity != null )
				{
					entity.lastTickPosX = entity.prevPosX = entity.posX = link.x;
					entity.lastTickPosY = entity.prevPosY = entity.posY = link.y;
					entity.lastTickPosZ = entity.prevPosZ = entity.posZ = link.z;

					if( entity instanceof EntityHanging )
					{
						final EntityHanging h = (EntityHanging) entity;
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
				{
					return null;
				}

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
			{
				entity.worldObj.updateEntityWithOptionalForce( entity, true );
			}

			entity.mountEntity( cart );
		}

		return entity;
	}

	private void transverseEdges( final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final ISpatialVisitor visitor )
	{
		for( int y = minY; y < maxY; y++ )
		{
			for( int z = minZ; z < maxZ; z++ )
			{
				visitor.visit( minX, y, z );
				visitor.visit( maxX, y, z );
			}
		}

		for( int x = minX; x < maxX; x++ )
		{
			for( int z = minZ; z < maxZ; z++ )
			{
				visitor.visit( x, minY, z );
				visitor.visit( x, maxY, z );
			}
		}

		for( int x = minX; x < maxX; x++ )
		{
			for( int y = minY; y < maxY; y++ )
			{
				visitor.visit( x, y, minZ );
				visitor.visit( x, y, maxZ );
			}
		}
	}

	public void swapRegions( final World src /** over world **/
			, final World dst /** storage cell **/
			, final int x, final int y, final int z, final int i, final int j, final int k, final int scaleX, final int scaleY, final int scaleZ )
	{
		for( final Block matrixFrameBlock : AEApi.instance().definitions().blocks().matrixFrame().maybeBlock().asSet() )
		{
			this.transverseEdges( i - 1, j - 1, k - 1, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1, new WrapInMatrixFrame( matrixFrameBlock, 0, dst ) );
		}

		final AxisAlignedBB srcBox = AxisAlignedBB.getBoundingBox( x, y, z, x + scaleX + 1, y + scaleY + 1, z + scaleZ + 1 );

		final AxisAlignedBB dstBox = AxisAlignedBB.getBoundingBox( i, j, k, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1 );

		final CachedPlane cDst = new CachedPlane( dst, i, j, k, i + scaleX, j + scaleY, k + scaleZ );
		final CachedPlane cSrc = new CachedPlane( src, x, y, z, x + scaleX, y + scaleY, z + scaleZ );

		// do nearly all the work... swaps blocks, tiles, and block ticks
		cSrc.swap( cDst );

		final List<Entity> srcE = src.getEntitiesWithinAABB( Entity.class, srcBox );
		final List<Entity> dstE = dst.getEntitiesWithinAABB( Entity.class, dstBox );

		for( final Entity e : dstE )
		{
			this.teleportEntity( e, new TelDestination( src, srcBox, e.posX, e.posY, e.posZ, -i + x, -j + y, -k + z ) );
		}

		for( final Entity e : srcE )
		{
			this.teleportEntity( e, new TelDestination( dst, dstBox, e.posX, e.posY, e.posZ, -x + i, -y + j, -z + k ) );
		}

		for( final WorldCoord wc : cDst.getUpdates() )
		{
			cDst.getWorld().notifyBlockOfNeighborChange( wc.x, wc.y, wc.z, Platform.AIR_BLOCK );
		}

		for( final WorldCoord wc : cSrc.getUpdates() )
		{
			cSrc.getWorld().notifyBlockOfNeighborChange( wc.x, wc.y, wc.z, Platform.AIR_BLOCK );
		}

		this.transverseEdges( x - 1, y - 1, z - 1, x + scaleX + 1, y + scaleY + 1, z + scaleZ + 1, new TriggerUpdates( src ) );
		this.transverseEdges( i - 1, j - 1, k - 1, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1, new TriggerUpdates( dst ) );

		this.transverseEdges( x, y, z, x + scaleX, y + scaleY, z + scaleZ, new TriggerUpdates( src ) );
		this.transverseEdges( i, j, k, i + scaleX, j + scaleY, k + scaleZ, new TriggerUpdates( dst ) );

		/*
		 * IChunkProvider cp = destination.getChunkProvider(); if ( cp instanceof ChunkProviderServer ) {
		 * ChunkProviderServer
		 * srv = (ChunkProviderServer) cp; srv.unloadAllChunks(); }
		 * cp.unloadQueuedChunks();
		 */

	}

	private static class TriggerUpdates implements ISpatialVisitor
	{

		private final World dst;

		public TriggerUpdates( final World dst2 )
		{
			this.dst = dst2;
		}

		@Override
		public void visit( final int x, final int y, final int z )
		{
			final Block blk = this.dst.getBlock( x, y, z );
			blk.onNeighborBlockChange( this.dst, x, y, z, Platform.AIR_BLOCK );
		}
	}


	private static class WrapInMatrixFrame implements ISpatialVisitor
	{

		private final World dst;
		private final Block blkID;
		private final int Meta;

		public WrapInMatrixFrame( final Block blockID, final int metaData, final World dst2 )
		{
			this.dst = dst2;
			this.blkID = blockID;
			this.Meta = metaData;
		}

		@Override
		public void visit( final int x, final int y, final int z )
		{
			this.dst.setBlock( x, y, z, this.blkID, this.Meta, 3 );
		}
	}


	private static class TelDestination
	{

		private final World dim;
		private final double x;
		private final double y;
		private final double z;
		private final int xOff;
		private final int yOff;
		private final int zOff;

		TelDestination( final World dimension, final AxisAlignedBB srcBox, final double x, final double y, final double z, final int tileX, final int tileY, final int tileZ )
		{
			this.dim = dimension;
			this.x = Math.min( srcBox.maxX - 0.5, Math.max( srcBox.minX + 0.5, x + tileX ) );
			this.y = Math.min( srcBox.maxY - 0.5, Math.max( srcBox.minY + 0.5, y + tileY ) );
			this.z = Math.min( srcBox.maxZ - 0.5, Math.max( srcBox.minZ + 0.5, z + tileZ ) );
			this.xOff = tileX;
			this.yOff = tileY;
			this.zOff = tileZ;
		}
	}


	private static class METeleporter extends Teleporter
	{

		private final TelDestination destination;

		public METeleporter( final WorldServer par1WorldServer, final TelDestination d )
		{
			super( par1WorldServer );
			this.destination = d;
		}

		@Override
		public void placeInPortal( final Entity par1Entity, final double par2, final double par4, final double par6, final float par8 )
		{
			par1Entity.setLocationAndAngles( this.destination.x, this.destination.y, this.destination.z, par1Entity.rotationYaw, 0.0F );
			par1Entity.motionX = par1Entity.motionY = par1Entity.motionZ = 0.0D;
		}

		@Override
		public boolean placeInExistingPortal( final Entity par1Entity, final double par2, final double par4, final double par6, final float par8 )
		{
			return false;
		}

		@Override
		public boolean makePortal( final Entity par1Entity )
		{
			return false;
		}

		@Override
		public void removeStalePortalLocations( final long par1 )
		{

		}
	}
}
