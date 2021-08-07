/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.menu.guisync;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.core.AELog;

/**
 * Helper class for synchronizing fields from server-side menus to client-side menus. Fields need to be
 * annotated with {@link GuiSync} and given a unique key within the class hierarchy.
 */
public class DataSynchronization {

    private final Map<Short, SynchronizedField<?>> fields = new HashMap<>();

    public DataSynchronization(Object host) {
        collectFields(host, host.getClass());
    }

    private void collectFields(Object host, Class<?> clazz) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(GuiSync.class)) {
                final GuiSync annotation = f.getAnnotation(GuiSync.class);
                short key = annotation.value();
                if (this.fields.containsKey(key)) {
                    throw new IllegalStateException(
                            "Class " + host.getClass() + " declares the same sync id twice: " + key);
                }
                this.fields.put(key, SynchronizedField.create(host, f));
            }
        }

        // Recurse upwards through the class hierarchy
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != AbstractContainerMenu.class && superclass != Object.class) {
            collectFields(host, superclass);
        }
    }

    public boolean hasChanges() {
        for (SynchronizedField<?> value : fields.values()) {
            if (value.hasChanges()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Write the data for all fields to the given buffer, and marks all fields as unchanged.
     */
    public void writeFull(FriendlyByteBuf data) {
        writeFields(data, true);
    }

    /**
     * Write the data for changed fields to the given buffer, and marks all fields as unchanged.
     */
    public void writeUpdate(FriendlyByteBuf data) {
        writeFields(data, false);
    }

    private void writeFields(FriendlyByteBuf data, boolean includeUnchanged) {
        for (Map.Entry<Short, SynchronizedField<?>> entry : fields.entrySet()) {
            if (includeUnchanged || entry.getValue().hasChanges()) {
                data.writeShort(entry.getKey());
                entry.getValue().write(data);
            }
        }

        // Terminator
        data.writeVarInt(-1);
    }

    public void readUpdate(FriendlyByteBuf data) {
        for (short key = data.readShort(); key != -1; key = data.readShort()) {
            SynchronizedField<?> field = fields.get(key);
            if (field == null) {
                AELog.warn("Server sent update for GUI field %d, which we don't know.", key);
                continue;
            }

            field.read(data);
        }
    }

    /**
     * @return True if any synchronized fields exist.
     */
    public boolean hasFields() {
        return !fields.isEmpty();
    }
}
