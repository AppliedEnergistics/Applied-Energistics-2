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

package appeng.me.pathfinding;


import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.me.cache.PathGridCache;

import java.util.*;


public class PathSegment {

    private final PathGridCache pgc;
    private final Set<IPathItem> semiOpen;
    private final Set<IPathItem> closed;
    private boolean isDead;
    private List<IPathItem> open;

    public PathSegment(final PathGridCache myPGC, final List<IPathItem> open, final Set<IPathItem> semiOpen, final Set<IPathItem> closed) {
        this.open = open;
        this.semiOpen = semiOpen;
        this.closed = closed;
        this.pgc = myPGC;
        this.setDead(false);
    }

    public boolean step() {
        final List<IPathItem> oldOpen = this.open;
        this.open = new ArrayList<>();

        for (final IPathItem i : oldOpen) {
            for (final IPathItem pi : i.getPossibleOptions()) {
                final EnumSet<GridFlags> flags = pi.getFlags();

                if (!this.closed.contains(pi)) {
                    pi.setControllerRoute(i, true);

                    if (flags.contains(GridFlags.REQUIRE_CHANNEL)) {
                        // close the semi open.
                        if (!this.semiOpen.contains(pi)) {
                            final boolean worked;

                            if (flags.contains(GridFlags.COMPRESSED_CHANNEL)) {
                                worked = this.useDenseChannel(pi);
                            } else {
                                worked = this.useChannel(pi);
                            }

                            if (worked && flags.contains(GridFlags.MULTIBLOCK)) {
                                final Iterator<IGridNode> oni = ((IGridMultiblock) ((IGridNode) pi).getGridBlock()).getMultiblockNodes();
                                while (oni.hasNext()) {
                                    final IGridNode otherNodes = oni.next();
                                    if (otherNodes != pi) {
                                        this.semiOpen.add((IPathItem) otherNodes);
                                    }
                                }
                            }
                        } else {
                            pi.incrementChannelCount(1); // give a channel.
                            this.semiOpen.remove(pi);
                        }
                    }

                    this.closed.add(pi);
                    this.open.add(pi);
                }
            }
        }

        return this.open.isEmpty();
    }

    private boolean useDenseChannel(final IPathItem start) {
        IPathItem pi = start;
        while (pi != null) {
            if (!pi.canSupportMoreChannels() || pi.getFlags().contains(GridFlags.CANNOT_CARRY_COMPRESSED)) {
                return false;
            }

            pi = pi.getControllerRoute();
        }

        pi = start;
        while (pi != null) {
            this.pgc.setChannelsByBlocks(this.pgc.getChannelsByBlocks() + 1);
            pi.incrementChannelCount(1);
            pi = pi.getControllerRoute();
        }

        this.pgc.setChannelsInUse(this.pgc.getChannelsInUse() + 1);
        return true;
    }

    private boolean useChannel(final IPathItem start) {
        IPathItem pi = start;
        while (pi != null) {
            if (!pi.canSupportMoreChannels()) {
                return false;
            }

            pi = pi.getControllerRoute();
        }

        pi = start;
        while (pi != null) {
            this.pgc.setChannelsByBlocks(this.pgc.getChannelsByBlocks() + 1);
            pi.incrementChannelCount(1);
            pi = pi.getControllerRoute();
        }

        this.pgc.setChannelsInUse(this.pgc.getChannelsInUse() + 1);
        return true;
    }

    public boolean isDead() {
        return this.isDead;
    }

    public void setDead(final boolean isDead) {
        this.isDead = isDead;
    }
}
