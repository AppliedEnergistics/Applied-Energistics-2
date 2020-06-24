package appeng.worldgen.meteorite;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.EndChunkGenerator;
import net.minecraft.world.gen.NetherChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;

import appeng.core.AELog;
import appeng.core.AppEng;

public class MeteoriteStructure extends ScatteredStructure<NoFeatureConfig> {

    public static final Structure<NoFeatureConfig> INSTANCE = new MeteoriteStructure(NoFeatureConfig::deserialize);

    static {
        INSTANCE.setRegistryName(AppEng.MOD_ID, "meteorite");
    }

    public MeteoriteStructure(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn) {
        super(configFactoryIn);
    }

    @Override
    public boolean canBeGenerated(BiomeManager biomeManagerIn, ChunkGenerator<?> generator, Random randIn, int chunkX,
            int chunkZ, Biome biomeIn) {
        if (super.canBeGenerated(biomeManagerIn, generator, randIn, chunkX, chunkZ, biomeIn)) {
            // In case the biome blacklist fails, we still double-check here that we're not
            // generating chunks for
            // the nether or end.
            if (generator.getClass().equals(EndChunkGenerator.class)
                    || generator.getClass().equals(NetherChunkGenerator.class)) {
                AELog.warn("ignoring attempt to generate meteorite in nether/end.");
                return false;
            }

            int i = chunkX >> 4;
            int j = chunkZ >> 4;
            randIn.setSeed((long) (i ^ j << 4) ^ generator.getSeed());
            randIn.nextInt();
            return randIn.nextBoolean();
        }
        return false;
    }

    @Override
    public String getStructureName() {
        return INSTANCE.getRegistryName().toString();
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public Structure.IStartFactory getStartFactory() {
        return MeteoriteStructureStart::new;
    }

    @Override
    protected int getSeedModifier() {
        return 124895654;
    }

}
