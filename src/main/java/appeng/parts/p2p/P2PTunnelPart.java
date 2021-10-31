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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.features.P2PTunnelAttunementInternal;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.definitions.AEParts;
import appeng.me.service.P2PService;
import appeng.me.service.helpers.TunnelCollection;
import appeng.parts.BasicStatePart;
import appeng.util.Platform;

public abstract class P2PTunnelPart<T extends P2PTunnelPart> extends BasicStatePart {
    private final TunnelCollection type = new TunnelCollection<T>(null, this.getClass());
    private boolean output;
    private short freq;
    private final EnergyDrainHandler energyDrainHandler = new EnergyDrainHandler();

    public P2PTunnelPart(final ItemStack is) {
        super(is);
        this.getMainNode().setIdlePowerUsage(this.getPowerDrainPerTick());
    }

    protected float getPowerDrainPerTick() {
        return 1.0f;
    }

    public TunnelCollection<T> getCollection(final Collection<P2PTunnelPart> collection,
            final Class<? extends P2PTunnelPart> c) {
        if (this.type.matches(c)) {
            this.type.setSource(collection);
            return this.type;
        }

        return null;
    }

    @Nullable
    public T getInput() {
        if (this.getFrequency() == 0) {
            return null;
        }

        var grid = getMainNode().getGrid();
        if (grid != null) {
            var tunnel = P2PService.get(grid).getInput(this.getFrequency());
            if (this.getClass().isInstance(tunnel)) {
                return (T) tunnel;
            }
        }
        return null;
    }

    public TunnelCollection<T> getOutputs() {
        if (this.getMainNode().isActive()) {
            var grid = getMainNode().getGrid();
            if (grid != null) {
                return (TunnelCollection<T>) P2PService.get(grid).getOutputs(this.getFrequency(), this.getClass());
            }
        }
        return new TunnelCollection(new ArrayList(), this.getClass());
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(5, 5, 12, 11, 11, 13);
        bch.addBox(3, 3, 13, 13, 13, 14);
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {
        if (type == PartItemStack.WORLD || type == PartItemStack.NETWORK || type == PartItemStack.WRENCH
                || type == PartItemStack.PICK) {
            return super.getItemStack(type);
        }

        return AEParts.ME_P2P_TUNNEL.stack();
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.setOutput(data.getBoolean("output"));
        this.freq = data.getShort("freq");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("output", this.isOutput());
        data.putShort("freq", this.getFrequency());
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final short oldf = this.freq;
        this.freq = data.readShort();
        return c || oldf != this.freq;
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeShort(this.getFrequency());
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    @Override
    public boolean useStandardMemoryCard() {
        return false;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (isRemote()) {
            return true;
        }

        if (hand == InteractionHand.OFF_HAND) {
            return false;
        }

        final ItemStack is = player.getItemInHand(hand);

        var tt = P2PTunnelAttunement.getTunnelTypeByItem(is);
        if (!is.isEmpty() && is.getItem() instanceof IMemoryCard mc) {
            final CompoundTag data = mc.getData(is);

            final ItemStack newType = ItemStack.of(data);
            final short freq = data.getShort("freq");

            if (!newType.isEmpty() && newType.getItem() instanceof IPartItem) {
                final IPart testPart = ((IPartItem<?>) newType.getItem()).createPart(newType);
                if (testPart instanceof P2PTunnelPart) {
                    final boolean dir = this.getHost().replacePart(newType, this.getSide(), player, hand);
                    final IPart newBus = this.getHost().getPart(this.getSide());

                    if (newBus instanceof P2PTunnelPart<?>newTunnel) {
                        newTunnel.setOutput(true);

                        newTunnel.getMainNode().ifPresent(grid -> {
                            P2PService.get(grid).updateFreq(newTunnel, freq);
                        });

                        newTunnel.onTunnelNetworkChange();
                    }

                    mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    return true;
                }
            }
            mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
        } else if (tt != null) // attunement
        {
            var newType = P2PTunnelAttunementInternal.getTunnelPart(tt);

            if (!newType.isEmpty() && !ItemStack.isSame(newType, this.getItemStack())) {
                final boolean oldOutput = this.isOutput();
                final short myFreq = this.getFrequency();

                final var partAdded = this.getHost().replacePart(newType, this.getSide(), player, hand);
                final IPart newBus = this.getHost().getPart(this.getSide());

                if (newBus instanceof P2PTunnelPart newTunnel) {
                    newTunnel.setOutput(oldOutput);
                    newTunnel.onTunnelNetworkChange();

                    newTunnel.getMainNode().ifPresent(grid -> {
                        P2PService.get(grid).updateFreq(newTunnel, myFreq);
                    });
                }

                Platform.notifyBlocksOfNeighbors(this.getBlockEntity().getLevel(), this.getBlockEntity().getBlockPos());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onPartShiftActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        final ItemStack is = player.getInventory().getSelected();
        if (!is.isEmpty() && is.getItem() instanceof IMemoryCard mc) {
            if (isRemote()) {
                return true;
            }

            final CompoundTag data = mc.getData(is);
            final short storedFrequency = data.getShort("freq");

            short newFreq = this.getFrequency();
            final boolean wasOutput = this.isOutput();
            this.setOutput(false);

            final boolean needsNewFrequency = wasOutput || this.getFrequency() == 0 || storedFrequency == newFreq;

            var grid = getMainNode().getGrid();
            if (grid != null) {
                var p2p = P2PService.get(grid);
                if (needsNewFrequency) {
                    newFreq = p2p.newFrequency();
                }

                p2p.updateFreq(this, newFreq);
            }

            this.onTunnelConfigChange();

            final ItemStack p2pItem = this.getItemStack(PartItemStack.WRENCH);
            final String type = p2pItem.getDescriptionId();

            p2pItem.save(data);
            data.putShort("freq", this.getFrequency());

            final AEColor[] colors = Platform.p2p().toColors(this.getFrequency());
            final int[] colorCode = new int[] { colors[0].ordinal(), colors[0].ordinal(), colors[1].ordinal(),
                    colors[1].ordinal(), colors[2].ordinal(), colors[2].ordinal(), colors[3].ordinal(),
                    colors[3].ordinal(), };

            data.putIntArray("colorCode", colorCode);

            mc.setMemoryCardContents(is, type + ".name", data);
            if (needsNewFrequency) {
                mc.notifyUser(player, MemoryCardMessages.SETTINGS_RESET);
            } else {
                mc.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
            }
            return true;
        }
        return false;
    }

    public void onTunnelConfigChange() {
    }

    public void onTunnelNetworkChange() {

    }

    protected void queueTunnelDrain(final PowerUnits unit, final double f, TransactionContext transaction) {
        final double ae_to_tax = unit.convertTo(PowerUnits.AE, f * AEConfig.TUNNEL_POWER_LOSS);

        energyDrainHandler.updateSnapshots(transaction);
        energyDrainHandler.pendingEnergy += ae_to_tax;
    }

    protected void queueTunnelDrain(final PowerUnits unit, final double f) {
        final double ae_to_tax = unit.convertTo(PowerUnits.AE, f * AEConfig.TUNNEL_POWER_LOSS);

        getMainNode().ifPresent(grid -> {
            grid.getEnergyService().extractAEPower(ae_to_tax, Actionable.MODULATE, PowerMultiplier.ONE);
        });
    }

    public short getFrequency() {
        return this.freq;
    }

    public void setFrequency(final short freq) {
        final short oldf = this.freq;
        this.freq = freq;
        if (oldf != this.freq) {
            this.getHost().markForUpdate();
        }
    }

    public boolean isOutput() {
        return this.output;
    }

    void setOutput(final boolean output) {
        this.output = output;
    }

    @Override
    public Object getRenderAttachmentData() {
        long ret = Short.toUnsignedLong(this.getFrequency());

        if (this.isActive() && this.isPowered()) {
            ret |= 0x10000L;
        }

        return ret;
    }

    private class EnergyDrainHandler extends SnapshotParticipant<Double> {
        private double pendingEnergy;

        @Override
        protected Double createSnapshot() {
            return pendingEnergy;
        }

        @Override
        protected void readSnapshot(Double snapshot) {
            pendingEnergy = snapshot;
        }

        @Override
        protected void onFinalCommit() {
            if (pendingEnergy > 0) {
                getMainNode().ifPresent(grid -> {
                    grid.getEnergyService().extractAEPower(pendingEnergy, Actionable.MODULATE, PowerMultiplier.ONE);
                });
                pendingEnergy = 0;
            }
        }
    }
}
