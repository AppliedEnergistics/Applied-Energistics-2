package appeng.worldgen;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.ScatteredStructurePiece;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class MeteoritePiece extends ScatteredStructurePiece {

    public static IStructurePieceType TYPE = IStructurePieceType.register(MeteoritePiece::new, "AE2M");

    public MeteoritePiece(Random random, int x, int z) {
        super(TYPE, random, x, 64, z, 7, 7, 9);
    }

    public MeteoritePiece(TemplateManager p_i51340_1_, CompoundNBT p_i51340_2_) {
        super(TYPE, p_i51340_2_);
    }

    @Override
    public boolean create(IWorld worldIn, ChunkGenerator<?> chunkGeneratorIn, Random randomIn,
            MutableBoundingBox mutableBoundingBoxIn, ChunkPos chunkPosIn) {
        return false;
    }

}
