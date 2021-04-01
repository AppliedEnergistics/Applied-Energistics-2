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

package appeng.parts.p2p;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.CacheInfo;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.ItemAttributes;
import alexiil.mc.lib.attributes.item.impl.CombinedFixedItemInv;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.util.Platform;

public class ItemP2PTunnelPart extends P2PTunnelPart<ItemP2PTunnelPart> implements IGridTickable {
    private static final float POWER_DRAIN = 2.0f;
    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_items");
    private boolean partVisited = false;

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private int oldSize = 0;
    private boolean requested;
    private FixedItemInv cachedInv;
    private List<FixedItemInv> cachedInvs = new ArrayList<>();

    public ItemP2PTunnelPart(final ItemStack is) {
        super(is);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return POWER_DRAIN;
    }

    @Override
    public void onNeighborUpdate(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        this.cachedInv = null;
        this.cachedInvs.clear();
        final ItemP2PTunnelPart input = this.getInput();
        if (input != null && this.isOutput()) {
            input.onTunnelNetworkChange();
        }
    }

    private FixedItemInv getDestination() {
        this.requested = true;

        if (this.cachedInv != null) {
            return this.cachedInv;
        }

        final List<FixedItemInv> outs = new ArrayList<FixedItemInv>();
        final TunnelCollection<ItemP2PTunnelPart> itemTunnels;

        try {
            itemTunnels = this.getOutputs();
        } catch (final GridAccessException e) {
            return null;
        }

        for (final ItemP2PTunnelPart t : itemTunnels) {
            final FixedItemInv inv = t.getOutputInv();
            if (inv != null && inv != this) {
                if (Platform.getRandomInt() % 2 == 0) {
                    outs.add(inv);
                } else {
                    outs.add(0, inv);
                }
            }
        }

        this.cachedInvs = outs;
        return this.cachedInv = new CombinedFixedItemInv<>(outs);
    }

    private FixedItemInv getOutputInv() {
        FixedItemInv ret = null;
        if (!this.partVisited) {
            this.partVisited = true;
            if (this.getProxy().isActive()) {
                final Direction facing = this.getSide().getFacing();
                ret = ItemAttributes.FIXED_INV.getFirstOrNullFromNeighbour(this.getTile(), facing);
            }
            this.partVisited = false;
        }
        return ret;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.ItemTunnel.getMin(), TickRates.ItemTunnel.getMax(), false, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        final boolean wasReq = this.requested;

        if (this.requested && !this.cachedInvs.isEmpty()) {
            // Don't modify the old list
            this.cachedInvs = new ArrayList<>(this.cachedInvs);
            FixedItemInv lastInv = this.cachedInvs.remove(this.cachedInvs.size() - 1);
            this.cachedInvs.add(0, lastInv);
            this.cachedInv = new CombinedFixedItemInv<>(this.cachedInvs);
        }

        this.requested = false;
        return wasReq ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    @MENetworkEventSubscribe
    public void changeStateA(final MENetworkBootingStatusChange bs) {
        if (!this.isOutput()) {
            this.cachedInv = null;
            final int olderSize = this.oldSize;
            this.oldSize = this.getDestination().getSlotCount();
            if (olderSize != this.oldSize) {
                this.getHost().notifyNeighbors();
            }
        }
    }

    @MENetworkEventSubscribe
    public void changeStateB(final MENetworkChannelsChanged bs) {
        if (!this.isOutput()) {
            this.cachedInv = null;
            final int olderSize = this.oldSize;
            this.oldSize = this.getDestination().getSlotCount();
            if (olderSize != this.oldSize) {
                this.getHost().notifyNeighbors();
            }
        }
    }

    @MENetworkEventSubscribe
    public void changeStateC(final MENetworkPowerStatusChange bs) {
        if (!this.isOutput()) {
            this.cachedInv = null;
            final int olderSize = this.oldSize;
            this.oldSize = this.getDestination().getSlotCount();
            if (olderSize != this.oldSize) {
                this.getHost().notifyNeighbors();
            }
        }
    }

    @Override
    public void onTunnelNetworkChange() {
        if (!this.isOutput()) {
            this.cachedInv = null;
            final int olderSize = this.oldSize;
            this.oldSize = this.getDestination().getSlotCount();
            if (olderSize != this.oldSize) {
                this.getHost().notifyNeighbors();
            }
        } else {
            final ItemP2PTunnelPart input = this.getInput();
            if (input != null) {
                input.getHost().notifyNeighbors();
            }
        }
    }

    @Override
    public void addAllAttributes(AttributeList<?> to) {
        to.offer(getDestination(), CacheInfo.NOT_CACHABLE);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }
}
