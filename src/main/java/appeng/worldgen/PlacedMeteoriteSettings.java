package appeng.worldgen;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import appeng.worldgen.meteorite.FalloutMode;

public final class PlacedMeteoriteSettings {

    private final BlockPos pos;
    private final boolean lava;
    private final boolean placeCrater;
    private final float meteoriteRadius;
    private final FalloutMode fallout;

    public PlacedMeteoriteSettings(BlockPos pos, float meteoriteRadius, boolean lava, boolean placeCrater,
            FalloutMode fallout) {
        this.pos = pos;
        this.lava = lava;
        this.placeCrater = placeCrater;
        this.meteoriteRadius = meteoriteRadius;
        this.fallout = fallout;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isLava() {
        return lava;
    }

    public boolean isPlaceCrater() {
        return placeCrater;
    }

    public float getMeteoriteRadius() {
        return meteoriteRadius;
    }

    public FalloutMode getFallout() {
        return fallout;
    }

    public CompoundNBT write(CompoundNBT tag) {
        tag.putLong("c", pos.toLong());

        tag.putFloat("mr", meteoriteRadius);

        tag.putBoolean("l", lava);
        tag.putBoolean("cr", placeCrater);
        tag.putByte("f", (byte) fallout.ordinal());
        return tag;
    }

    public static PlacedMeteoriteSettings read(CompoundNBT tag) {
        BlockPos pos = BlockPos.fromLong(tag.getLong("c"));
        float meteoriteRadius = tag.getFloat("mr");
        boolean lava = tag.getBoolean("l");
        boolean placeCrater = tag.getBoolean("cr");
        FalloutMode fallout = FalloutMode.values()[tag.getByte("f")];

        return new PlacedMeteoriteSettings(pos, meteoriteRadius, lava, placeCrater, fallout);
    }

}
