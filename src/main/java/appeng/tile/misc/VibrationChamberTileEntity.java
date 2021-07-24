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

package appeng.tile.misc;

import java.io.IOException;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.core.settings.TickRates;
import appeng.tile.grid.AENetworkInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;

public class VibrationChamberTileEntity extends AENetworkInvTileEntity implements IGridTickable {
    public static final double POWER_PER_TICK = 5;
    public static final int MIN_BURN_SPEED = 20;
    public static final int MAX_BURN_SPEED = 200;
    public static final double DILATION_SCALING = 25.0; // x4 ~ 40 AE/t at max
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final IItemHandler invExt = new WrapperFilteredItemHandler(this.inv, new FuelSlotFilter());

    private int burnSpeed = 100;
    private double burnTime = 0;
    private double maxBurnTime = 0;

    // client side..
    public boolean isOn;

    public VibrationChamberTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(0)
                .setFlags()
                .addService(IGridTickable.class, this);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final boolean wasOn = this.isOn;

        this.isOn = data.readBoolean();

        return wasOn != this.isOn || c; // TESR doesn't need updates!
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.getBurnTime() > 0);
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        data.putDouble("burnTime", this.getBurnTime());
        data.putDouble("maxBurnTime", this.getMaxBurnTime());
        data.putInt("burnSpeed", this.getBurnSpeed());
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.setBurnTime(data.getDouble("burnTime"));
        this.setMaxBurnTime(data.getDouble("maxBurnTime"));
        this.setBurnSpeed(data.getInt("burnSpeed"));
    }

    @Override
    protected IItemHandler getItemHandlerForSide(@Nonnull Direction facing) {
        return this.invExt;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
                                  final ItemStack removed, final ItemStack added) {
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
    public TickingRequest getTickingRequest(final IGridNode node) {
        if (this.getBurnTime() <= 0) {
            this.eatFuel();
        }

        return new TickingRequest(TickRates.VibrationChamber.getMin(), TickRates.VibrationChamber.getMax(),
                this.getBurnTime() <= 0, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.getBurnTime() <= 0) {
            this.eatFuel();

            if (this.getBurnTime() > 0) {
                return TickRateModulation.URGENT;
            }

            this.setBurnSpeed(100);
            return TickRateModulation.SLEEP;
        }

        this.setBurnSpeed(Math.max(MIN_BURN_SPEED, Math.min(this.getBurnSpeed(), MAX_BURN_SPEED)));
        final double dilation = this.getBurnSpeed() / DILATION_SCALING;

        double timePassed = ticksSinceLastCall * dilation;
        this.setBurnTime(this.getBurnTime() - timePassed);
        if (this.getBurnTime() < 0) {
            timePassed += this.getBurnTime();
            this.setBurnTime(0);
        }

        var grid = node.getGrid();
        if (grid != null) {
            var energy = grid.getEnergyService();
            final double newPower = timePassed * POWER_PER_TICK;
            final double overFlow = energy.injectPower(newPower, Actionable.SIMULATE);

            // burn the over flow.
            energy.injectPower(Math.max(0.0, newPower - overFlow), Actionable.MODULATE);

            if (overFlow > 0) {
                this.setBurnSpeed(this.getBurnSpeed() - ticksSinceLastCall);
            } else {
                this.setBurnSpeed(this.getBurnSpeed() + ticksSinceLastCall);
            }

            this.setBurnSpeed(Math.max(MIN_BURN_SPEED, Math.min(this.getBurnSpeed(), MAX_BURN_SPEED)));
            return overFlow > 0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
        } else {
            this.setBurnSpeed(this.getBurnSpeed() - ticksSinceLastCall);
            this.setBurnSpeed(Math.max(MIN_BURN_SPEED, Math.min(this.getBurnSpeed(), MAX_BURN_SPEED)));
            return TickRateModulation.SLOWER;
        }
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
                    this.inv.setStackInSlot(0, fuelItem.getContainerItem(is));
                } else {
                    this.inv.setStackInSlot(0, is);
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

    private static int getBurnTime(ItemStack is) {
        return ForgeHooks.getBurnTime(is, null);
    }

    private static boolean hasBurnTime(ItemStack is) {
        return getBurnTime(is) > 0;
    }

    public int getBurnSpeed() {
        return this.burnSpeed;
    }

    private void setBurnSpeed(final int burnSpeed) {
        this.burnSpeed = burnSpeed;
    }

    public double getMaxBurnTime() {
        return this.maxBurnTime;
    }

    private void setMaxBurnTime(final double maxBurnTime) {
        this.maxBurnTime = maxBurnTime;
    }

    public double getBurnTime() {
        return this.burnTime;
    }

    private void setBurnTime(final double burnTime) {
        this.burnTime = burnTime;
    }

    private static class FuelSlotFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            return !hasBurnTime(inv.getStackInSlot(slot));
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            return hasBurnTime(stack);
        }
    }
}
