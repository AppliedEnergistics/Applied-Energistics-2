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

package appeng.me.cache;

import java.util.HashMap;
import java.util.Random;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.ITickManager;
import appeng.core.AELog;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.p2p.P2PTunnelPart;

public class P2PCache implements IGridCache {
    private static final TunnelCollection<P2PTunnelPart> NULL_COLLECTION = new TunnelCollection<P2PTunnelPart>(null,
            null);

    private final IGrid myGrid;
    private final HashMap<Short, P2PTunnelPart> inputs = new HashMap<>();
    private final Multimap<Short, P2PTunnelPart> outputs = LinkedHashMultimap.create();
    private final Random frequencyGenerator;

    public P2PCache(final IGrid g) {
        this.myGrid = g;
        this.frequencyGenerator = new Random(g.hashCode());
    }

    @MENetworkEventSubscribe
    public void bootComplete(final MENetworkBootingStatusChange bootStatus) {
        final ITickManager tm = this.myGrid.getCache(ITickManager.class);
        for (final P2PTunnelPart me : this.inputs.values()) {
            if (me instanceof MEP2PTunnelPart) {
                tm.wakeDevice(me.getGridNode());
            }
        }
    }

    @MENetworkEventSubscribe
    public void bootComplete(final MENetworkPowerStatusChange power) {
        final ITickManager tm = this.myGrid.getCache(ITickManager.class);
        for (final P2PTunnelPart me : this.inputs.values()) {
            if (me instanceof MEP2PTunnelPart) {
                tm.wakeDevice(me.getGridNode());
            }
        }
    }

    @Override
    public void onUpdateTick() {

    }

    @Override
    public void removeNode(final IGridNode node, final IGridHost machine) {
        if (machine instanceof P2PTunnelPart) {
            if ((machine instanceof MEP2PTunnelPart) && !node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                return;
            }

            final P2PTunnelPart t = (P2PTunnelPart) machine;
            // AELog.info( "rmv-" + (t.output ? "output: " : "input: ") + t.freq );

            if (t.isOutput()) {
                this.outputs.remove(t.getFrequency(), t);
            } else {
                this.inputs.remove(t.getFrequency());
            }

            this.updateTunnel(t.getFrequency(), !t.isOutput(), false);
        }
    }

    @Override
    public void addNode(final IGridNode node, final IGridHost machine) {
        if (machine instanceof P2PTunnelPart) {
            if ((machine instanceof MEP2PTunnelPart) && !node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                return;
            }

            final P2PTunnelPart t = (P2PTunnelPart) machine;
            // AELog.info( "add-" + (t.output ? "output: " : "input: ") + t.freq );

            if (t.isOutput()) {
                this.outputs.put(t.getFrequency(), t);
            } else {
                this.inputs.put(t.getFrequency(), t);
            }

            this.updateTunnel(t.getFrequency(), !t.isOutput(), false);
        }
    }

    @Override
    public void onSplit(final IGridStorage storageB) {

    }

    @Override
    public void onJoin(final IGridStorage storageB) {

    }

    @Override
    public void populateGridStorage(final IGridStorage storage) {

    }

    private void updateTunnel(final short freq, final boolean updateOutputs, final boolean configChange) {
        for (final P2PTunnelPart p : this.outputs.get(freq)) {
            if (configChange) {
                p.onTunnelConfigChange();
            }
            p.onTunnelNetworkChange();
        }

        final P2PTunnelPart in = this.inputs.get(freq);
        if (in != null) {
            if (configChange) {
                in.onTunnelConfigChange();
            }
            in.onTunnelNetworkChange();
        }
    }

    public void updateFreq(final P2PTunnelPart t, final short newFrequency) {
        if (this.outputs.containsValue(t)) {
            this.outputs.remove(t.getFrequency(), t);
        }

        if (this.inputs.containsValue(t)) {
            this.inputs.remove(t.getFrequency());
        }

        t.setFrequency(newFrequency);

        if (t.isOutput()) {
            this.outputs.put(t.getFrequency(), t);
        } else {
            this.inputs.put(t.getFrequency(), t);
        }

        // AELog.info( "update-" + (t.output ? "output: " : "input: ") + t.freq );
        this.updateTunnel(t.getFrequency(), t.isOutput(), true);
        this.updateTunnel(t.getFrequency(), !t.isOutput(), true);
    }

    public short newFrequency() {
        short newFrequency;
        int cycles = 0;

        do {
            newFrequency = (short) this.frequencyGenerator.nextInt(1 << 16);
            cycles++;
        } while (newFrequency == 0 || this.inputs.containsKey(newFrequency));

        if (cycles > 25) {
            AELog.debug("Generating a new P2P frequency '%1$d' took %2$d cycles", newFrequency, cycles);
        }

        return newFrequency;
    }

    public TunnelCollection<P2PTunnelPart> getOutputs(final short freq, final Class<? extends P2PTunnelPart> c) {
        final P2PTunnelPart in = this.inputs.get(freq);

        if (in == null) {
            return NULL_COLLECTION;
        }

        final TunnelCollection<P2PTunnelPart> out = this.inputs.get(freq).getCollection(this.outputs.get(freq), c);

        if (out == null) {
            return NULL_COLLECTION;
        }

        return out;
    }

    public P2PTunnelPart getInput(final short freq) {
        return this.inputs.get(freq);
    }
}
