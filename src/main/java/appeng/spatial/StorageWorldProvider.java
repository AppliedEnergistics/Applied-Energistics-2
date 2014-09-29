package appeng.spatial;

import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;
import appeng.client.render.SpatialSkyRender;
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
		super.worldChunkMgr = new WorldChunkManagerHell( Registration.instance.storageBiome, 0.0F );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float p_76560_1_, float p_76560_2_)
	{
		return null;
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
	public boolean canSnowAt(int x, int y, int z, boolean checkLight)
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
		return Vec3.createVectorHelper( 0.07, 0.07, 0.07 );
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
		return true;
	}

	@Override
	public Vec3 getFogColor(float par1, float par2)
	{
		return Vec3.createVectorHelper( 0.07, 0.07, 0.07 );
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
	
	@Override
	public IRenderHandler getSkyRenderer()
	{
		return SpatialSkyRender.getInstance();
	}

}
