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
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import appeng.core.AppEng;

public class MeteoriteStructure extends Structure<NoFeatureConfig> {

    /**
     * Configures how this structure will be placed throughout the world.
     */
    public static final StructureSeparationSettings PLACEMENT_CONFIG = new StructureSeparationSettings(32, 8, 124895654);

    public static final ResourceLocation ID = AppEng.makeId("meteorite");

    public static final Structure<NoFeatureConfig> INSTANCE = new MeteoriteStructure(
            NoFeatureConfig.field_24893);

    public static final StructureFeature<NoFeatureConfig, ? extends Structure<NoFeatureConfig>> CONFIGURED_INSTANCE = INSTANCE
            .withConfiguration(NoFeatureConfig.field_24894);

    public MeteoriteStructure(Codec<NoFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean shouldStartAt(ChunkGenerator generator, BiomeProvider biomeSource, long seed, SharedSeedRandom randIn,
            int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos2, NoFeatureConfig featureConfig) {
        return randIn.nextBoolean();
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return MeteoriteStructureStart::new;
    }

}
