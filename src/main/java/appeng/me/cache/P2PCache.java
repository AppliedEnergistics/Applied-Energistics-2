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


import appeng.api.networking.*;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.ITickManager;
import appeng.core.AELog;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.parts.p2p.PartP2PTunnelME;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Random;


public class P2PCache implements IGridCache {
    private static final TunnelCollection<PartP2PTunnel> NULL_COLLECTION = new TunnelCollection<PartP2PTunnel>(null, null);

    private final IGrid myGrid;
    private final Multimap<Short, PartP2PTunnel> inputs = LinkedHashMultimap.create();
    private final Multimap<Short, PartP2PTunnel> outputs = LinkedHashMultimap.create();
    private final Random frequencyGenerator;

    public P2PCache(final IGrid g) {
        this.myGrid = g;
        this.frequencyGenerator = new Random(g.hashCode());
    }

    @MENetworkEventSubscribe
    public void bootComplete(final MENetworkBootingStatusChange bootStatus) {
        final ITickManager tm = this.myGrid.getCache(ITickManager.class);
        for (final PartP2PTunnel me : this.inputs.values()) {
            if (me instanceof PartP2PTunnelME) {
                tm.wakeDevice(me.getGridNode());
            }
        }
    }

    @MENetworkEventSubscribe
    public void bootComplete(final MENetworkPowerStatusChange power) {
        final ITickManager tm = this.myGrid.getCache(ITickManager.class);
        for (final PartP2PTunnel me : this.inputs.values()) {
            if (me instanceof PartP2PTunnelME) {
                tm.wakeDevice(me.getGridNode());
            }
        }
    }

    @Override
    public void onUpdateTick() {

    }

    @Override
    public void removeNode(final IGridNode node, final IGridHost machine) {
        if (machine instanceof PartP2PTunnel) {
            if (machine instanceof PartP2PTunnelME) {
                if (!node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                    return;
                }
            }

            final PartP2PTunnel t = (PartP2PTunnel) machine;
            // AELog.info( "rmv-" + (t.output ? "output: " : "input: ") + t.freq );

            if (t.isOutput()) {
                this.outputs.remove(t.getFrequency(), t);
            } else {
                this.inputs.remove(t.getFrequency(), t);
            }

            if (this.inputs.get(t.getFrequency()).isEmpty()) {
                this.inputs.removeAll(t.getFrequency());
            }
            if (this.outputs.get(t.getFrequency()).isEmpty()) {
                this.outputs.removeAll(t.getFrequency());
            }

            this.updateTunnel(t.getFrequency(), t.isOutput(), false);
        }
    }

    @Override
    public void addNode(final IGridNode node, final IGridHost machine) {
        if (machine instanceof PartP2PTunnel) {
            if (machine instanceof PartP2PTunnelME) {
                if (!node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                    return;
                }
            }

            final PartP2PTunnel t = (PartP2PTunnel) machine;
            // AELog.info( "add-" + (t.output ? "output: " : "input: ") + t.freq );

            if (t.isOutput()) {
                this.outputs.put(t.getFrequency(), t);
            } else {
                this.inputs.put(t.getFrequency(), t);
            }

            this.updateTunnel(t.getFrequency(), t.isOutput(), false);
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

    public void removeTunnel(final PartP2PTunnel t, short freq) {
        this.outputs.remove(freq, t);
        this.inputs.remove(freq, t);
        if (this.inputs.get(t.getFrequency()).isEmpty()) {
            this.inputs.removeAll(t.getFrequency());
        }
        if (this.outputs.get(t.getFrequency()).isEmpty()) {
            this.outputs.removeAll(t.getFrequency());
        }
    }

    private void updateTunnel(final short freq, final boolean updateOutputs, final boolean configChange) {
        for (final PartP2PTunnel p : this.outputs.get(freq)) {
            if (configChange) {
                p.onTunnelConfigChange();
            }
            p.onTunnelNetworkChange();
        }

        for (final PartP2PTunnel in : this.inputs.get(freq)) {
            if (configChange) {
                in.onTunnelConfigChange();
            }
            in.onTunnelNetworkChange();
        }
    }

    public void updateFreq(final PartP2PTunnel t, final short newFrequency) {
        if (this.outputs.containsValue(t)) {
            this.outputs.remove(t.getFrequency(), t);
        }

        if (this.inputs.containsValue(t)) {
            this.inputs.remove(t.getFrequency(), t);
        }

        t.setFrequency(newFrequency);

        if (t.isOutput()) {
            this.outputs.put(t.getFrequency(), t);
        } else {
            this.inputs.put(t.getFrequency(), t);
        }

        // AELog.info( "update-" + (t.output ? "output: " : "input: ") + t.freq );
        this.updateTunnel(t.getFrequency(), t.isOutput(), true);
    }

    public short newFrequency() {
        short newFrequency;
        int cycles = 0;

        do {
            newFrequency = (short) this.frequencyGenerator.nextInt(1 << 16);
            cycles++;
        }
        while (newFrequency == 0 || this.inputs.containsKey(newFrequency));

        if (cycles > 25) {
            AELog.debug("Generating a new P2P frequency '%1$d' took %2$d cycles", newFrequency, cycles);
        }

        return newFrequency;
    }

    public TunnelCollection<PartP2PTunnel> getOutputs(final short freq, final Class<? extends PartP2PTunnel> c) {
        Collection<PartP2PTunnel> in = this.inputs.get(freq);

        if (in == null) {
            return NULL_COLLECTION;
        }

        TunnelCollection<PartP2PTunnel> out;
        for (PartP2PTunnel part : this.inputs.get(freq)) {
            out = part.getCollection(this.outputs.get(freq), c);
            if (out != null) {
                return out;
            }
        }
        return NULL_COLLECTION;
    }

    public TunnelCollection<PartP2PTunnel> getInputs(final short freq, final Class<? extends PartP2PTunnel> c) {
        Collection<PartP2PTunnel> out = this.outputs.get(freq);

        if (out == null) {
            return NULL_COLLECTION;
        }

        TunnelCollection<PartP2PTunnel> in;
        for (PartP2PTunnel part : this.outputs.get(freq)) {
            in = part.getCollection(this.inputs.get(freq), c);
            if (in != null) {
                return in;
            }
        }
        return NULL_COLLECTION;
    }
}
