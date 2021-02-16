package appeng.worldgen.meteorite;

import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StructureConfig;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import appeng.core.AppEng;

public class MeteoriteStructure extends StructureFeature<DefaultFeatureConfig> {

    /**
     * Configures how this structure will be placed throughout the world.
     */
    public static final StructureConfig PLACEMENT_CONFIG = new StructureConfig(32, 8, 124895654);

    public static final Identifier ID = AppEng.makeId("meteorite");

    public static final StructureFeature<DefaultFeatureConfig> INSTANCE = new MeteoriteStructure(
            DefaultFeatureConfig.CODEC);

    public static final ConfiguredStructureFeature<DefaultFeatureConfig, ? extends StructureFeature<DefaultFeatureConfig>> CONFIGURED_INSTANCE = INSTANCE
            .configure(DefaultFeatureConfig.INSTANCE);

    public MeteoriteStructure(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean shouldStartAt(ChunkGenerator generator, BiomeSource biomeSource, long seed, ChunkRandom randIn,
            int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos2, DefaultFeatureConfig featureConfig,
            HeightLimitView heightLimitView) {
        return randIn.nextBoolean();
    }

    @Override
    public StructureStartFactory<DefaultFeatureConfig> getStructureStartFactory() {
        return MeteoriteStructureStart::new;
    }

}
