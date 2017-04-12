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


import appeng.api.AEApi;
import appeng.core.AEConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;

import java.util.ArrayList;
import java.util.List;


public class StorageChunkProvider extends ChunkProviderGenerate
{
	private static final int SQUARE_CHUNK_SIZE = 256;
	private static final Block[] BLOCKS;

	static
	{
		BLOCKS = new Block[255 * SQUARE_CHUNK_SIZE];

		for( final Block matrixFrameBlock : AEApi.instance().definitions().blocks().matrixFrame().maybeBlock().asSet() )
		{
			for( int x = 0; x < BLOCKS.length; x++ )
			{
				BLOCKS[x] = matrixFrameBlock;
			}
		}
	}

	private final World world;

	public StorageChunkProvider( final World world, final long i )
	{
		super( world, i, false );
		this.world = world;
	}

	@Override
	public Chunk provideChunk( final int x, final int z )
	{
		final Chunk chunk = new Chunk( this.world, BLOCKS, x, z );

		final byte[] biomes = chunk.getBiomeArray();
		final AEConfig config = AEConfig.instance;

		for( int k = 0; k < biomes.length; ++k )
		{
			biomes[k] = (byte) config.storageBiomeID;
		}

		if( !chunk.isTerrainPopulated )
		{
			chunk.isTerrainPopulated = true;
			chunk.resetRelightChecks();
		}

		return chunk;
	}

	@Override
	public void populate( final IChunkProvider par1iChunkProvider, final int par2, final int par3 )
	{

	}

	@Override
	public boolean unloadQueuedChunks()
	{
		return true;
	}

	@Override
	public List getPossibleCreatures( final EnumCreatureType a, final int b, final int c, final int d )
	{
		return new ArrayList();
	}
}
