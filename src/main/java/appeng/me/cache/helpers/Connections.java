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

package appeng.me.cache.helpers;


import appeng.api.networking.IGridNode;
import appeng.parts.p2p.PartP2PTunnelME;
import appeng.util.IWorldCallable;
import net.minecraft.world.World;

import java.util.HashMap;


public class Connections implements IWorldCallable<Void> {

    private final HashMap<IGridNode, TunnelConnection> connections = new HashMap<>();
    private final PartP2PTunnelME me;
    private boolean create = false;
    private boolean destroy = false;

    public Connections(final PartP2PTunnelME o) {
        this.me = o;
    }

    @Override
    public Void call(final World world) throws Exception {
        this.me.updateConnections(this);

        return null;
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

    private void setCreate(final boolean create) {
        this.create = create;
    }

    public boolean isDestroy() {
        return this.destroy;
    }

    private void setDestroy(final boolean destroy) {
        this.destroy = destroy;
    }
}
