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


import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridConnectionVisitor;
import appeng.api.networking.IGridNode;
import appeng.me.GridConnection;
import appeng.me.GridNode;


public class AdHocChannelUpdater implements IGridConnectionVisitor {

    private final int usedChannels;

    public AdHocChannelUpdater(final int used) {
        this.usedChannels = used;
    }

    @Override
    public boolean visitNode(final IGridNode n) {
        final GridNode gn = (GridNode) n;
        gn.setControllerRoute(null, true);
        gn.incrementChannelCount(this.usedChannels);
        gn.finalizeChannels();
        return true;
    }

    @Override
    public void visitConnection(final IGridConnection gcc) {
        final GridConnection gc = (GridConnection) gcc;
        gc.setControllerRoute(null, true);
        gc.incrementChannelCount(this.usedChannels);
        gc.finalizeChannels();
    }
}
