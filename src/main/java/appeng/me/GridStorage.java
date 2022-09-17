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

package appeng.me;


import appeng.api.networking.IGrid;
import appeng.api.networking.IGridStorage;
import appeng.core.AELog;
import appeng.core.worlddata.WorldData;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;


public class GridStorage implements IGridStorage {

    private final long myID;
    private final NBTTagCompound data;
    private final GridStorageSearch mySearchEntry; // keep myself in the list until I'm
    private final WeakHashMap<GridStorage, Boolean> divided = new WeakHashMap<>();
    private WeakReference<IGrid> internalGrid = null;

    // lost...

    /**
     * for use with world settings
     *
     * @param id  ID of grid storage
     * @param gss grid storage search
     */
    public GridStorage(final long id, final GridStorageSearch gss) {
        this.myID = id;
        this.mySearchEntry = gss;
        this.data = new NBTTagCompound();
    }

    /**
     * for use with world settings
     *
     * @param input array of bytes string
     * @param id    ID of grid storage
     * @param gss   grid storage search
     */
    public GridStorage(final String input, final long id, final GridStorageSearch gss) {
        this.myID = id;
        this.mySearchEntry = gss;
        NBTTagCompound myTag = null;

        try {
            final byte[] byteData = javax.xml.bind.DatatypeConverter.parseBase64Binary(input);
            myTag = CompressedStreamTools.readCompressed(new ByteArrayInputStream(byteData));
        } catch (final Throwable t) {
            myTag = new NBTTagCompound();
        }

        this.data = myTag;
    }

    /**
     * fake storage.
     */
    public GridStorage() {
        this.myID = 0;
        this.mySearchEntry = null;
        this.data = new NBTTagCompound();
    }

    public String getValue() {
        final Grid currentGrid = (Grid) this.getGrid();
        if (currentGrid != null) {
            currentGrid.saveState();
        }

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(this.data, out);
            return javax.xml.bind.DatatypeConverter.printBase64Binary(out.toByteArray());
        } catch (final IOException e) {
            AELog.debug(e);
        }

        return "";
    }

    public IGrid getGrid() {
        return this.internalGrid == null ? null : this.internalGrid.get();
    }

    void setGrid(final Grid grid) {
        this.internalGrid = new WeakReference<>(grid);
    }

    @Override
    public NBTTagCompound dataObject() {
        return this.data;
    }

    @Override
    public long getID() {
        return this.myID;
    }

    void addDivided(final GridStorage gs) {
        this.divided.put(gs, true);
    }

    boolean hasDivided(final GridStorage myStorage) {
        return this.divided.containsKey(myStorage);
    }

    void remove() {
        WorldData.instance().storageData().destroyGridStorage(this.myID);
    }
}
