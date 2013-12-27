package appeng.spatial;

import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import appeng.core.Registration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class StorageWorldProvider extends WorldProvider
{

	public StorageWorldProvider() {
		this.hasNoSky = true;
	}

	@Override
	public ChunkCoordinates getSpawnPoint()
	{
		return new ChunkCoordinates( 0, 0, 0 );
	}

	@Override
	public boolean canRespawnHere()
	{
		return false;
	}

	@Override
	protected void registerWorldChunkManager()
	{
		super.worldChunkMgr = new WorldChunkManagerHell( Registration.instance.storageBiome, 1, 1 );
	}

	@Override
	public float getStarBrightness(float par1)
	{
		return 0;
	}

	@Override
	public boolean isSurfaceWorld()
	{
		return false;
	}

	@Override
	public boolean canDoLightning(Chunk chunk)
	{
		return false;
	}

	@Override
	public boolean isBlockHighHumidity(int x, int y, int z)
	{
		return false;
	}

	@Override
	public boolean isDaytime()
	{
		return false;
	}

	@Override
	public Vec3 getSkyColor(Entity cameraEntity, float partialTicks)
	{
		return this.worldObj.getWorldVec3Pool().getVecFromPool( 0.0, 0.0, 0.0 );
	}

	@Override
	public boolean doesXZShowFog(int par1, int par2)
	{
		return false;
	}

	@Override
	public float calculateCelestialAngle(long par1, float par3)
	{
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isSkyColored()
	{
		return false;
	}

	@Override
	public Vec3 getFogColor(float par1, float par2)
	{
		return this.worldObj.getWorldVec3Pool().getVecFromPool( 0.0, 0.0, 0.0 );
	}

	@Override
	public IChunkProvider createChunkGenerator()
	{
		return new StorageChunkProvider( worldObj, 0 );
	}

	@Override
	public String getDimensionName()
	{
		return "Storage Cell";
	}

}
