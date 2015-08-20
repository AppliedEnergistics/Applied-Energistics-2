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
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import appeng.client.render.SpatialSkyRender;
import appeng.core.AppEng;
import appeng.core.Registration;


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
		return new Vec3( 0.07, 0.07, 0.07 );
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
		return new Vec3( 0.07, 0.07, 0.07 );
	}

	@Override
	public float getStarBrightness( final float par1 )
	{
		return 0;
	}

	@Override
	public boolean canSnowAt( final BlockPos pos, final boolean checkLight )
	{
		return false;
	}
	
	@Override
	public BlockPos getSpawnCoordinate()
	{
		return new BlockPos(0,0,0);
	}

	@Override
	public boolean isBlockHighHumidity( final BlockPos pos )
	{
		return false;
	}

	@Override
	public boolean canDoLightning( final Chunk chunk )
	{
		return false;
	}

	@Override
	public String getInternalNameSuffix()
	{
		return null;
	}
}
