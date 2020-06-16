package appeng.worldgen;

import java.util.Objects;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public final class PlacedMeteoriteSettings {

    private final BlockPos pos;
    private final ResourceLocation blk;
    private final boolean lava;
    private final int skyMode;
    private final double meteoriteRadius;
    private final double craterRadius;

    public PlacedMeteoriteSettings(BlockPos pos, ResourceLocation blk, boolean lava, int skyMode,
            double meteoriteRadius, double craterRadius) {
        this.pos = pos;
        this.blk = blk;
        this.lava = lava;
        this.skyMode = skyMode;
        this.meteoriteRadius = meteoriteRadius;
        this.craterRadius = craterRadius;
    }

    public BlockPos getPos() {
        return pos;
    }

    public ResourceLocation getBlk() {
        return blk;
    }

    public boolean isLava() {
        return lava;
    }

    public int getSkyMode() {
        return skyMode;
    }

    public double getMeteoriteRadius() {
        return meteoriteRadius;
    }

    public double getCraterRadius() {
        return craterRadius;
    }

    public CompoundNBT write(CompoundNBT tag) {
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString("blk", blk.toString());

        tag.putDouble("meteoriteRadius", meteoriteRadius);
        tag.putDouble("craterRadius", craterRadius);

        tag.putBoolean("lava", lava);
        tag.putInt("skyMode", skyMode);
        return tag;
    }

    public static PlacedMeteoriteSettings read(CompoundNBT tag) {
        BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        ResourceLocation blk = new ResourceLocation(tag.getString("blk"));
        double meteoriteRadius = tag.getDouble("meteoriteRadius");
        double craterRadius = tag.getDouble("craterRadius");
        boolean lava = tag.getBoolean("lava");
        int skyMode = tag.getInt("skyMode");

        return new PlacedMeteoriteSettings(pos, blk, lava, skyMode, meteoriteRadius, craterRadius);
    }

}
