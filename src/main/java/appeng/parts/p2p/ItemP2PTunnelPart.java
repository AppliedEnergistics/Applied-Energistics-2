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

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

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
import appeng.util.inv.WrapperChainedItemHandler;

public class ItemP2PTunnelPart extends P2PTunnelPart<ItemP2PTunnelPart> implements IItemHandler, IGridTickable {
    private static final float POWER_DRAIN = 2.0f;
    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_items");
    private boolean partVisited = false;

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private int oldSize = 0;
    private boolean requested;
    private IItemHandler cachedInv;

    public ItemP2PTunnelPart(final ItemStack is) {
        super(is);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return POWER_DRAIN;
    }

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        this.cachedInv = null;
        final ItemP2PTunnelPart input = this.getInput();
        if (input != null && this.isOutput()) {
            input.onTunnelNetworkChange();
        }
    }

    private IItemHandler getDestination() {
        this.requested = true;

        if (this.cachedInv != null) {
            return this.cachedInv;
        }

        final List<IItemHandler> outs = new ArrayList<IItemHandler>();
        final TunnelCollection<ItemP2PTunnelPart> itemTunnels;

        try {
            itemTunnels = this.getOutputs();
        } catch (final GridAccessException e) {
            return EmptyHandler.INSTANCE;
        }

        for (final ItemP2PTunnelPart t : itemTunnels) {
            final IItemHandler inv = t.getOutputInv();
            if (inv != null && inv != this) {
                if (Platform.getRandomInt() % 2 == 0) {
                    outs.add(inv);
                } else {
                    outs.add(0, inv);
                }
            }
        }

        return this.cachedInv = new WrapperChainedItemHandler(outs.toArray(new IItemHandler[outs.size()]));
    }

    private IItemHandler getOutputInv() {
        IItemHandler ret = null;
        if (!this.partVisited) {
            this.partVisited = true;
            if (this.getProxy().isActive()) {
                final Direction facing = this.getSide().getFacing();
                final TileEntity te = this.getTile().getWorld().getTileEntity(this.getTile().getPos().offset(facing));

                if (te != null) {
                    ret = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                            .orElse(ret);
                }
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

        if (this.requested && this.cachedInv != null) {
            ((WrapperChainedItemHandler) this.cachedInv).cycleOrder();
        }

        this.requested = false;
        return wasReq ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

	private void notifyHostNeighbors() {
		this.cachedInv = null;
		final int olderSize = this.oldSize;
		this.oldSize = this.getDestination().getSlots();
		if (olderSize != this.oldSize) {
			this.getHost().notifyNeighbors();
		}
	}

    @MENetworkEventSubscribe
    public void changeStateA(final MENetworkBootingStatusChange bs) {
        if (!this.isOutput()) {
			notifyHostNeighbors();
        }
    }

    @MENetworkEventSubscribe
    public void changeStateB(final MENetworkChannelsChanged bs) {
        if (!this.isOutput()) {
			notifyHostNeighbors();
        }
    }

    @MENetworkEventSubscribe
    public void changeStateC(final MENetworkPowerStatusChange bs) {
        if (!this.isOutput()) {
			notifyHostNeighbors();
        }
    }

    @Override
    public void onTunnelNetworkChange() {
        if (!this.isOutput()) {
			notifyHostNeighbors();
        } else {
            final ItemP2PTunnelPart input = this.getInput();
            if (input != null) {
                input.getHost().notifyNeighbors();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capabilityClass) {
        if (capabilityClass == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (LazyOptional<T>) LazyOptional.of(() -> this);
        }

        return super.getCapability(capabilityClass);
    }

    @Override
    public int getSlots() {
        return this.getDestination().getSlots();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.getDestination().isItemValid(slot, stack);
    }

    @Override
    public ItemStack getStackInSlot(final int i) {
        return this.getDestination().getStackInSlot(i);
    }

    @Override
    public ItemStack insertItem(final int slot, final ItemStack stack, boolean simulate) {
        return this.getDestination().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(final int slot, final int amount, boolean simulate) {
        return this.getDestination().extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.getDestination().getSlotLimit(slot);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }
}
