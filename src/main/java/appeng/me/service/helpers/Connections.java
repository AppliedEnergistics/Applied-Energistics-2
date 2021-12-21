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

package appeng.me.service.helpers;

import java.util.HashMap;

import appeng.api.networking.IGridNode;
import appeng.parts.p2p.MEP2PTunnelPart;

public class Connections implements Runnable {

    private final HashMap<IGridNode, TunnelConnection> connections = new HashMap<>();
    private final MEP2PTunnelPart me;
    private boolean create = false;
    private boolean destroy = false;

    public Connections(MEP2PTunnelPart o) {
        this.me = o;
    }

    @Override
    public void run() {
        this.me.updateConnections(this);
    }

    public void markDestroy() {
        this.setCreate(false);
        this.setDestroy(true);
    }

    public void markCreate() {
        this.setCreate(true);
        this.setDestroy(false);
    }

    public HashMap<IGridNode, TunnelConnection> getConnections() {
        return this.connections;
    }

    public boolean isCreate() {
        return this.create;
    }

    private void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isDestroy() {
        return this.destroy;
    }

    private void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }
}
