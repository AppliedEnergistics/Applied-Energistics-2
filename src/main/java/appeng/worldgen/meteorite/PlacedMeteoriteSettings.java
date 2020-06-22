/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2020, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.worldgen.meteorite;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import appeng.worldgen.meteorite.fallout.FalloutMode;

public final class PlacedMeteoriteSettings {

    private final BlockPos pos;
    private final float meteoriteRadius;
    private final CraterType craterType;
    private final FalloutMode fallout;
    private final boolean pureCrater;

    public PlacedMeteoriteSettings(BlockPos pos, float meteoriteRadius, CraterType craterType, FalloutMode fallout,
            boolean pureCrater) {
        this.pos = pos;
        this.craterType = craterType;
        this.meteoriteRadius = meteoriteRadius;
        this.fallout = fallout;
        this.pureCrater = pureCrater;
    }

    public BlockPos getPos() {
        return pos;
    }

    public CraterType getCraterType() {
        return this.craterType;
    }

    public float getMeteoriteRadius() {
        return meteoriteRadius;
    }

    public FalloutMode getFallout() {
        return fallout;
    }

    public boolean shouldPlaceCrater() {
        return this.craterType != CraterType.NONE;
    }

    public boolean isPureCrater() {
        return this.pureCrater;
    }

    public CompoundNBT write(CompoundNBT tag) {
        tag.putLong(Constants.TAG_POS, pos.toLong());

        tag.putFloat(Constants.TAG_RADIUS, meteoriteRadius);
        tag.putByte(Constants.TAG_CRATER, (byte) craterType.ordinal());
        tag.putByte(Constants.TAG_FALLOUT, (byte) fallout.ordinal());
        tag.putBoolean(Constants.TAG_PURE, this.pureCrater);
        return tag;
    }

    public static PlacedMeteoriteSettings read(CompoundNBT tag) {
        BlockPos pos = BlockPos.fromLong(tag.getLong(Constants.TAG_POS));
        float meteoriteRadius = tag.getFloat(Constants.TAG_RADIUS);
        CraterType craterType = CraterType.values()[tag.getByte(Constants.TAG_CRATER)];
        FalloutMode fallout = FalloutMode.values()[tag.getByte(Constants.TAG_FALLOUT)];
        boolean pureCrater = tag.getBoolean(Constants.TAG_PURE);

        return new PlacedMeteoriteSettings(pos, meteoriteRadius, craterType, fallout, pureCrater);
    }

    @Override
    public String toString() {
        return "PlacedMeteoriteSettings [pos=" + pos + ", meteoriteRadius=" + meteoriteRadius + ", craterType="
                + craterType + ", fallout=" + fallout + ", pureCrater=" + pureCrater + "]";
    }

}
