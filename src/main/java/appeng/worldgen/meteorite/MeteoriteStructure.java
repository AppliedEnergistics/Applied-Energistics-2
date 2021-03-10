package appeng.worldgen.meteorite;

import com.mojang.serialization.Codec;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.Structure.IStartFactory;

import appeng.core.AppEng;

public class MeteoriteStructure extends Structure<NoFeatureConfig> {

    public static final ResourceLocation ID = AppEng.makeId("meteorite");

    public static final Structure<NoFeatureConfig> INSTANCE = new MeteoriteStructure(NoFeatureConfig.CODEC);

    public static final StructureFeature<NoFeatureConfig, ? extends Structure<NoFeatureConfig>> CONFIGURED_INSTANCE = INSTANCE
            .configured(NoFeatureConfig.INSTANCE);

    public MeteoriteStructure(Codec<NoFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    protected boolean isFeatureChunk(ChunkGenerator generator, BiomeProvider biomeSource, long seed,
            SharedSeedRandom randIn, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos2,
            NoFeatureConfig featureConfig) {
        return randIn.nextBoolean();
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return MeteoriteStructureStart::new;
    }

}
