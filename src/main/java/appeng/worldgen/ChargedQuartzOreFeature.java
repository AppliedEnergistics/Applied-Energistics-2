package appeng.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.StructureManager;

import java.util.Random;

/**
 * Extends {@link net.minecraft.world.gen.feature.OreFeature} by also allowing
 * for a replacement chance. In addition, the feature will check every block in
 * the chunk.
 */
public class ChargedQuartzOreFeature extends Feature<ChargedQuartzOreConfig> {

    public static final ChargedQuartzOreFeature INSTANCE = new ChargedQuartzOreFeature(ChargedQuartzOreConfig.CODEC);

    private ChargedQuartzOreFeature(Codec<ChargedQuartzOreConfig> codec) {
        super(codec);
    }

    @Override
    public boolean func_230362_a_(ISeedReader worldIn, StructureManager structureAccessor, ChunkGenerator generator, Random rand, BlockPos pos, ChargedQuartzOreConfig config) {
        ChunkPos chunkPos = new ChunkPos(pos);

        BlockPos.Mutable bpos = new BlockPos.Mutable();
        int height = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        IChunk chunk = worldIn.getChunk(pos);
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
