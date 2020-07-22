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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.CondenserOutput;
import appeng.api.config.Settings;
import appeng.api.definitions.IMaterials;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.capabilities.Capabilities;
import appeng.core.Api;
import appeng.tile.AEBaseInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.AEItemFilters;

public class CondenserTileEntity extends AEBaseInvTileEntity implements IConfigManagerHost, IConfigurableObject {

    public static final int BYTE_MULTIPLIER = 8;

    private final ConfigManager cm = new ConfigManager(this);

    private final AppEngInternalInventory outputSlot = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory storageSlot = new AppEngInternalInventory(this, 1);
    private final IItemHandler inputSlot = new CondenseItemHandler();
    private final IFluidHandler fluidHandler = new FluidHandler();
    private final MEHandler meHandler = new MEHandler();

    private final IItemHandler externalInv = new WrapperChainedItemHandler(this.inputSlot,
            new WrapperFilteredItemHandler(this.outputSlot, AEItemFilters.EXTRACT_ONLY));
    private final IItemHandler combinedInv = new WrapperChainedItemHandler(this.inputSlot, this.outputSlot,
            this.storageSlot);

    private double storedPower = 0;

    public CondenserTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.cm.registerSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH);
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        this.cm.writeToNBT(data);
        data.putDouble("storedPower", this.getStoredPower());
        return data;
    }

    @Override
    public void read(BlockState blockState, final CompoundNBT data) {
        super.read(blockState, data);
        this.cm.readFromNBT(data);
        this.setStoredPower(data.getDouble("storedPower"));
    }

    public double getStorage() {
        final ItemStack is = this.storageSlot.getStackInSlot(0);
        if (!is.isEmpty()) {
            if (is.getItem() instanceof IStorageComponent) {
                final IStorageComponent sc = (IStorageComponent) is.getItem();
                if (sc.isStorageComponent(is)) {
                    return sc.getBytes(is) * BYTE_MULTIPLIER;
                }
            }
        }
        return 0;
    }

    public void addPower(final double rawPower) {
        this.setStoredPower(this.getStoredPower() + rawPower);
        this.setStoredPower(Math.max(0.0, Math.min(this.getStorage(), this.getStoredPower())));

        final double requiredPower = this.getRequiredPower();
        final ItemStack output = this.getOutput();
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

    IItemHandler getOutputSlot() {
        return this.outputSlot;
    }

    private ItemStack getOutput() {
        final IMaterials materials = Api.instance().definitions().materials();

        switch ((CondenserOutput) this.cm.getSetting(Settings.CONDENSER_OUTPUT)) {
            case MATTER_BALLS:
                return materials.matterBall().maybeStack(1).orElse(ItemStack.EMPTY);

            case SINGULARITY:
                return materials.singularity().maybeStack(1).orElse(ItemStack.EMPTY);

            case TRASH:
            default:
                return ItemStack.EMPTY;
        }
    }

    public double getRequiredPower() {
        return ((CondenserOutput) this.cm.getSetting(Settings.CONDENSER_OUTPUT)).requiredPower;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.combinedInv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (inv == this.outputSlot) {
            this.meHandler.outputChanged(added, removed);
        }
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.externalInv);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.fluidHandler);
        } else if (capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
            return (LazyOptional<T>) LazyOptional.of(() -> this.meHandler);
        }
        return super.getCapability(capability, facing);
    }

    private class CondenseItemHandler implements IItemHandler {

        @Override
        public int getSlots() {
            // We only expose the void slot
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot == 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            // The void slot never has any content
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0) {
                return stack;
            }
            if (!simulate && !stack.isEmpty()) {
                CondenserTileEntity.this.addPower(stack.getCount());
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    }

    /**
     * A fluid handler that exposes a 1 bucket tank that can only be filled, and -
     * when filled - will add power to this condenser.
     */
    private class FluidHandler implements IFluidTank, IFluidHandler {

        @Nonnull
        @Override
        public FluidStack getFluid() {
            return FluidStack.EMPTY;
        }

        @Override
        public int getFluidAmount() {
            return 0;
        }

        @Override
        public int getCapacity() {
            return FluidAttributes.BUCKET_VOLUME;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (action == FluidAction.EXECUTE) {
                final IStorageChannel<IAEFluidStack> chan = Api.instance().storage()
                        .getStorageChannel(IFluidStorageChannel.class);
                CondenserTileEntity.this
                        .addPower((resource.isEmpty() ? 0.0 : (double) resource.getAmount()) / chan.transferFactor());
            }

            return resource.isEmpty() ? 0 : resource.getAmount();
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? getCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return tank == 0 && isFluidValid(stack);
        }
    }

    /**
     * This is used to expose a fake ME subnetwork that is only composed of this
     * condenser tile. The purpose of this is to enable the condenser to override
     * the {@link appeng.api.storage.IMEInventoryHandler#validForPass(int)} method
     * to make sure a condenser is only ever used if an item can't go anywhere else.
     */
    private class MEHandler implements IStorageMonitorableAccessor, IStorageMonitorable {
        private final CondenserItemInventory itemInventory = new CondenserItemInventory(CondenserTileEntity.this);

        void outputChanged(ItemStack added, ItemStack removed) {
            this.itemInventory.updateOutput(added, removed);
        }

        @Nullable
        @Override
        public IStorageMonitorable getInventory(IActionSource src) {
            return this;
        }

        @Override
        public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
            if (channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
                return (IMEMonitor<T>) this.itemInventory;
            } else {
                return new CondenserVoidInventory<>(CondenserTileEntity.this, channel);
            }
        }
    }
}
