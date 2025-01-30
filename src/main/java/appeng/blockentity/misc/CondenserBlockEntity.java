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

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.MEStorage;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.util.ConfigManager;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.AEItemFilters;

public class CondenserBlockEntity extends AEBaseInvBlockEntity implements IConfigurableObject {

    public static final int BYTE_MULTIPLIER = 8;

    private final ConfigManager cm = new ConfigManager(() -> {
        saveChanges();
        addPower(0);
    });

    private final AppEngInternalInventory outputSlot = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory storageSlot = new AppEngInternalInventory(this, 1);
    private final InternalInventory inputSlot = new CondenseItemHandler();
    private final Storage<FluidVariant> fluidHandler = new CondenseStorage<>(
            1.0 / AEKeyType.fluids().getAmountPerOperation(),
            AEFluidKey.AMOUNT_BUCKET);

    /**
     * This is used to expose a fake ME subnetwork that is only composed of this condenser. The purpose of this is to
     * enable the condenser to override the {@link appeng.api.storage.MEStorage#isPreferredStorageFor} method to make
     * sure a condenser is only ever used if an item can't go anywhere else.
     */
    private final CondenserMEStorage meStorage = new CondenserMEStorage(this);

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
    public void loadTag(CompoundTag data) {
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

    public void addPower(double rawPower) {
        this.setStoredPower(this.getStoredPower() + rawPower);
        this.setStoredPower(Math.max(0.0, Math.min(this.getStorage(), this.getStoredPower())));
        fillOutput();
    }

    private void fillOutput() {
        var requiredPower = this.getRequiredPower();
        while (requiredPower <= this.getStoredPower() && !getOutput().isEmpty() && requiredPower > 0) {
            if (this.canAddOutput()) {
                this.setStoredPower(this.getStoredPower() - requiredPower);
                this.addOutput();
            } else {
                break;
            }
        }
    }

    boolean canAddOutput() {
        return this.outputSlot.insertItem(0, getOutput(), true).isEmpty();
    }

    /**
     * make sure you validate with canAddOutput prior to this.
     */
    private void addOutput() {
        this.outputSlot.insertItem(0, getOutput(), false);
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
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (inv == outputSlot)
            fillOutput();
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public double getStoredPower() {
        return this.storedPower;
    }

    private void setStoredPower(double storedPower) {
        this.storedPower = storedPower;
    }

    public InternalInventory getExternalInv() {
        return externalInv;
    }

    public Storage<FluidVariant> getFluidHandler() {
        return fluidHandler;
    }

    public MEStorage getMEStorage() {
        return meStorage;
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
        public boolean isItemValid(int slot, ItemStack stack) {
            return canAddOutput();
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            if (!stack.isEmpty()) {
                CondenserBlockEntity.this.addPower(stack.getCount());
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!canAddOutput()) {
                return stack;
            }
            if (!simulate && !stack.isEmpty()) {
                CondenserBlockEntity.this.addPower(stack.getCount());
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        protected Storage<ItemVariant> createStorage() {
            return new CondenseStorage<>(1.0, Long.MAX_VALUE);
        }
    }

    private class CondenseStorage<T> extends SnapshotParticipant<Double> implements InsertionOnlyStorage<T> {
        private double pendingEnergy = 0;
        private final double energyFactor;
        private final long maxAmountPerOperation;

        public CondenseStorage(double energyFactor, long maxAmountPerOperation) {
            this.energyFactor = energyFactor;
            this.maxAmountPerOperation = maxAmountPerOperation;
        }

        @Override
        public long insert(T resource, long maxAmount, TransactionContext transaction) {
            // Clamp the amount per operation
            var amount = Math.min(maxAmountPerOperation, maxAmount);
            updateSnapshots(transaction);
            pendingEnergy += amount * energyFactor;
            return amount;
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
}
