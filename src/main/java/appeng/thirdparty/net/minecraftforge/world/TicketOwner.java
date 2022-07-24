/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package appeng.thirdparty.net.minecraftforge.world;

import java.util.Objects;

/**
 * Helper class to keep track of a ticket owner by modid and owner object
 */
public record TicketOwner<T extends Comparable<? super T>> (String modId,
        T owner) implements Comparable<TicketOwner<T>> {

    @Override
    public int compareTo(TicketOwner<T> other) {
        int res = modId.compareTo(other.modId);
        return res == 0 ? owner.compareTo(other.owner) : res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TicketOwner<?> that = (TicketOwner<?>) o;
        return Objects.equals(modId, that.modId) && Objects.equals(owner, that.owner);
    }
}
