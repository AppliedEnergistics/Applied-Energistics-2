package appeng.worldgen.meteorite;

import appeng.core.AppEng;
import com.mojang.serialization.Codec;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;

public class MeteoriteStructure extends Structure<NoFeatureConfig> {

    public static final ResourceLocation ID = AppEng.makeId("meteorite");

    public static final Structure<NoFeatureConfig> INSTANCE = new MeteoriteStructure(
            NoFeatureConfig.field_236558_a_);

    public MeteoriteStructure(Codec<NoFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public String getStructureName() {
        String s = super.getStructureName();
        return s;
    }

    @Override
    protected boolean func_230363_a_(ChunkGenerator generator, BiomeProvider biomeSource, long seed, SharedSeedRandom randIn, int chunkX, int chunkZ, Biome biome, ChunkPos chunkPos2, NoFeatureConfig featureConfig) {
        return randIn.nextBoolean();
    }

    @Override
    public IStartFactory<NoFeatureConfig> getStartFactory() {
        return MeteoriteStructureStart::new;
    }

}
