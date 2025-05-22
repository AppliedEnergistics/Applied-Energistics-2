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

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

import appeng.api.config.Actionable;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkInvBlockEntity;
import appeng.core.AEConfig;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

public class VibrationChamberBlockEntity extends AENetworkInvBlockEntity implements IGridTickable, IUpgradeableObject {
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final InternalInventory invExt = new FilteredInternalInventory(this.inv, new FuelSlotFilter());

    private final IUpgradeInventory upgrades;

    private double currentFuelTicksPerTick;
    private double remainingFuelTicks = 0;
    private double fuelItemFuelTicks = 0;

    private double minFuelTicksPerTick;
    private double maxFuelTicksPerTick;
    private double initialFuelTicksPerTick;

    // client side.. (caches last sent state on server)
    public boolean isOn;

    private final double minEnergyRate;
    private final double baseMaxEnergyRate;
    private final double initialEnergyRate;

    public VibrationChamberBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(0)
                .setFlags()
                .addService(IGridTickable.class, this);

        this.upgrades = UpgradeInventories.forMachine(AEBlocks.VIBRATION_CHAMBER, 3, this::saveChanges);

        // Compute the original burn rate parameters from the config
        minEnergyRate = AEConfig.instance().getVibrationChamberMinEnergyPerGameTick();
        baseMaxEnergyRate = AEConfig.instance().getVibrationChamberMaxEnergyPerGameTick();
        initialEnergyRate = Mth.clamp(
                AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick(),
                minEnergyRate,
                baseMaxEnergyRate);

        minFuelTicksPerTick = minEnergyRate / getEnergyPerFuelTick();
        maxFuelTicksPerTick = baseMaxEnergyRate / getEnergyPerFuelTick();
        initialFuelTicksPerTick = initialEnergyRate / getEnergyPerFuelTick();
        currentFuelTicksPerTick = initialFuelTicksPerTick;
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
        this.isOn = this.getRemainingFuelTicks() > 0;
        data.writeBoolean(this.isOn);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
        data.putDouble("burnTime", this.getRemainingFuelTicks());
        data.putDouble("maxBurnTime", this.getFuelItemFuelTicks());
        // Save as percentage of max-speed
        var speed = (int) (currentFuelTicksPerTick * 100 / maxFuelTicksPerTick);
        data.putInt("burnSpeed", speed);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.setRemainingFuelTicks(data.getDouble("burnTime"));
        this.setFuelItemFuelTicks(data.getDouble("maxBurnTime"));
        this.setCurrentFuelTicksPerTick(data.getInt("burnSpeed") * maxFuelTicksPerTick / 100.0);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        upgrades.clear();
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.STORAGE)) {
            return this.getInternalInventory();
        } else if (id.equals(ISegmentedInventory.UPGRADES)) {
            return this.upgrades;
        }

        return super.getSubInventory(id);
    }

    @Override
    protected InternalInventory getExposedInventoryForSide(Direction facing) {
        return this.invExt;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (this.getRemainingFuelTicks() <= 0 && this.canEatFuel()) {
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
        if (this.getRemainingFuelTicks() <= 0) {
            this.eatFuel();
        }

        return new TickingRequest(TickRates.VibrationChamber,
                this.getRemainingFuelTicks() <= 0, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {

        // Recalculate fuel tick rate min and max
        this.minFuelTicksPerTick = minEnergyRate / getEnergyPerFuelTick();
        this.maxFuelTicksPerTick = getMaxFuelTicksPerTick();
        this.initialFuelTicksPerTick = initialEnergyRate / getEnergyPerFuelTick();

        if (this.getRemainingFuelTicks() <= 0) {
            this.eatFuel();

            if (this.getRemainingFuelTicks() > 0) {
                return TickRateModulation.URGENT;
            }

            this.setCurrentFuelTicksPerTick(this.initialFuelTicksPerTick);
            return TickRateModulation.SLEEP;
        }

        double fuelTicksConsumed = ticksSinceLastCall * currentFuelTicksPerTick;
        this.setRemainingFuelTicks(this.getRemainingFuelTicks() - fuelTicksConsumed);
        if (this.getRemainingFuelTicks() < 0) {
            fuelTicksConsumed += this.getRemainingFuelTicks();
            this.setRemainingFuelTicks(0);
        }

        // The full range should scale in 5 seconds (=100 ticks)
        var speedScalingPerTick = (this.maxFuelTicksPerTick - this.minFuelTicksPerTick) / 100;
        double speedStep = (double) ticksSinceLastCall * speedScalingPerTick;

        var grid = node.getGrid();
        var energy = grid.getEnergyService();

        // If our burn rate is zero, check if the network would now accept any power
        // And if it does, increase burn rate and tick faster
        if (Math.abs(fuelTicksConsumed - 0) < 0.01) {
            if (energy.injectPower(1, Actionable.SIMULATE) == 0) {
                this.setCurrentFuelTicksPerTick(this.getCurrentFuelTicksPerTick() + speedStep);
                return TickRateModulation.FASTER;
            }
            return TickRateModulation.IDLE;
        }

        final double newPower = fuelTicksConsumed * getEnergyPerFuelTick();
        final double overFlow = energy.injectPower(newPower, Actionable.MODULATE);

        // Speed up or slow down the burn rate, the overflow is voided
        if (overFlow > 0) {
            this.setCurrentFuelTicksPerTick(this.getCurrentFuelTicksPerTick() - speedStep);
        } else {
            this.setCurrentFuelTicksPerTick(this.getCurrentFuelTicksPerTick() + speedStep);
        }

        return overFlow > 0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
    }

    private void eatFuel() {
        final ItemStack is = this.inv.getStackInSlot(0);
        if (!is.isEmpty()) {
            final int newBurnTime = getBurnTime(is);
            if (newBurnTime > 0 && is.getCount() > 0) {
                this.setRemainingFuelTicks(this.getRemainingFuelTicks() + newBurnTime);
                this.setFuelItemFuelTicks(this.getRemainingFuelTicks());

                final Item fuelItem = is.getItem();

                if (is.getCount() <= 1) {
                    // fuel was fully consumed. for items like lava-bucket, put the remainder in the slot
                    this.inv.setItemDirect(0, fuelItem.getCraftingRemainingItem(is));
                } else {
                    is.shrink(1);
                    this.inv.setItemDirect(0, is);
                }
                this.saveChanges();
            }
        }

        if (this.getRemainingFuelTicks() > 0) {
            getMainNode().ifPresent((grid, node) -> {
                grid.getTickManager().wakeDevice(node);
            });
        }

        // state change
        if (!this.isOn && this.getRemainingFuelTicks() > 0 || this.isOn && this.getRemainingFuelTicks() <= 0) {
            this.isOn = this.getRemainingFuelTicks() > 0;
            this.markForUpdate();

            if (this.hasLevel()) {
                Platform.notifyBlocksOfNeighbors(this.level, this.worldPosition);
            }
        }
    }

    public static int getBurnTime(ItemStack is) {
        return ForgeHooks.getBurnTime(is, null);
    }

    public static boolean hasBurnTime(ItemStack is) {
        return getBurnTime(is) > 0;
    }

    public double getCurrentFuelTicksPerTick() {
        return this.currentFuelTicksPerTick;
    }

    private void setCurrentFuelTicksPerTick(double currentFuelTicksPerTick) {
        this.currentFuelTicksPerTick = Mth.clamp(currentFuelTicksPerTick, this.minFuelTicksPerTick,
                this.maxFuelTicksPerTick);
    }

    public double getFuelItemFuelTicks() {
        return this.fuelItemFuelTicks;
    }

    private void setFuelItemFuelTicks(double fuelItemFuelTicks) {
        this.fuelItemFuelTicks = fuelItemFuelTicks;
    }

    public double getRemainingFuelTicks() {
        return this.remainingFuelTicks;
    }

    private void setRemainingFuelTicks(double remainingFuelTicks) {
        this.remainingFuelTicks = remainingFuelTicks;
    }

    /**
     * AE Power generated per consumed fuel-tick.
     */
    public double getEnergyPerFuelTick() {
        return AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick()
                * (1 + this.upgrades.getInstalledUpgrades(AEItems.ENERGY_CARD) / 2.0f);
    }

    /**
     * Lowest fuel-ticks per game-tick when power is not being consumed fast enough.
     */
    public double getMinFuelTicksPerTick() {
        return this.minFuelTicksPerTick;
    }

    /**
     * Highest fuel-ticks per game-tick when all power is being consumed.
     */
    public double getMaxFuelTicksPerTick() {
        return getMaxEnergyRate() / getEnergyPerFuelTick();
    }

    public double getMaxEnergyRate() {
        return baseMaxEnergyRate + baseMaxEnergyRate * this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD) / 2.0f;
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
