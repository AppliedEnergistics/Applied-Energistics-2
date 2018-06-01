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

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import appeng.api.AEApi;
import appeng.core.AppEng;


public class StorageChunkProvider implements IChunkGenerator
{

	private final World world;

	public StorageChunkProvider( final World world )
	{
		this.world = world;
	}

	@Override
	public Chunk generateChunk( final int x, final int z )
	{
		ChunkPrimer primer = new ChunkPrimer();

		AEApi.instance().definitions().blocks().matrixFrame().maybeBlock().ifPresent( block -> this.fillChunk( primer, block.getDefaultState() ) );

		Chunk chunk = new Chunk( this.world, primer, x, z );
		
		final byte[] biomes = chunk.getBiomeArray();
		Biome biome = AppEng.instance().getStorageBiome();
		byte biomeId = (byte) Biome.getIdForBiome( biome );

		for( int k = 0; k < biomes.length; ++k )
		{
			biomes[k] = biomeId;
		}
		
		chunk.setModified( false );

		if( !chunk.isTerrainPopulated() )
		{
			chunk.setTerrainPopulated( true );
			chunk.resetRelightChecks();
		}

		return chunk;
	}

	private void fillChunk( ChunkPrimer primer, IBlockState defaultState )
	{
		for( int cx = 0; cx < 16; cx++ )
		{
			for( int cz = 0; cz < 16; cz++ )
			{
				for( int cy = 0; cy < 256; cy++ )
				{
					primer.setBlockState( cx, cy, cz, defaultState );
				}
			}
		}
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
	public BlockPos getNearestStructurePos( World worldIn, String structureName, BlockPos position, boolean p_180513_4_ )
	{
		return null;
	}

	@Override
	public void recreateStructures( Chunk chunkIn, int x, int z )
	{

	}
	
	@Override
	public boolean isInsideStructure( World worldIn, String structure, BlockPos pos){
		return false;
	}
}
