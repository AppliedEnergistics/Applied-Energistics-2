package appeng.worldgen;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.common.util.Constants;

import appeng.core.worlddata.WorldData;
import appeng.worldgen.meteorite.FalloutMode;

public class MeteoriteStructurePiece extends StructurePiece {

    public static final IStructurePieceType TYPE = IStructurePieceType.register(MeteoriteStructurePiece::new,
            "AE2MTRT");

    private PlacedMeteoriteSettings settings;

    protected MeteoriteStructurePiece(BlockPos center, float coreRadius) {
        super(TYPE, 0);
        this.settings = new PlacedMeteoriteSettings(center, coreRadius, false, false, null);

        // Since we don't know yet if the meteorite will be underground or not,
        // we have to assume maximum size
        int range = (int) Math.ceil((coreRadius * 2 + 5) * 1.25f);

        this.boundingBox = new MutableBoundingBox(center.getX() - range, center.getY(), center.getZ() - range,
                center.getX() + range, center.getY(), center.getZ() + range);
    }

    public MeteoriteStructurePiece(TemplateManager templateManager, CompoundNBT tag) {
        super(TYPE, tag);

        // Mandatory fields
        BlockPos center = BlockPos.fromLong(tag.getLong("c"));
        float coreRadius = tag.getFloat("r");
        boolean placeCrater = false;
        boolean lava = false;
        FalloutMode fallout = null;
        if (tag.contains("f", Constants.NBT.TAG_COMPOUND)) {
            placeCrater = tag.getBoolean("pc");
            lava = tag.getBoolean("l");
            fallout = FalloutMode.values()[tag.getByte("f")];
        }

        this.settings = new PlacedMeteoriteSettings(center, coreRadius, lava, placeCrater, fallout);
    }

    public boolean isFinalized() {
        return settings.getFallout() != null;
    }

    public PlacedMeteoriteSettings getSettings() {
        return settings;
    }

    @Override
    protected void readAdditional(CompoundNBT tag) {
        tag.putFloat("r", settings.getMeteoriteRadius());
        tag.putLong("c", settings.getPos().toLong());
        if (isFinalized()) {
            tag.putBoolean("pc", settings.isPlaceCrater());
            tag.putBoolean("l", settings.isLava());
            tag.putByte("f", (byte) settings.getFallout().ordinal());
        }
    }

    @Override
    public boolean create(IWorld world, ChunkGenerator<?> chunkGeneratorIn, Random rand, MutableBoundingBox bounds,
            ChunkPos chunkPos) {

        // The parent structure synchronizes on the list of components, so this
        // placement should be
        // mutually exclusive with any other chunk the structure is placed in. This
        // allows us to
        // finalize some of the placement parameters now that we have access to an
        // actual world object
        if (!isFinalized()) {
            boolean lava = rand.nextFloat() > 0.9f;
            MeteoriteSpawner spawner = new MeteoriteSpawner();
            BlockPos center = settings.getPos();
            float coreRadius = settings.getMeteoriteRadius();
            settings = spawner.trySpawnMeteoriteAtSuitableHeight(world, center, coreRadius, lava);
            if (settings == null) {
                return false;
            }
        }

        MeteoritePlacer placer = new MeteoritePlacer(world, settings, bounds);
        placer.place();

        WorldData.instance().compassData().service().updateArea(world, chunkPos); // FIXME: We know the y-range here...
        return true;
    }

}
