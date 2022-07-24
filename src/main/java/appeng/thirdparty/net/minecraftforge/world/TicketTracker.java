/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package appeng.thirdparty.net.minecraftforge.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Helper class to manage tracking and handling loaded tickets.
 */
public class TicketTracker<T extends Comparable<? super T>> {
    public final Map<TicketOwner<T>, LongSet> chunks = new HashMap<>();
    public final Map<TicketOwner<T>, LongSet> tickingChunks = new HashMap<>();

    /**
     * Gets an unmodifiable view of the tracked chunks.
     */
    public Map<TicketOwner<T>, LongSet> getChunks() {
        return Collections.unmodifiableMap(chunks);
    }

    /**
     * Gets an unmodifiable view of the tracked fully ticking chunks.
     */
    public Map<TicketOwner<T>, LongSet> getTickingChunks() {
        return Collections.unmodifiableMap(tickingChunks);
    }

    /**
     * Checks if this tracker is empty.
     *
     * @return {@code true} if there are no chunks or ticking chunks being tracked.
     */
    public boolean isEmpty() {
        return chunks.isEmpty() && tickingChunks.isEmpty();
    }

    private Map<TicketOwner<T>, LongSet> getTickets(boolean ticking) {
        return ticking ? tickingChunks : chunks;
    }

    /**
     * @return {@code true} if the state changed.
     */
    public boolean remove(TicketOwner<T> owner, long chunk, boolean ticking) {
        Map<TicketOwner<T>, LongSet> tickets = getTickets(ticking);
        if (tickets.containsKey(owner)) {
            LongSet ticketChunks = tickets.get(owner);
            if (ticketChunks.remove(chunk)) {
                if (ticketChunks.isEmpty())
                    tickets.remove(owner);
                return true;
            }
        }
        return false;
    }

    /**
     * @return {@code true} if the state changed.
     */
    public boolean add(TicketOwner<T> owner, long chunk, boolean ticking) {
        return getTickets(ticking).computeIfAbsent(owner, o -> new LongOpenHashSet()).add(chunk);
    }
}
