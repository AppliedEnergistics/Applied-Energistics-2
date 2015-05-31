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


import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.client.render.SpatialSkyRender;
import appeng.core.Registration;


public final class StorageWorldProvider extends WorldProvider
{

	public StorageWorldProvider()
	{
		this.hasNoSky = true;
	}

	@Override
	protected final void registerWorldChunkManager()
	{
		super.worldChunkMgr = new WorldChunkManagerHell( Registration.INSTANCE.storageBiome, 0.0F );
	}

	@Override
	public final IChunkProvider createChunkGenerator()
	{
		return new StorageChunkProvider( this.worldObj, 0 );
	}

	@Override
	public final float calculateCelestialAngle( long par1, float par3 )
	{
		return 0;
	}

	@Override
	public final boolean isSurfaceWorld()
	{
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public final float[] calcSunriseSunsetColors( float celestialAngle, float partialTicks )
	{
		return null;
	}

	@Override
	public final Vec3 getFogColor( float par1, float par2 )
	{
		return Vec3.createVectorHelper( 0.07, 0.07, 0.07 );
	}

	@Override
	public final boolean canRespawnHere()
	{
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public final boolean isSkyColored()
	{
		return true;
	}

	@Override
	public final boolean doesXZShowFog( int par1, int par2 )
	{
		return false;
	}

	@Override
	public final String getDimensionName()
	{
		return "Storage Cell";
	}

	@Override
	public final IRenderHandler getSkyRenderer()
	{
		return SpatialSkyRender.getInstance();
	}

	@Override
	public final boolean isDaytime()
	{
		return false;
	}

	@Override
	public final Vec3 getSkyColor( Entity cameraEntity, float partialTicks )
	{
		return Vec3.createVectorHelper( 0.07, 0.07, 0.07 );
	}

	@Override
	public final float getStarBrightness( float par1 )
	{
		return 0;
	}

	@Override
	public final boolean canSnowAt( int x, int y, int z, boolean checkLight )
	{
		return false;
	}

	@Override
	public final ChunkCoordinates getSpawnPoint()
	{
		return new ChunkCoordinates( 0, 0, 0 );
	}

	@Override
	public final boolean isBlockHighHumidity( int x, int y, int z )
	{
		return false;
	}

	@Override
	public final boolean canDoLightning( Chunk chunk )
	{
		return false;
	}
}
