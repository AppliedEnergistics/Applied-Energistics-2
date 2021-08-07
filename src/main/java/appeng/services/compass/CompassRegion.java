/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.services.compass;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

final class CompassRegion {
    private final int lowX;
    private final int lowZ;
    private final ServerLevel level;
    private SaveData data;

    public CompassRegion(ServerLevel level, final int cx, final int cz) {
        Preconditions.checkNotNull(level);

        this.level = level;

        final int region_x = cx >> 10;
        final int region_z = cz >> 10;

        this.lowX = region_x << 10;
        this.lowZ = region_z << 10;

        this.openData(false);
    }

    void close() {
        if (this.data != null) {
            this.data = null;
        }
    }

    boolean hasBeacon(int cx, int cz) {
        if (this.data != null) {
            cx &= 0x3FF;
            cz &= 0x3FF;

            final int val = this.read(cx, cz);
            return val != 0;
        }

        return false;
    }

    void setHasBeacon(int cx, int cz, final int cdy, final boolean hasBeacon) {
        cx &= 0x3FF;
        cz &= 0x3FF;

        this.openData(hasBeacon);

        if (this.data != null) {
            int val = this.read(cx, cz);
            final int originalVal = val;

            if (hasBeacon) {
                val |= 1 << cdy;
            } else {
                val &= ~(1 << cdy);
            }

            if (originalVal != val) {
                this.write(cx, cz, val);
            }
        }
    }

    private void openData(final boolean create) {
        if (this.data != null) {
            return;
        }

        String name = this.lowX + "_" + this.lowZ;

        if (create) {
            this.data = level.getDataStorage().computeIfAbsent(SaveData::load, SaveData::new, name);
            if (this.data.bitmap == null) {
                this.data.bitmap = new byte[SaveData.BITMAP_LENGTH];
            }
        } else {
            this.data = level.getDataStorage().get(SaveData::load, name);
        }
    }

    private int read(final int cx, final int cz) {
        try {
            return this.data.bitmap[cx + cz * 0x400];
        } catch (final IndexOutOfBoundsException outOfBounds) {
            return 0;
        }
    }

    private void write(final int cx, final int cz, final int val) {
        this.data.bitmap[cx + cz * 0x400] = (byte) val;
        this.data.setDirty();
    }

    private static class SaveData extends SavedData {

        private static final int BITMAP_LENGTH = 0x400 * 0x400;

        private byte[] bitmap;

        public static SaveData load(CompoundTag nbt) {
            var result = new SaveData();
            result.bitmap = nbt.getByteArray("b");
            if (result.bitmap.length != BITMAP_LENGTH) {
                throw new IllegalStateException("Invalid bitmap length: " + result.bitmap.length);
            }
            return result;
        }

        @Override
        public CompoundTag save(CompoundTag compound) {
            compound.putByteArray("b", bitmap);
            return compound;
        }

    }

}
