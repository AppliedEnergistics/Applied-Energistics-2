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
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;

import appeng.api.AEApi;
import appeng.core.AEConfig;

public class StorageChunkProvider extends ChunkProviderGenerate
{

	final static Block[] BLOCKS;

	static {
		BLOCKS = new Block[255 * 256];

		final Block matrixFrameBlock = AEApi.instance().definitions().blocks().matrixFrame().block();
		for (int x = 0; x < BLOCKS.length; x++)
		{
			BLOCKS[x] = matrixFrameBlock;
		}
	}

	final World w;

	public StorageChunkProvider(World wrd, long i) {
		super( wrd, i, false );
		this.w = wrd;
	}

	@Override
	public boolean unloadQueuedChunks()
	{
		return true;
	}

	@Override
	public Chunk provideChunk(int x, int z)
	{
		Chunk chunk = new Chunk( this.w, BLOCKS, x, z );

		byte[] biomes = chunk.getBiomeArray();
		AEConfig config = AEConfig.instance;

		for (int k = 0; k < biomes.length; ++k)
			biomes[k] = (byte) config.storageBiomeID;

		if ( !chunk.isTerrainPopulated )
		{
			chunk.isTerrainPopulated = true;
			chunk.resetRelightChecks();
		}

		return chunk;
	}

	@Override
	public void populate(IChunkProvider par1iChunkProvider, int par2, int par3)
	{

	}

	@Override
	public List getPossibleCreatures(EnumCreatureType a, int b, int c, int d)
	{
		return new ArrayList();
	}

}
