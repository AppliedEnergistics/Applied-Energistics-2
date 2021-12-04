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

package appeng.menu.me.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEKey;

/**
 * This utility class helps menus that need to send a list of information that is grouped by
 * {@link AEKey} to the client and keep it updated, without having to resend the
 * {@link AEKey} everytime. This can be especially important if the item stack is serialized
 * using it's {@link ItemStack#getShareTag() share tag}, which would not match the server-side stack if it's sent back,
 * or that would group distinct server-side entries together on the client-side if their share tag was equal.
 */
public class IncrementalUpdateHelper implements Iterable<AEKey> {

    /**
     * Maps stacks to serial numbers. This relies on the fact that these stacks are equal iff their type is equal, and
     * two stacks with different counts are still equal.
     */
    private final BiMap<AEKey, Long> mapping;

    private final Set<AEKey> changes = new HashSet<>();

    private long serial;

    /**
     * Indicates that a full update should be sent.
     */
    private boolean fullUpdate = true;

    public IncrementalUpdateHelper() {
        this.mapping = HashBiMap.create();
    }

    @Nullable
    public Long getSerial(AEKey stack) {
        return mapping.get(stack);
    }

    public long getOrAssignSerial(AEKey key) {
        return mapping.computeIfAbsent(key, k -> ++this.serial);
    }

    public AEKey getBySerial(long serial) {
        return mapping.inverse().get(serial);
    }

    /**
     * Clear pending changes and prepare for a full update.
     * <p/>
     * Mappings are kept because even in case of a full update, many items are usually still present and should keep
     * their serial.
     */
    public void clear() {
        this.changes.clear();
        fullUpdate = true;
    }

    /**
     * Fully resets this helper into its initial state. This will also clear any serial mapping.
     */
    public void reset() {
        clear();
        this.serial = 0;
        this.mapping.clear();
    }

    public void addChange(AEKey entry) {
        if (!changes.add(entry)) {
            changes.remove(entry);
            changes.add(entry);
        }
    }

    /**
     * Removes the serial mapping for the given key. Will lead to a new serial being generated the next time this
     * particular key is used.
     */
    public void removeSerial(AEKey what) {
        mapping.remove(what);
    }

    public void commitChanges() {
        changes.clear();
        fullUpdate = false;
    }

    public boolean hasChanges() {
        return fullUpdate || !changes.isEmpty();
    }

    public boolean isFullUpdate() {
        return fullUpdate;
    }

    @Override
    public Iterator<AEKey> iterator() {
        return changes.iterator();
    }

    @Override
    public void forEach(Consumer<? super AEKey> action) {
        changes.forEach(action);
    }

    @Override
    public Spliterator<AEKey> spliterator() {
        return changes.spliterator();
    }
}
