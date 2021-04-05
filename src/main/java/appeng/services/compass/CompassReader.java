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

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import net.minecraft.world.server.ServerWorld;

public final class CompassReader {
    private final Map<Long, CompassRegion> regions = new HashMap<>(100);
    private final ServerWorld world;

    public CompassReader(ServerWorld world) {
        this.world = Preconditions.checkNotNull(world);
    }

    public void close() {
        for (final CompassRegion r : this.regions.values()) {
            r.close();
        }

        this.regions.clear();
    }

    public void setHasBeacon(final int cx, final int cz, final int cdy, final boolean hasBeacon) {
        final CompassRegion r = this.getRegion(cx, cz);

        r.setHasBeacon(cx, cz, cdy, hasBeacon);
    }

    public boolean hasBeacon(final int cx, final int cz) {
        final CompassRegion r = this.getRegion(cx, cz);

        return r.hasBeacon(cx, cz);
    }

    private CompassRegion getRegion(final int cx, final int cz) {
        long pos = cx >> 10;
        pos <<= 32;
        pos |= (cz >> 10);

        CompassRegion cr = this.regions.get(pos);

        if (cr == null) {
            cr = new CompassRegion(world, cx, cz);
            this.regions.put(pos, cr);
        }

        return cr;
    }
}
