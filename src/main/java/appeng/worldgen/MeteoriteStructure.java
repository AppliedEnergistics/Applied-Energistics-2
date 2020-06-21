package appeng.worldgen;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.EndChunkGenerator;
import net.minecraft.world.gen.NetherChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

import appeng.core.AEConfig;
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
        return MeteoriteStructure.Start::new;
    }

    @Override
    protected int getSeedModifier() {
        return 124895654;
    }

    public static class Start extends StructureStart {
        public Start(Structure<?> p_i225815_1_, int p_i225815_2_, int p_i225815_3_, MutableBoundingBox p_i225815_4_,
                int p_i225815_5_, long p_i225815_6_) {
            super(p_i225815_1_, p_i225815_2_, p_i225815_3_, p_i225815_4_, p_i225815_5_, p_i225815_6_);
        }

        public void init(ChunkGenerator<?> generator, TemplateManager templateManagerIn, int chunkX, int chunkZ,
                Biome biome) {
            float meteoriteSize = (this.rand.nextFloat() * 6.0f) + 2;

            int centerX = chunkX * 16 + rand.nextInt(16);
            int centerZ = chunkZ * 16 + rand.nextInt(16);

            BlockPos actualPos = new BlockPos(centerX, AEConfig.instance().getMeteoriteMaximumSpawnHeight(), centerZ);

            components.add(new MeteoriteStructurePiece(actualPos, meteoriteSize));
            this.recalculateStructureSize();
        }
    }

}
