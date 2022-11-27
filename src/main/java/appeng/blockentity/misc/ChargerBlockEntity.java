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

package appeng.blockentity.misc;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEItemKey;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalBlockPos;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class ChargerBlockEntity extends AENetworkPowerBlockEntity implements IGridTickable {
    private static final int POWER_MAXIMUM_AMOUNT = 1600;
    private static final int POWER_THRESHOLD = POWER_MAXIMUM_AMOUNT - 1;
    private boolean working;

    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1, 1, new ChargerInvFilter());

    public ChargerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setFlags()
                .setIdlePowerUsage(0)
                .addService(IGridTickable.class, this);
        this.setInternalMaxPower(POWER_MAXIMUM_AMOUNT);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public void onReady() {
        var validSides = EnumSet.allOf(Direction.class);
        validSides.remove(this.getForward());

        this.getMainNode().setExposedOnSides(validSides);
        super.onReady();
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        var changed = super.readFromStream(data);

        this.working = data.readBoolean();

        if (data.readBoolean()) {
            var item = AEItemKey.fromPacket(data);
            this.inv.setItemDirect(0, item.toStack());
        } else {
            this.inv.setItemDirect(0, ItemStack.EMPTY);
        }

        return changed; // TESR doesn't need updates!
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeBoolean(working);
        var is = AEItemKey.of(this.inv.getStackInSlot(0));
        data.writeBoolean(is != null);
        if (is != null) {
            is.writeToPacket(data);
        }
    }

    @Override
    public void setOrientation(Direction inForward, Direction inUp) {
        super.setOrientation(inForward, inUp);

        var validSides = EnumSet.allOf(Direction.class);
        validSides.remove(this.getForward());

        this.getMainNode().setExposedOnSides(validSides);
        this.setPowerSides(validSides);
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().wakeDevice(node);
        });

        this.markForUpdate();
    }

    public void activate(Player player) {
        if (!Platform.hasPermissions(new DimensionalBlockPos(this), player)) {
            return;
        }

        final ItemStack myItem = this.inv.getStackInSlot(0);
        if (myItem.isEmpty()) {
            ItemStack held = player.getInventory().getSelected();

            if (AEItems.CERTUS_QUARTZ_CRYSTAL.isSameAs(held)
                    || Platform.isChargeable(held)) {
                held = player.getInventory().removeItem(player.getInventory().selected, 1);
                this.inv.setItemDirect(0, held);
            }
        } else {
            final List<ItemStack> drops = new ArrayList<>();
            drops.add(myItem);
            this.inv.setItemDirect(0, ItemStack.EMPTY);
            Platform.spawnDrops(this.level, this.worldPosition.relative(this.getForward()), drops);
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Charger, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        doWork(ticksSinceLastCall);
        return TickRateModulation.FASTER;
    }

    private void doWork(int ticksSinceLastCall) {
        var wasWorking = this.working;
        this.working = false;
        var changed = false;

        var myItem = this.inv.getStackInSlot(0);

        if (!myItem.isEmpty()) {

            if (Platform.isChargeable(myItem)) {
                var ps = (IAEItemPowerStorage) myItem.getItem();

                var currentPower = ps.getAECurrentPower(myItem);
                var maxPower = ps.getAEMaxPower(myItem);
                if (currentPower < maxPower) {
                    // Since we specify the charge rate in "per tick", calculate it per tick of the charger,
                    // which only ticks once every few actual game ticks.
                    var chargeRate = ps.getChargeRate(myItem) * ticksSinceLastCall
                            * AEConfig.instance().getChargerChargeRate();

                    // First charge from the local buffer
                    double extractedAmount = this.extractAEPower(chargeRate, Actionable.MODULATE,
                            PowerMultiplier.CONFIG);

                    var missingChargeRate = chargeRate - extractedAmount;
                    var missingAEPower = maxPower - currentPower;
                    var toExtract = Math.min(missingChargeRate, missingAEPower);

                    // Then directly extract from the grid
                    var grid = getMainNode().getGrid();
                    if (grid != null) {
                        extractedAmount += grid.getEnergyService().extractAEPower(toExtract, Actionable.MODULATE,
                                PowerMultiplier.ONE);
                    }

                    if (extractedAmount > 0) {
                        var adjustment = ps.injectAEPower(myItem, extractedAmount, Actionable.MODULATE);

                        this.setInternalCurrentPower(this.getInternalCurrentPower() + adjustment);

                        this.working = true;
                        changed = true;
                    }
                }
            } else if (this.getInternalCurrentPower() > POWER_THRESHOLD
                    && AEItems.CERTUS_QUARTZ_CRYSTAL.isSameAs(myItem)) {
                this.working = true;
                if (Platform.getRandomFloat() > 0.8f) {
                    this.extractAEPower(this.getInternalMaxPower(), Actionable.MODULATE, PowerMultiplier.CONFIG);

                    ItemStack charged = AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.stack(myItem.getCount());
                    this.inv.setItemDirect(0, charged);

                    changed = true;
                }
            }
        }

        // charge from the network!
        if (this.getInternalCurrentPower() < POWER_THRESHOLD) {
            getMainNode().ifPresent(grid -> {
                double toExtract = Math.min(800.0, this.getInternalMaxPower() - this.getInternalCurrentPower());
                final double extracted = grid.getEnergyService().extractAEPower(toExtract, Actionable.MODULATE,
                        PowerMultiplier.ONE);

                this.injectExternalPower(PowerUnits.AE, extracted, Actionable.MODULATE);
            });

            changed = true;
        }

        if (changed || this.working != wasWorking) {
            this.markForUpdate();
        }
    }

    public boolean isWorking() {
        return working;
    }

    private static class ChargerInvFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inv, int i, ItemStack itemstack) {
            return Platform.isChargeable(itemstack) || AEItems.CERTUS_QUARTZ_CRYSTAL.isSameAs(itemstack);
        }

        @Override
        public boolean allowExtract(InternalInventory inv, int slotIndex, int amount) {
            ItemStack extractedItem = inv.getStackInSlot(slotIndex);

            if (Platform.isChargeable(extractedItem)) {
                final IAEItemPowerStorage ips = (IAEItemPowerStorage) extractedItem.getItem();
                if (ips.getAECurrentPower(extractedItem) >= ips.getAEMaxPower(extractedItem)) {
                    return true;
                }
            }

            return AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.isSameAs(extractedItem);
        }
    }
}
