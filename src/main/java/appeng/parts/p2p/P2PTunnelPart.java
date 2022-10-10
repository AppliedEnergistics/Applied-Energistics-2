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

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.client.render.cablebus.P2PTunnelFrequencyModelData;
import appeng.core.AEConfig;
import appeng.me.service.P2PService;
import appeng.parts.BasicStatePart;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class P2PTunnelPart<T extends P2PTunnelPart<T>> extends BasicStatePart {
    private static final String CONFIG_NBT_TYPE = "p2pType";
    private static final String CONFIG_NBT_FREQ = "p2pFreq";

    private boolean output;
    private short freq;

    public P2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(this.getPowerDrainPerTick());
    }

    protected float getPowerDrainPerTick() {
        return 1.0f;
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

    public List<T> getOutputs() {
        return getOutputStream().toList();
    }

    public Stream<T> getOutputStream() {
        if (this.getMainNode().isActive()) {
            var grid = getMainNode().getGrid();
            if (grid != null) {
                return P2PService.get(grid).getOutputs(this.getFrequency(), this.getClass());
            }
        }
        return Stream.empty();
    }

    @Override
    public void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(5, 5, 12, 11, 11, 13);
        bch.addBox(3, 3, 13, 13, 13, 14);
        bch.addBox(2, 2, 14, 14, 14, 16);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.setOutput(data.getBoolean("output"));
        this.freq = data.getShort("freq");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putBoolean("output", this.isOutput());
        data.putShort("freq", this.getFrequency());
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final short oldf = this.freq;
        this.freq = data.readShort();
        return c || oldf != this.freq;
    }

    @Override
    public void writeToStream(FriendlyByteBuf data) {
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
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (isClientSide()) {
            return true;
        }

        if (hand == InteractionHand.OFF_HAND) {
            return false;
        }

        var is = player.getItemInHand(hand);

        // Prefer restoring from memory card
        if (!is.isEmpty() && is.getItem() instanceof IMemoryCard mc) {
            var configData = mc.getData(is);

            // Change the actual tunnel type and import settings when the encoded type is a P2P
            var partItem = IPartItem.byId(new ResourceLocation(configData.getString(CONFIG_NBT_TYPE)));
            if (partItem != null && P2PTunnelPart.class.isAssignableFrom(partItem.getPartClass())) {
                IPart newBus = this;
                if (newBus.getPartItem() != partItem) {
                    newBus = this.getHost().replacePart(partItem, this.getSide(), player, hand);
                }

                if (newBus instanceof P2PTunnelPart<?>newTunnel) {
                    newTunnel.importSettings(SettingsFrom.MEMORY_CARD, configData, player);
                }

                mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                return true;
            }
            mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
            return false;
        }

        // Attunement via held item replaces the tunnel part with the desired target part type
        var newType = P2PTunnelAttunement.getTunnelPartByTriggerItem(is);
        if (!newType.isEmpty() && newType.getItem() != getPartItem()
                && newType.getItem() instanceof IPartItem<?>partItem) {
            var oldOutput = isOutput();
            var myFreq = getFrequency();

            // If we were able to replace the tunnel part, copy over frequency/output state
            var tunnel = getHost().replacePart(partItem, getSide(), player, hand);
            if (tunnel instanceof P2PTunnelPart newTunnel) {
                newTunnel.setOutput(oldOutput);
                newTunnel.onTunnelNetworkChange();

                newTunnel.getMainNode().ifPresent(grid -> {
                    P2PService.get(grid).updateFreq(newTunnel, myFreq);
                });
            }

            Platform.notifyBlocksOfNeighbors(getLevel(), getBlockEntity().getBlockPos());
            return true;
        }

        return false;
    }

    @Override
    public boolean onPartShiftActivate(Player player, InteractionHand hand, Vec3 pos) {
        final ItemStack is = player.getInventory().getSelected();
        if (!is.isEmpty() && is.getItem() instanceof IMemoryCard mc) {
            if (isClientSide()) {
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

            var type = getPartItem().asItem().getDescriptionId();

            exportSettings(SettingsFrom.MEMORY_CARD, data);

            mc.setMemoryCardContents(is, type, data);
            if (needsNewFrequency) {
                mc.notifyUser(player, MemoryCardMessages.SETTINGS_RESET);
            } else {
                mc.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
            }
            return true;
        }
        return false;
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (input.contains(CONFIG_NBT_FREQ, Tag.TAG_SHORT)) {
            var freq = input.getShort(CONFIG_NBT_FREQ);

            // Only make this an output, if it's not already on the frequency.
            // Otherwise, the tunnel input may be made unusable by accidentally loading it with its own settings
            if (freq != this.freq) {
                setOutput(true);
                var grid = getMainNode().getGrid();
                if (grid != null) {
                    P2PService.get(grid).updateFreq(this, freq);
                } else {
                    setFrequency(freq); // Remember it for when we actually join the grid
                    onTunnelNetworkChange();
                }

            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output) {
        super.exportSettings(mode, output);

        // Save the tunnel type
        if (mode == SettingsFrom.MEMORY_CARD) {
            output.putString(CONFIG_NBT_TYPE, IPartItem.getId(getPartItem()).toString());

            if (freq != 0) {
                output.putShort(CONFIG_NBT_FREQ, freq);

                var colors = Platform.p2p().toColors(freq);
                var colorCode = new int[] { colors[0].ordinal(), colors[0].ordinal(), colors[1].ordinal(),
                        colors[1].ordinal(), colors[2].ordinal(), colors[2].ordinal(), colors[3].ordinal(),
                        colors[3].ordinal(), };
                output.putIntArray(IMemoryCard.NBT_COLOR_CODE, colorCode);
            }
        }
    }

    public void onTunnelConfigChange() {
    }

    public void onTunnelNetworkChange() {

    }

    protected void queueTunnelDrain(PowerUnits unit, double f) {
        final double ae_to_tax = unit.convertTo(PowerUnits.AE, f * AEConfig.TUNNEL_POWER_LOSS);

        getMainNode().ifPresent(grid -> {
            grid.getEnergyService().extractAEPower(ae_to_tax, Actionable.MODULATE, PowerMultiplier.ONE);
        });
    }

    public short getFrequency() {
        return this.freq;
    }

    public void setFrequency(short freq) {
        final short oldf = this.freq;
        this.freq = freq;
        if (oldf != this.freq) {
            this.getHost().markForSave();
            this.getHost().markForUpdate();
        }
    }

    public boolean isOutput() {
        return this.output;
    }

    void setOutput(boolean output) {
        this.output = output;
        this.getHost().markForSave();
    }

    @Override
    public ModelData getModelData() {
        long ret = Short.toUnsignedLong(this.getFrequency());

        if (this.isActive() && this.isPowered()) {
            ret |= 0x10000L;
        }

        return ModelData.builder()
                .with(P2PTunnelFrequencyModelData.FREQUENCY, ret)
                .build();
    }
}
