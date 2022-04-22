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

package appeng.me.service;

import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.GridBootingStatusChange;
import appeng.api.networking.events.GridPowerStatusChange;
import appeng.core.AELog;
import appeng.parts.p2p.MEP2PTunnelPart;
import appeng.parts.p2p.P2PTunnelPart;

public class P2PService implements IGridService, IGridServiceProvider {
    static {
        GridHelper.addGridServiceEventHandler(GridBootingStatusChange.class, P2PService.class,
                (service, evt) -> {
                    if (!evt.isBooting()) {
                        service.wakeInputTunnels();
                    }
                });
        GridHelper.addGridServiceEventHandler(GridPowerStatusChange.class, P2PService.class,
                (service, evt) -> {
                    service.wakeInputTunnels();
                });
    }

    public static P2PService get(IGrid grid) {
        return grid.getService(P2PService.class);
    }

    private final IGrid myGrid;
    private final HashMap<Short, P2PTunnelPart> inputs = new HashMap<>();
    private final Multimap<Short, P2PTunnelPart> outputs = LinkedHashMultimap.create();
    private final Random frequencyGenerator;

    public P2PService(IGrid g) {
        this.myGrid = g;
        this.frequencyGenerator = new Random(g.hashCode());
    }

    public void wakeInputTunnels() {
        var tm = this.myGrid.getTickManager();
        for (var tunnel : this.inputs.values()) {
            if (tunnel instanceof MEP2PTunnelPart) {
                tm.wakeDevice(tunnel.getGridNode());
            }
        }
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node.getOwner() instanceof P2PTunnelPart<?>tunnel) {
            if (tunnel instanceof MEP2PTunnelPart && !node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                return;
            }

            // AELog.info( "rmv-" + (t.output ? "output: " : "input: ") + t.freq );

            if (tunnel.isOutput()) {
                this.outputs.remove(tunnel.getFrequency(), tunnel);
            } else {
                this.inputs.remove(tunnel.getFrequency());
            }

            this.updateTunnel(tunnel.getFrequency(), !tunnel.isOutput(), false);
        }
    }

    @Override
    public void addNode(IGridNode node) {
        if (node.getOwner() instanceof P2PTunnelPart<?>tunnel) {
            if (tunnel instanceof MEP2PTunnelPart && !node.hasFlag(GridFlags.REQUIRE_CHANNEL)) {
                return;
            }

            // AELog.info( "add-" + (t.output ? "output: " : "input: ") + t.freq );

            if (tunnel.isOutput()) {
                this.outputs.put(tunnel.getFrequency(), tunnel);
            } else {
                this.inputs.put(tunnel.getFrequency(), tunnel);
            }

            this.updateTunnel(tunnel.getFrequency(), !tunnel.isOutput(), false);
        }
    }

    private void updateTunnel(short freq, boolean updateOutputs, boolean configChange) {
        for (P2PTunnelPart p : this.outputs.get(freq)) {
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

    public void updateFreq(P2PTunnelPart t, short newFrequency) {
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

    public <T extends P2PTunnelPart<T>> Stream<T> getOutputs(short freq, Class<T> c) {
        // Check that a matching input exists for the requested type
        var input = this.inputs.get(freq);
        if (!c.isInstance(input)) {
            return Stream.empty();
        }

        return this.outputs.get(freq)
                .stream()
                .filter(c::isInstance)
                .map(c::cast);
    }

    public P2PTunnelPart getInput(short freq) {
        return this.inputs.get(freq);
    }
}
