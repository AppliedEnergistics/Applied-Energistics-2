package appeng.hooks;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.core.AEConfig;
import appeng.core.WorldSettings;
import appeng.core.features.registries.WorldGenRegistry;
import appeng.helpers.MeteoritePlacer;
import appeng.services.helpers.ICompassCallback;
import appeng.util.Platform;
import cpw.mods.fml.common.IWorldGenerator;

final public class MeteoriteWorldGen implements IWorldGenerator
{

	static class MyGen implements ICompassCallback
	{

		double distance = 0;

		@Override
		public void calculatedDirection(boolean hasResult, boolean spin, double radians, double dist)
		{
			if ( hasResult )
				distance = dist;
			else
				distance = Double.MAX_VALUE;
		}

	}

	@Override
	public void generate(Random r, int chunkX, int chunkZ, World w, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if ( WorldGenRegistry.instance.isWorldGenEnabled( WorldGenType.Meteorites, w ) )
		{
			// add new meteorites?
			if ( r.nextFloat() < AEConfig.instance.meteoriteSpawnChance )
			{
				int x = r.nextInt( 16 ) + (chunkX << 4);
				int z = r.nextInt( 16 ) + (chunkZ << 4);

				int depth = 180 + r.nextInt( 20 );
				TickHandler.instance.addCallable( w, new MeteoriteSpawn( x, depth, z, w ) );
			}
			else
				TickHandler.instance.addCallable( w, new MeteoriteSpawn( chunkX << 4, 128, chunkZ << 4, w ) );
		}
		else
			WorldSettings.getInstance().getCompass().updateArea( w, chunkX, chunkZ );
	}

	class MeteoriteSpawn implements Callable
	{

		final int x;
		final int z;
		final World w;
		final int depth;

		public MeteoriteSpawn(int x, int depth, int z, World w) {
			this.x = x;
			this.z = z;
			this.w = w;
			this.depth = depth;
		}

		@Override
		public Object call() throws Exception
		{
			int chunkX = x >> 4;
			int chunkZ = z >> 4;

			double minSqDist = Double.MAX_VALUE;

			// near by meteorites!
			for (NBTTagCompound data : getNearByMeteorites( w, chunkX, chunkZ ))
			{
				MeteoritePlacer mp = new MeteoritePlacer();
				mp.spawnMeteorite( new MeteoritePlacer.ChunkOnly( w, chunkX, chunkZ ), data );

				minSqDist = Math.min( minSqDist, mp.getSqDistance( x, z ) );
			}

			boolean isCluster = (minSqDist < 30 * 30) && Platform.getRandomFloat() < AEConfig.instance.meteoriteClusterChance;

			if ( minSqDist > AEConfig.instance.minMeteoriteDistanceSq || isCluster )
				tryMeteorite( w, depth, x, z );

			WorldSettings.getInstance().setGenerated( w.provider.dimensionId, chunkX, chunkZ );
			WorldSettings.getInstance().getCompass().updateArea( w, chunkX, chunkZ );

			return null;
		}
	}

	private boolean tryMeteorite(World w, int depth, int x, int z)
	{
		for (int tries = 0; tries < 20; tries++)
		{
			MeteoritePlacer mp = new MeteoritePlacer();

			if ( mp.spawnMeteorite( new MeteoritePlacer.ChunkOnly( w, x >> 4, z >> 4 ), x, depth, z ) )
			{
				int px = x >> 4;
				int pz = z >> 4;

				for (int cx = px - 6; cx < px + 6; cx++)
					for (int cz = pz - 6; cz < pz + 6; cz++)
					{
						if ( w.getChunkProvider().chunkExists( cx, cz ) )
						{
							if ( px == cx && pz == cz )
								continue;

							if ( WorldSettings.getInstance().hasGenerated( w.provider.dimensionId, cx, cz ) )
							{
								MeteoritePlacer mp2 = new MeteoritePlacer();
								mp2.spawnMeteorite( new MeteoritePlacer.ChunkOnly( w, cx, cz ), mp.getSettings() );
							}
						}
					}

				return true;
			}

			depth -= 15;
			if ( depth < 40 )
				return false;
		}

		return false;
	}

	private Collection<NBTTagCompound> getNearByMeteorites(World w, int chunkX, int chunkZ)
	{
		return WorldSettings.getInstance().getNearByMeteorites( w.provider.dimensionId, chunkX, chunkZ );
	}

}
