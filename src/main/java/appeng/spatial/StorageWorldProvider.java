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


import appeng.client.render.SpatialSkyRender;
import appeng.core.AppEng;
import appeng.core.Registration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;


public class StorageWorldProvider extends WorldProvider
{

	public StorageWorldProvider()
	{
		this.hasNoSky = true;
	}

	@Override
	protected void registerWorldChunkManager()
	{
		final AppEng ae2internal = AppEng.instance();
		final Registration ae2registration = ae2internal.getRegistration();
		final BiomeGenBase storageBiome = ae2registration.getStorageBiome();

		super.worldChunkMgr = new WorldChunkManagerHell( storageBiome, 0.0F );
	}

	@Override
	public IChunkProvider createChunkGenerator()
	{
		return new StorageChunkProvider( this.worldObj, 0 );
	}

	@Override
	public float calculateCelestialAngle( final long par1, final float par3 )
	{
		return 0;
	}

	@Override
	public boolean isSurfaceWorld()
	{
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public float[] calcSunriseSunsetColors( final float celestialAngle, final float partialTicks )
	{
		return null;
	}

	@Override
	public Vec3 getFogColor( final float par1, final float par2 )
	{
		return Vec3.createVectorHelper( 0.07, 0.07, 0.07 );
	}

	@Override
	public boolean canRespawnHere()
	{
		return false;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public boolean isSkyColored()
	{
		return true;
	}

	@Override
	public boolean doesXZShowFog( final int par1, final int par2 )
	{
		return false;
	}

	@Override
	public String getDimensionName()
	{
		return "Storage Cell";
	}

	@Override
	public IRenderHandler getSkyRenderer()
	{
		return SpatialSkyRender.getInstance();
	}

	@Override
	public boolean isDaytime()
	{
		return false;
	}

	@Override
	public Vec3 getSkyColor( final Entity cameraEntity, final float partialTicks )
	{
		return Vec3.createVectorHelper( 0.07, 0.07, 0.07 );
	}

	@Override
	public float getStarBrightness( final float par1 )
	{
		return 0;
	}

	@Override
	public boolean canSnowAt( final int x, final int y, final int z, final boolean checkLight )
	{
		return false;
	}

	@Override
	public ChunkCoordinates getSpawnPoint()
	{
		return new ChunkCoordinates( 0, 0, 0 );
	}

	@Override
	public boolean isBlockHighHumidity( final int x, final int y, final int z )
	{
		return false;
	}

	@Override
	public boolean canDoLightning( final Chunk chunk )
	{
		return false;
	}
}
