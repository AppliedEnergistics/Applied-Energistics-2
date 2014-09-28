package appeng.hooks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import appeng.api.AEApi;
import appeng.api.features.IWorldGen.WorldGenType;
import appeng.core.AEConfig;
import appeng.core.features.registries.WorldGenRegistry;
import cpw.mods.fml.common.IWorldGenerator;

final public class QuartzWorldGen implements IWorldGenerator
{

	final WorldGenMinable oreNormal;
	final WorldGenMinable oreCharged;

	public QuartzWorldGen() {
		Block normal = AEApi.instance().blocks().blockQuartzOre.block();
		Block charged = AEApi.instance().blocks().blockQuartzOreCharged.block();

		if ( normal != null && charged != null )
		{
			oreNormal = new WorldGenMinable( normal, 0, AEConfig.instance.quartzOresPerCluster, Blocks.stone );
			oreCharged = new WorldGenMinable( charged, 0, AEConfig.instance.quartzOresPerCluster, Blocks.stone );
		}
		else
			oreNormal = oreCharged = null;
	};

	@Override
	public void generate(Random r, int chunkX, int chunkZ, World w, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		int seaLevel = w.provider.getAverageGroundLevel() + 1;

		if ( seaLevel < 20 )
		{
			int x = (chunkX << 4) + 8;
			int z = (chunkZ << 4) + 8;
			seaLevel = w.getHeightValue( x, z );
		}

		if ( oreNormal == null || oreCharged == null )
			return;

		double oreDepthMultiplier = AEConfig.instance.quartzOresClusterAmount * seaLevel / 64;
		int scale = (int) Math.round( r.nextGaussian() * Math.sqrt( oreDepthMultiplier ) + oreDepthMultiplier );

		for (int x = 0; x < (r.nextBoolean() ? scale * 2 : scale) / 2; ++x)
		{
			boolean isCharged = r.nextFloat() > AEConfig.instance.spawnChargedChance;
			WorldGenMinable whichOre = isCharged ? oreCharged : oreNormal;

			if ( WorldGenRegistry.instance.isWorldGenEnabled( isCharged ? WorldGenType.ChargedCertusQuartz : WorldGenType.CertusQuartz, w ) )
			{
				int cx = chunkX * 16 + r.nextInt( 22 );
				int cy = r.nextInt( 40 * seaLevel / 64 ) + r.nextInt( 22 * seaLevel / 64 ) + 12 * seaLevel / 64;
				int cz = chunkZ * 16 + r.nextInt( 22 );
				whichOre.generate( w, r, cx, cy, cz );
			}
		}

	}
}
