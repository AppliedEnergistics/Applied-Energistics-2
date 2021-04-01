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

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.LimitedFixedItemInv;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkInvBlockEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;

public class VibrationChamberBlockEntity extends AENetworkInvBlockEntity implements IGridTickable {
    public static final double POWER_PER_TICK = 5;
    public static final int MIN_BURN_SPEED = 20;
    public static final int MAX_BURN_SPEED = 200;
    public static final double DILATION_SCALING = 25.0; // x4 ~ 40 AE/t at max
    private final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1);
    private final LimitedFixedItemInv invExt;

    private int burnSpeed = 100;
    private double burnTime = 0;
    private double maxBurnTime = 0;

    // client side..
    public boolean isOn;

    public VibrationChamberBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.getProxy().setIdlePowerUsage(0);
        this.getProxy().setFlags();

        invExt = inv.createLimitedFixedInv();
        invExt.getAllRule().filterInserts(stack -> FuelRegistry.INSTANCE.get(stack.getItem()) != null)
                .filterExtracts(stack -> FuelRegistry.INSTANCE.get(stack.getItem()) == null);
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.COVERED;
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final boolean wasOn = this.isOn;

        this.isOn = data.readBoolean();

        return wasOn != this.isOn || c; // TESR doesn't need updates!
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.getBurnTime() > 0);
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        data.putDouble("burnTime", this.getBurnTime());
        data.putDouble("maxBurnTime", this.getMaxBurnTime());
        data.putInt("burnSpeed", this.getBurnSpeed());
        return data;
    }

    @Override
    public void read(BlockState state, final CompoundNBT data) {
        super.read(state, data);
        this.setBurnTime(data.getDouble("burnTime"));
        this.setMaxBurnTime(data.getDouble("maxBurnTime"));
        this.setBurnSpeed(data.getInt("burnSpeed"));
    }

    @Override
    protected FixedItemInv getItemHandlerForSide(@Nonnull Direction facing) {
        return this.invExt;
    }

    @Override
    public FixedItemInv getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (this.getBurnTime() <= 0) {
            if (this.canEatFuel()) {
                try {
                    this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
                } catch (final GridAccessException e) {
                    // wake up!
                }
            }
        }
    }

    private boolean canEatFuel() {
        final ItemStack is = this.inv.getInvStack(0);
        if (!is.isEmpty()) {
            final Integer newBurnTime = FuelRegistry.INSTANCE.get(is.getItem());
            if (newBurnTime != null && is.getCount() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
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

        try {
            final IEnergyGrid grid = this.getProxy().getEnergy();
            final double newPower = timePassed * POWER_PER_TICK;
            final double overFlow = grid.injectPower(newPower, Actionable.SIMULATE);

            // burn the over flow.
            grid.injectPower(Math.max(0.0, newPower - overFlow), Actionable.MODULATE);

            if (overFlow > 0) {
                this.setBurnSpeed(this.getBurnSpeed() - ticksSinceLastCall);
            } else {
                this.setBurnSpeed(this.getBurnSpeed() + ticksSinceLastCall);
            }

            this.setBurnSpeed(Math.max(MIN_BURN_SPEED, Math.min(this.getBurnSpeed(), MAX_BURN_SPEED)));
            return overFlow > 0 ? TickRateModulation.SLOWER : TickRateModulation.FASTER;
        } catch (final GridAccessException e) {
            this.setBurnSpeed(this.getBurnSpeed() - ticksSinceLastCall);
            this.setBurnSpeed(Math.max(MIN_BURN_SPEED, Math.min(this.getBurnSpeed(), MAX_BURN_SPEED)));
            return TickRateModulation.SLOWER;
        }
    }

    private void eatFuel() {
        final ItemStack is = this.inv.getInvStack(0);
        if (!is.isEmpty()) {
            final Integer newBurnTime = FuelRegistry.INSTANCE.get(is.getItem());
            if (newBurnTime != null && is.getCount() > 0) {
                this.setBurnTime(this.getBurnTime() + newBurnTime);
                this.setMaxBurnTime(this.getBurnTime());

                final Item fuelItem = is.getItem();
                is.shrink(1);

                if (is.isEmpty()) {
                    this.inv.setInvStack(0, new ItemStack(fuelItem.getContainerItem()), Simulation.ACTION);
                } else {
                    this.inv.setInvStack(0, is, Simulation.ACTION);
                }
                this.saveChanges();
            }
        }

        if (this.getBurnTime() > 0) {
            try {
                this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
            } catch (final GridAccessException e) {
                // gah!
            }
        }

        // state change
        if ((!this.isOn && this.getBurnTime() > 0) || (this.isOn && this.getBurnTime() <= 0)) {
            this.isOn = this.getBurnTime() > 0;
            this.markForUpdate();

            if (this.hasWorld()) {
                Platform.notifyBlocksOfNeighbors(this.world, this.pos);
            }
        }
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

}
