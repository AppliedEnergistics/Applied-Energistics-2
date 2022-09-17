/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.hooks;


import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompassRequest;

import java.util.HashMap;
import java.util.Iterator;


public class CompassManager {

    public static final CompassManager INSTANCE = new CompassManager();
    private final HashMap<CompassRequest, CompassResult> requests = new HashMap<>();

    public void postResult(final long attunement, final int x, final int y, final int z, final CompassResult result) {
        final CompassRequest r = new CompassRequest(attunement, x, y, z);
        this.requests.put(r, result);
    }

    public CompassResult getCompassDirection(final long attunement, final int x, final int y, final int z) {
        final long now = System.currentTimeMillis();

        final Iterator<CompassResult> i = this.requests.values().iterator();
        while (i.hasNext()) {
            final CompassResult res = i.next();
            final long diff = now - res.getTime();
            if (diff > 20000) {
                i.remove();
            }
        }

        final CompassRequest r = new CompassRequest(attunement, x, y, z);
        CompassResult res = this.requests.get(r);

        if (res == null) {
            res = new CompassResult(false, true, 0);
            this.requests.put(r, res);
            this.requestUpdate(r);
        } else if (now - res.getTime() > 1000 * 3) {
            if (!res.isRequested()) {
                res.setRequested(true);
                this.requestUpdate(r);
            }
        }

        return res;
    }

    private void requestUpdate(final CompassRequest r) {
        NetworkHandler.instance().sendToServer(new PacketCompassRequest(r.attunement, r.cx, r.cz, r.cdy));
    }

    private static class CompassRequest {

        private final int hash;
        private final long attunement;
        private final int cx;
        private final int cdy;
        private final int cz;

        public CompassRequest(final long attunement, final int x, final int y, final int z) {
            this.attunement = attunement;
            this.cx = x >> 4;
            this.cdy = y >> 5;
            this.cz = z >> 4;
            this.hash = ((Integer) this.cx).hashCode() ^ ((Integer) this.cdy).hashCode() ^ ((Integer) this.cz).hashCode() ^ ((Long) attunement)
                    .hashCode();
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final CompassRequest other = (CompassRequest) obj;
            return this.attunement == other.attunement && this.cx == other.cx && this.cdy == other.cdy && this.cz == other.cz;
        }
    }
}
