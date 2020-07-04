package appeng.worldgen;

import java.util.Random;
import java.util.function.Function;

import com.mojang.datafixers.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.ReplaceBlockFeature;

import appeng.core.AppEng;

/**
 * Extends {@link ReplaceBlockFeature} by also allowing for a replacement
 * chance. In addition, the feature will check every block in the chunk.
 */
public class ChargedQuartzOreFeature extends Feature<ChargedQuartzOreConfig> {

    public static final ChargedQuartzOreFeature INSTANCE = new ChargedQuartzOreFeature(
            ChargedQuartzOreConfig::deserialize);

    static {
        INSTANCE.setRegistryName(AppEng.MOD_ID, "charged_quartz_ore");
    }

    public ChargedQuartzOreFeature(Function<Dynamic<?>, ? extends ChargedQuartzOreConfig> p_i51444_1_) {
        super(p_i51444_1_);
    }

    @Override
    public boolean place(WorldAccess worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand,
                         BlockPos pos, ChargedQuartzOreConfig config) {
        ChunkPos chunkPos = new ChunkPos(pos);

        BlockPos.Mutable bpos = new BlockPos.Mutable();
        int height = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        Chunk chunk = worldIn.getChunk(pos);
        for (int y = 0; y < height; y++) {
            bpos.setY(y);
            for (int x = chunkPos.getXStart(); x <= chunkPos.getXEnd(); x++) {
                bpos.setX(x);
                for (int z = chunkPos.getZStart(); z <= chunkPos.getZEnd(); z++) {
                    bpos.setZ(z);
                    if (chunk.getBlockState(bpos).getBlock() == config.target.getBlock()
                            && rand.nextFloat() < config.chance) {
                        chunk.setBlockState(bpos, config.state, false);
                    }
                }
            }
        }

        return true;
    }
}
