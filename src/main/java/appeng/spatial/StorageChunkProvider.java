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


import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderOverworld;

import appeng.api.AEApi;
import appeng.core.AEConfig;


public class StorageChunkProvider extends ChunkProviderOverworld
{
	private static final int SQUARE_CHUNK_SIZE = 256;
	private static final Block[] BLOCKS;

	static
	{
		BLOCKS = new Block[255 * SQUARE_CHUNK_SIZE];

		AEApi.instance().definitions().blocks().matrixFrame().maybeBlock().ifPresent( matrixFrameBlock -> {
			for( int x = 0; x < BLOCKS.length; x++ )
			{
				BLOCKS[x] = matrixFrameBlock;
			}
		} );
	}

	private final World world;

	public StorageChunkProvider( final World world, final long i )
	{
		super( world, i, false, null );
		this.world = world;
	}

	@Override
	public Chunk provideChunk( final int x, final int z )
	{
		final Chunk chunk = new Chunk( this.world, x, z );

		final byte[] biomes = chunk.getBiomeArray();
		final AEConfig config = AEConfig.instance;

		for( int k = 0; k < biomes.length; ++k )
		{
			biomes[k] = (byte) config.storageBiomeID;
		}

		if( !chunk.isTerrainPopulated() )
		{
			chunk.setTerrainPopulated( true );
			chunk.resetRelightChecks();
		}

		return chunk;
	}

	@Override
	public void populate( final int par2, final int par3 )
	{

	}

	@Override
	public List getPossibleCreatures( final EnumCreatureType creatureType, final BlockPos pos )
	{
		return new ArrayList();
	}

	@Override
	public boolean generateStructures( Chunk chunkIn, int x, int z )
	{
		return false;
	}

	@Override
	public BlockPos getStrongholdGen( World worldIn, String structureName, BlockPos position )
	{
		return null;
	}

	@Override
	public void recreateStructures( Chunk chunkIn, int x, int z )
	{
		
	}
}
