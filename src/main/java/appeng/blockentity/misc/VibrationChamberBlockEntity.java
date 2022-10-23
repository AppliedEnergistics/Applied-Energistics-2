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

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.AEConfig;
import appeng.core.settings.TickRates;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class VibrationChamberBlockEntity extends AENetworkInvBlockEntity implements IGridTickable {
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new FuelSlotFilter());

    private int burnSpeed;
    private double burnTime = 0;
    private double maxBurnTime = 0;

    // client side.. (caches last sent state on server)
    public boolean isOn;

    private final int minBurnSpeed;
    private final int maxBurnSpeed;
    private final int initialBurnSpeed;

    public VibrationChamberBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(0)
                .setFlags()
                .addService(IGridTickable.class, this);

        // Compute the original burn rate parameters from the config
        var minEnergyRate = AEConfig.instance().getVibrationChamberMinEnergyPerGameTick();
        var maxEnergyRate = AEConfig.instance().getVibrationChamberMaxEnergyPerGameTick();
        var initialEnergyRate = Mth.clamp(
                AEConfig.instance().getVibrationChamberEnergyPerFuelTick(),
                minEnergyRate,
                maxEnergyRate);

        // Amount of fuel ticks we need to get minimum rate
        minBurnSpeed = (int) Math.round(minEnergyRate / getEnergyPerFuelTick() * 100);
        maxBurnSpeed = (int) Math.round(maxEnergyRate / getEnergyPerFuelTick() * 100);
        initialBurnSpeed = (int) Math.round(initialEnergyRate / getEnergyPerFuelTick() * 100);
        burnSpeed = initialBurnSpeed;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final boolean wasOn = this.isOn;

        this.isOn = data.readBoolean();

        return wasOn != this.isOn || c; // TESR doesn't need updates!
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);
        this.isOn = this.getBurnTime() > 0;
        data.writeBoolean(this.isOn);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putDouble("burnTime", this.getBurnTime());
        data.putDouble("maxBurnTime", this.getMaxBurnTime());
        data.putInt("burnSpeed", this.getBurnSpeed());
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.setBurnTime(data.getDouble("burnTime"));
        this.setMaxBurnTime(data.getDouble("maxBurnTime"));
        this.setBurnSpeed(data.getInt("burnSpeed"));
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction facing) {
        return this.invExt;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (this.getBurnTime() <= 0 && this.canEatFuel()) {
            getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }
    }

    private boolean canEatFuel() {
        final ItemStack is = this.inv.getStackInSlot(0);
        if (!is.isEmpty()) {
            final int newBurnTime = getBurnTime(is);
            if (newBurnTime > 0 && is.getCount() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        if (this.getBurnTime() <= 0) {
            this.eatFuel();
        }

        return new TickingRequest(TickRates.VibrationChamber,
                this.getBurnTime() <= 0, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.getBurnTime() <= 0) {
            this.eatFuel();

            if (this.getBurnTime() > 0) {
                return TickRateModulation.URGENT;
            }

            this.setBurnSpeed(initialBurnSpeed);
            return TickRateModulation.SLEEP;
        }

        this.setBurnSpeed(Math.max(getMinBurnSpeed(), Math.min(this.getBurnSpeed(), getMaxBurnSpeed())));
        final double fuelTicksPerTick = this.getBurnSpeed() / 100.0;

        double fuelTicksConsumed = ticksSinceLastCall * fuelTicksPerTick;
        this.setBurnTime(this.getBurnTime() - fuelTicksConsumed);
        if (this.getBurnTime() < 0) {
            fuelTicksConsumed += this.getBurnTime();
            this.setBurnTime(0);
        }

        // Increment used for speed stepping
        int speedStep = (int) Math.max(1, ticksSinceLastCall * getEnergyPerFuelTick());

        var grid = node.getGrid();
        var energy = grid.getEnergyService();

        // If our burn rate is zero, check if the network would now accept any power
        // And if it does, increase burn rate and tick faster
        if (Math.abs(fuelTicksConsumed - 0) < 0.01) {
            if (energy.injectPower(1, Actionable.SIMULATE) == 0) {
                this.setBurnSpeed(this.getBurnSpeed() + speedStep);
                this.setBurnSpeed(Math.max(getMinBurnSpeed(), Math.min(this.getBurnSpeed(), getMaxBurnSpeed())));
                return TickRateModulation.FASTER;
            }
            return TickRateModulation.IDLE;
        }

        final double newPower = fuelTicksConsumed * getEnergyPerFuelTick();
        final double overFlow = energy.injectPower(newPower, Actionable.SIMULATE);

        // burn the overflow.
        energy.injectPower(Math.max(0.0, newPower - overFlow), Actionable.MODULATE);

        // Speed up or slow down the burn rate
        if (overFlow > 0) {
            this.setBurnSpeed(this.getBurnSpeed() - speedStep);
        } else {
            this.setBurnSpeed(this.getBurnSpeed() + speedStep);
        }

        this.setBurnSpeed(Math.max(getMinBurnSpeed(), Math.min(this.getBurnSpeed(), getMaxBurnSpeed())));
        return overFlow > 0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
    }

    private void eatFuel() {
        final ItemStack is = this.inv.getStackInSlot(0);
        if (!is.isEmpty()) {
            final int newBurnTime = getBurnTime(is);
            if (newBurnTime > 0 && is.getCount() > 0) {
                this.setBurnTime(this.getBurnTime() + newBurnTime);
                this.setMaxBurnTime(this.getBurnTime());

                final Item fuelItem = is.getItem();
                is.shrink(1);

                if (is.isEmpty()) {
                    this.inv.setItemDirect(0, new ItemStack(fuelItem.getCraftingRemainingItem()));
                } else {
                    this.inv.setItemDirect(0, is);
                }
                this.saveChanges();
            }
        }

        if (this.getBurnTime() > 0) {
            getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }

        // state change
        if (!this.isOn && this.getBurnTime() > 0 || this.isOn && this.getBurnTime() <= 0) {
            this.isOn = this.getBurnTime() > 0;
            this.markForUpdate();

            if (this.hasLevel()) {
                Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
            }
        }
    }

    public static int getBurnTime(ItemStack is) {
        var burnTime = FuelRegistry.INSTANCE.get(is.getItem());
        return burnTime != null ? burnTime : 0;
    }

    public static boolean hasBurnTime(ItemStack is) {
        return getBurnTime(is) > 0;
    }

    public int getBurnSpeed() {
        return this.burnSpeed;
    }

    private void setBurnSpeed(int burnSpeed) {
        this.burnSpeed = burnSpeed;
    }

    public double getMaxBurnTime() {
        return this.maxBurnTime;
    }

    private void setMaxBurnTime(double maxBurnTime) {
        this.maxBurnTime = maxBurnTime;
    }

    public double getBurnTime() {
        return this.burnTime;
    }

    private void setBurnTime(double burnTime) {
        this.burnTime = burnTime;
    }

    /**
     * AE Power generated per consumed burn-time-tick of fuel.
     */
    public double getEnergyPerFuelTick() {
        return AEConfig.instance().getVibrationChamberEnergyPerFuelTick();
    }

    /**
     * Lowest throttle percentage when power is not being consumed fast enough.
     */
    public int getMinBurnSpeed() {
        return minBurnSpeed;
    }

    /**
     * Highest throttle percentage when power is not being consumed fast enough.
     */
    public int getMaxBurnSpeed() {
        return maxBurnSpeed;
    }

    private static class FuelSlotFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return !hasBurnTime(inv.getStackInSlot(slot));
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return hasBurnTime(stack);
        }
    }
}
