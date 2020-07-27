package appeng.worldgen;

import java.util.Random;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;

/**
 * Extends {@link net.minecraft.world.gen.feature.OreFeature} by also allowing
 * for a replacement chance. In addition, the feature will check every block in
 * the chunk.
 */
public class ChargedQuartzOreFeature extends Feature<ChargedQuartzOreConfig> {

    public static final ChargedQuartzOreFeature INSTANCE = new ChargedQuartzOreFeature(ChargedQuartzOreConfig.CODEC);

    public ChargedQuartzOreFeature(Codec<ChargedQuartzOreConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(ServerWorldAccess worldIn, net.minecraft.world.gen.StructureAccessor structureAccessor,
            ChunkGenerator generator, Random rand, BlockPos pos, ChargedQuartzOreConfig config) {
        ChunkPos chunkPos = new ChunkPos(pos);

        BlockPos.Mutable bpos = new BlockPos.Mutable();
        int height = worldIn.getTopY(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        Chunk chunk = worldIn.getChunk(pos);
        for (int y = 0; y < height; y++) {
            bpos.setY(y);
            for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
                bpos.setX(x);
                for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
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
