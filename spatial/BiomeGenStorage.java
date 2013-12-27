package appeng.spatial;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenStorage extends BiomeGenBase
{

	public BiomeGenStorage(int id) {
		super( id );
		this.setBiomeName( "Storage Cell" );

		this.setDisableRain();
		this.temperature = -100;

		this.theBiomeDecorator.treesPerChunk = 0;
		this.theBiomeDecorator.flowersPerChunk = 0;
		this.theBiomeDecorator.grassPerChunk = 0;

		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.clear();
		this.spawnableWaterCreatureList.clear();
		this.spawnableCaveCreatureList.clear();
	}

}
