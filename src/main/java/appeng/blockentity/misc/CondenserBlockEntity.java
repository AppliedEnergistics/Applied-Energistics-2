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

import java.util.Collections;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerListener;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemFilters;

public class CondenserBlockEntity extends AEBaseInvBlockEntity implements IConfigManagerListener, IConfigurableObject {

    public static final int BYTE_MULTIPLIER = 8;

    private final ConfigManager cm = new ConfigManager(this);

    private final AppEngInternalInventory outputSlot = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory storageSlot = new AppEngInternalInventory(this, 1);
    private final InternalInventory inputSlot = new CondenseItemHandler();
    private final Storage<FluidVariant> fluidHandler = new FluidHandler();
    private final MEHandler meHandler = new MEHandler();

    private final InternalInventory externalInv = new CombinedInternalInventory(this.inputSlot,
            new FilteredInternalInventory(this.outputSlot, AEItemFilters.EXTRACT_ONLY));
    private final InternalInventory combinedInv = new CombinedInternalInventory(this.inputSlot, this.outputSlot,
            this.storageSlot);

    private double storedPower = 0;

    public CondenserBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.cm.registerSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH);
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.cm.writeToNBT(data);
        data.putDouble("storedPower", this.getStoredPower());
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        this.cm.readFromNBT(data);
        this.setStoredPower(data.getDouble("storedPower"));
    }

    public double getStorage() {
        final ItemStack is = this.storageSlot.getStackInSlot(0);
        if (!is.isEmpty() && is.getItem() instanceof IStorageComponent sc) {
            if (sc.isStorageComponent(is)) {
                return sc.getBytes(is) * BYTE_MULTIPLIER;
            }
        }
        return 0;
    }

    public void addPower(final double rawPower) {
        this.setStoredPower(this.getStoredPower() + rawPower);
        this.setStoredPower(Math.max(0.0, Math.min(this.getStorage(), this.getStoredPower())));

        var requiredPower = this.getRequiredPower();
        var output = this.getOutput();
        while (requiredPower <= this.getStoredPower() && !output.isEmpty() && requiredPower > 0) {
            if (this.canAddOutput(output)) {
                this.setStoredPower(this.getStoredPower() - requiredPower);
                this.addOutput(output);
            } else {
                break;
            }
        }
    }

    private boolean canAddOutput(final ItemStack output) {
        return this.outputSlot.insertItem(0, output, true).isEmpty();
    }

    /**
     * make sure you validate with canAddOutput prior to this.
     *
     * @param output to be added output
     */
    private void addOutput(final ItemStack output) {
        this.outputSlot.insertItem(0, output, false);
    }

    InternalInventory getOutputSlot() {
        return this.outputSlot;
    }

    private ItemStack getOutput() {

        return switch (this.cm.getSetting(Settings.CONDENSER_OUTPUT)) {
            case MATTER_BALLS -> AEItems.MATTER_BALL.stack();
            case SINGULARITY -> AEItems.SINGULARITY.stack();
            default -> ItemStack.EMPTY;
        };
    }

    public double getRequiredPower() {
        return this.cm.getSetting(Settings.CONDENSER_OUTPUT).requiredPower;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.combinedInv;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
            final ItemStack removed, final ItemStack added) {
        if (inv == this.outputSlot) {
            this.meHandler.outputChanged(added, removed);
        }
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.addPower(0);
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public double getStoredPower() {
        return this.storedPower;
    }

    private void setStoredPower(final double storedPower) {
        this.storedPower = storedPower;
    }

    public InternalInventory getExternalInv() {
        return externalInv;
    }

    public Storage<FluidVariant> getFluidHandler() {
        return fluidHandler;
    }

    public MEHandler getMEHandler() {
        return meHandler;
    }

    private class CondenseItemHandler extends BaseInternalInventory {
        @Override
        public int size() {
            // We only expose the void slot
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            // The void slot never has any content
            return ItemStack.EMPTY;
        }

        @Override
        public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
            if (!stack.isEmpty()) {
                CondenserBlockEntity.this.addPower(stack.getCount());
            }
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!simulate && !stack.isEmpty()) {
                CondenserBlockEntity.this.addPower(stack.getCount());
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * A fluid handler that exposes a 1 bucket tank that can only be filled, and - when filled - will add power to this
     * condenser.
     */
    private class FluidHandler extends SnapshotParticipant<Double> implements InsertionOnlyStorage<FluidVariant> {
        private double pendingEnergy = 0;

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            // We allow up to a bucket per insert
            var amount = Math.min(AEFluidKey.AMOUNT_BUCKET, maxAmount);
            updateSnapshots(transaction);
            pendingEnergy += amount / (double) AEFluidKey.AMOUNT_BUCKET / AEKeyType.fluids().getAmountPerOperation();
            return amount;
        }

        @Override
        public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
            return Collections.emptyIterator();
        }

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
            CondenserBlockEntity.this.addPower(pendingEnergy);
            pendingEnergy = 0;
        }
    }

    /**
     * This is used to expose a fake ME subnetwork that is only composed of this condenser. The purpose of this is to
     * enable the condenser to override the {@link appeng.api.storage.MEStorage#isPreferredStorageFor} method to make
     * sure a condenser is only ever used if an item can't go anywhere else.
     */
    private class MEHandler implements IStorageMonitorableAccessor {
        private final CondenserInventory itemInventory = new CondenserInventory(CondenserBlockEntity.this);

        void outputChanged(ItemStack added, ItemStack removed) {
            this.itemInventory.updateOutput(added, removed);
        }

        @Nullable
        @Override
        public MEMonitorStorage getInventory(IActionSource src) {
            return this.itemInventory;
        }
    }
}
