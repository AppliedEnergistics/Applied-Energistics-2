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

package appeng.core.worlddata;


import appeng.core.AELog;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * @author thatsIch
 * @version rv3 - 30.05.2015
 * @since rv3 30.05.2015
 */
final class SpawnData implements IWorldSpawnData {
    @Nonnull
    private final File spawnDirectory;
    @Nonnull
    private final MeteorDataNameEncoder encoder;

    public SpawnData(@Nonnull final File spawnDirectory) {
        Preconditions.checkNotNull(spawnDirectory);

        this.spawnDirectory = spawnDirectory;
        this.encoder = new MeteorDataNameEncoder(4);
    }

    @Override
    public void setGenerated(final int dim, final int chunkX, final int chunkZ) {
        synchronized (SpawnData.class) {
            final NBTTagCompound data = this.loadSpawnData(dim, chunkX, chunkZ);

            // edit.
            data.setBoolean(chunkX + "," + chunkZ, true);

            this.writeSpawnData(dim, chunkX, chunkZ, data);
        }
    }

    @Override
    public boolean hasGenerated(final int dim, final int chunkX, final int chunkZ) {
        synchronized (SpawnData.class) {
            final NBTTagCompound data = this.loadSpawnData(dim, chunkX, chunkZ);
            return data.getBoolean(chunkX + "," + chunkZ);
        }
    }

    @Override
    public boolean addNearByMeteorites(final int dim, final int chunkX, final int chunkZ, final NBTTagCompound newData) {
        synchronized (SpawnData.class) {
            final NBTTagCompound data = this.loadSpawnData(dim, chunkX, chunkZ);

            // edit.
            final int size = data.getInteger("num");
            data.setTag(String.valueOf(size), newData);
            data.setInteger("num", size + 1);

            this.writeSpawnData(dim, chunkX, chunkZ, data);

            return true;
        }
    }

    @Override
    public Collection<NBTTagCompound> getNearByMeteorites(final int dim, final int chunkX, final int chunkZ) {
        final Collection<NBTTagCompound> ll = new ArrayList<>();

        synchronized (SpawnData.class) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    final int cx = x + (chunkX >> 4);
                    final int cz = z + (chunkZ >> 4);

                    final NBTTagCompound data = this.loadSpawnData(dim, cx << 4, cz << 4);

                    if (data != null) {
                        // edit.
                        final int size = data.getInteger("num");
                        for (int s = 0; s < size; s++) {
                            ll.add(data.getCompoundTag(String.valueOf(s)));
                        }
                    }
                }
            }
        }

        return ll;
    }

    private NBTTagCompound loadSpawnData(final int dim, final int chunkX, final int chunkZ) {
        if (!Thread.holdsLock(SpawnData.class)) {
            throw new IllegalStateException("Invalid Request");
        }

        NBTTagCompound data = null;
        final String fileName = this.encoder.encode(dim, chunkX, chunkZ);
        final File file = new File(this.spawnDirectory, fileName);

        if (file.isFile()) {
            FileInputStream fileInputStream = null;

            try {
                fileInputStream = new FileInputStream(file);
                data = CompressedStreamTools.readCompressed(fileInputStream);
            } catch (final Throwable e) {
                data = new NBTTagCompound();
                AELog.debug(e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (final IOException e) {
                        AELog.debug(e);
                    }
                }
            }
        } else {
            data = new NBTTagCompound();
        }

        return data;
    }

    private void writeSpawnData(final int dim, final int chunkX, final int chunkZ, final NBTTagCompound data) {
        if (!Thread.holdsLock(SpawnData.class)) {
            throw new IllegalStateException("Invalid Request");
        }

        final String fileName = this.encoder.encode(dim, chunkX, chunkZ);
        final File file = new File(this.spawnDirectory, fileName);
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            CompressedStreamTools.writeCompressed(data, fileOutputStream);
        } catch (final Throwable e) {
            AELog.debug(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (final IOException e) {
                    AELog.debug(e);
                }
            }
        }
    }
}
