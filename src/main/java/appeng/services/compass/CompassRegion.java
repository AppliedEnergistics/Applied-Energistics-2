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


import appeng.core.worlddata.MeteorDataNameEncoder;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public final class CompassRegion {
    private final int lowX;
    private final int lowZ;
    private final int world;
    private final File worldCompassFolder;
    private final MeteorDataNameEncoder encoder;

    private boolean hasFile = false;
    private RandomAccessFile raf = null;
    private ByteBuffer buffer;

    public CompassRegion(final int cx, final int cz, final int worldID, @Nonnull final File worldCompassFolder) {
        Preconditions.checkNotNull(worldCompassFolder);
        Preconditions.checkArgument(worldCompassFolder.isDirectory());

        this.world = worldID;
        this.worldCompassFolder = worldCompassFolder;
        this.encoder = new MeteorDataNameEncoder(0);

        final int region_x = cx >> 10;
        final int region_z = cz >> 10;

        this.lowX = region_x << 10;
        this.lowZ = region_z << 10;

        this.openFile(false);
    }

    void close() {
        try {
            if (this.hasFile) {
                this.buffer = null;
                this.raf.close();
                this.raf = null;
                this.hasFile = false;
            }
        } catch (final Throwable t) {
            throw new CompassException(t);
        }
    }

    boolean hasBeacon(int cx, int cz) {
        if (this.hasFile) {
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

        this.openFile(hasBeacon);

        if (this.hasFile) {
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

    @Override
    protected void finalize() throws Throwable {
        try {
            if (this.raf != null) {
                this.raf.close();
            }
        } finally {
            super.finalize();
        }

    }

    private void openFile(final boolean create) {
        if (this.hasFile) {
            return;
        }

        final File file = this.getFile();
        if (create || this.isFileExistent(file)) {
            try {
                this.raf = new RandomAccessFile(file, "rw");
                final FileChannel fc = this.raf.getChannel();
                this.buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, 0x400 * 0x400);// fc.size() );
                this.hasFile = true;
            } catch (final Throwable t) {
                throw new CompassException(t);
            }
        }
    }

    private File getFile() {
        final String fileName = this.encoder.encode(this.world, this.lowX, this.lowZ);

        return new File(this.worldCompassFolder, fileName);
    }

    private boolean isFileExistent(final File file) {
        return file.exists() && file.isFile();
    }

    private int read(final int cx, final int cz) {
        try {
            return this.buffer.get(cx + cz * 0x400);
            // raf.seek( cx + cz * 0x400 );
            // return raf.readByte();
        } catch (final IndexOutOfBoundsException outOfBounds) {
            return 0;
        } catch (final Throwable t) {
            throw new CompassException(t);
        }
    }

    private void write(final int cx, final int cz, final int val) {
        try {
            this.buffer.put(cx + cz * 0x400, (byte) val);
            // raf.seek( cx + cz * 0x400 );
            // raf.writeByte( val );
        } catch (final Throwable t) {
            throw new CompassException(t);
        }
    }
}
