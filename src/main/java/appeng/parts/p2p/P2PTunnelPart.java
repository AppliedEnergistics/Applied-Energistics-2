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

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.features.P2PTunnelAttunement;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.GridFlags;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEKeyType;
import appeng.api.util.AECableType;
import appeng.core.AEConfig;
import appeng.items.tools.MemoryCardItem;
import appeng.me.service.P2PService;
import appeng.parts.AEBasePart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class P2PTunnelPart<T extends P2PTunnelPart<T>> extends AEBasePart {
    private boolean output;
    private short freq;

    public P2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode().setIdlePowerUsage(this.getPowerDrainPerTick());
        this.getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
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
        if (this.getMainNode().isOnline()) {
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
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.setOutput(data.getBooleanOr("output", false));
        this.freq = data.getShortOr("freq", (short) 0);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        data.putBoolean("output", this.isOutput());
        data.putShort("freq", this.getFrequency());
    }

    @Override
    public boolean readFromStream(RegistryFriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final short oldf = this.freq;
        this.freq = data.readShort();
        return c || oldf != this.freq;
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf data) {
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
    public boolean onUseItemOn(ItemStack heldItem, Player player, InteractionHand hand, Vec3 pos) {
        // Attunement via held item replaces the tunnel part with the desired target part type
        var newType = P2PTunnelAttunement.getTunnelPartByTriggerItem(heldItem);
        if (!newType.isEmpty() && newType.getItem() != getPartItem()
                && newType.getItem() instanceof IPartItem<?> partItem) {
            var oldOutput = isOutput();
            var myFreq = getFrequency();

            // If we were able to replace the tunnel part, copy over frequency/output state
            var tunnel = getHost().replacePart(partItem, getSide(), player, hand);
            if (!isClientSide()) {
                if (tunnel instanceof P2PTunnelPart<?> newTunnel) {
                    newTunnel.setOutput(oldOutput);
                    newTunnel.onTunnelNetworkChange();

                    newTunnel.getMainNode().ifPresent(grid -> {
                        P2PService.get(grid).updateFreq(newTunnel, myFreq);
                    });
                }
            }

            Platform.notifyBlocksOfNeighbors(getLevel(), getBlockEntity().getBlockPos());
            return true;
        }

        if (isClientSide() || hand == InteractionHand.OFF_HAND) {
            return false;
        }

        // Prefer restoring from memory card
        if (heldItem.getItem() instanceof IMemoryCard mc) {
            if (InteractionUtil.isInAlternateUseMode(player)) {
                var storedFrequency = heldItem.get(AEComponents.EXPORTED_P2P_FREQUENCY);

                short newFreq = this.getFrequency();
                final boolean wasOutput = this.isOutput();
                this.setOutput(false);

                var needsNewFrequency = wasOutput || newFreq == 0;

                var grid = getMainNode().getGrid();
                if (grid != null) {
                    var p2p = P2PService.get(grid);
                    if (needsNewFrequency) {
                        newFreq = p2p.newFrequency();
                    }

                    p2p.updateFreq(this, newFreq);
                }

                this.onTunnelConfigChange();

                MemoryCardItem.clearCard(heldItem);
                heldItem.set(AEComponents.EXPORTED_SETTINGS_SOURCE, getPartItem().asItem().getName());
                heldItem.applyComponents(exportSettings(SettingsFrom.MEMORY_CARD));

                if (needsNewFrequency) {
                    mc.notifyUser(player, MemoryCardMessages.SETTINGS_RESET);
                } else {
                    mc.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                }
                return true;
            } else {
                // Change the actual tunnel type and import settings when the encoded type is a P2P
                var p2pTunnelItem = heldItem.get(AEComponents.EXPORTED_P2P_TYPE);
                if (p2pTunnelItem instanceof IPartItem<?> partItem
                        && P2PTunnelPart.class.isAssignableFrom(partItem.getPartClass())) {
                    IPart newBus = this;
                    if (newBus.getPartItem() != partItem) {
                        newBus = this.getHost().replacePart(partItem, this.getSide(), player, hand);
                    }

                    if (newBus instanceof P2PTunnelPart<?> newTunnel) {
                        newTunnel.importSettings(SettingsFrom.MEMORY_CARD, heldItem.getComponents(), player);
                    }

                    mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                    return true;
                }
                mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
            }
            return false;
        }

        return false;
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        var frequency = input.get(AEComponents.EXPORTED_P2P_FREQUENCY);
        if (frequency != null) {
            // Only make this an output, if it's not already on the frequency.
            // Otherwise, the tunnel input may be made unusable by accidentally loading it with its own settings
            if (frequency != this.freq) {
                setOutput(true);
                var grid = getMainNode().getGrid();
                if (grid != null) {
                    P2PService.get(grid).updateFreq(this, frequency);
                } else {
                    setFrequency(frequency); // Remember it for when we actually join the grid
                    onTunnelNetworkChange();
                }

            }
        }
    }

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        super.exportSettings(mode, builder);

        // Save the tunnel type
        if (mode == SettingsFrom.MEMORY_CARD) {
            builder.set(AEComponents.EXPORTED_P2P_TYPE, getPartItem().asItem());

            if (freq != 0) {
                builder.set(AEComponents.EXPORTED_P2P_FREQUENCY, freq);

                var colors = Platform.p2p().toColors(freq);
                // the P2P freq only has 4 colors, so we stretch em out a bit
                builder.set(AEComponents.MEMORY_CARD_COLORS, new MemoryCardColors(
                        colors[0], colors[0], colors[1], colors[1],
                        colors[2], colors[2], colors[3], colors[3]));
            }
        }
    }

    public void onTunnelConfigChange() {
    }

    public void onTunnelNetworkChange() {
    }

    protected void deductEnergyCost(double energyTransported, PowerUnit typeTransported) {
        var costFactor = AEConfig.instance().getP2PTunnelEnergyTax();
        if (costFactor <= 0) {
            return;
        }

        getMainNode().ifPresent(grid -> {
            var tax = typeTransported.convertTo(PowerUnit.AE, energyTransported * costFactor);
            grid.getEnergyService().extractAEPower(tax, Actionable.MODULATE, PowerMultiplier.CONFIG);
        });
    }

    protected void deductTransportCost(long amountTransported, AEKeyType typeTransported) {
        var costFactor = AEConfig.instance().getP2PTunnelTransportTax();
        if (costFactor <= 0) {
            return;
        }

        getMainNode().ifPresent(grid -> {
            double operations = amountTransported / (double) typeTransported.getAmountPerOperation();
            double tax = operations * costFactor;
            grid.getEnergyService().extractAEPower(tax, Actionable.MODULATE, PowerMultiplier.CONFIG);
        });
    }

    /**
     * Use {@link #deductEnergyCost} or {@link #deductTransportCost}.
     */
    @Deprecated(forRemoval = true, since = "1.21.1")
    protected void queueTunnelDrain(PowerUnit unit, double f) {
        final double ae_to_tax = unit.convertTo(PowerUnit.AE, f * 0.05);

        getMainNode().ifPresent(grid -> {
            grid.getEnergyService().extractAEPower(ae_to_tax, Actionable.MODULATE, PowerMultiplier.CONFIG);
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
