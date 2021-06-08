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

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.ListenerRemovalToken;
import alexiil.mc.lib.attributes.ListenerToken;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.InvMarkDirtyListener;
import alexiil.mc.lib.attributes.item.LimitedFixedItemInv;

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
import appeng.core.Api;
import appeng.tile.AEBaseInvTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;

public class CondenserTileEntity extends AEBaseInvTileEntity implements IConfigManagerHost, IConfigurableObject {

    public static final int BYTE_MULTIPLIER = 8;

    private final ConfigManager cm = new ConfigManager(this);

    private final AppEngInternalInventory outputSlot = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory storageSlot = new AppEngInternalInventory(this, 1);
    // This is a FixedItemInv implementation to satisfy the UI, which is slot based
    private final CondenseItemHandler internalInputSlot = new CondenseItemHandler();
    private final FluidInsertable externalFluidInput = new FluidHandler();
    private final MEHandler meHandler = new MEHandler();

    private final FixedItemInv combinedInv = new WrapperChainedItemHandler(this.internalInputSlot, this.outputSlot,
            this.storageSlot);

    // Only makes input+output visible and limits output to extraction
    private final FixedItemInv externalCombinedInv;

    private double storedPower = 0;

    public CondenserTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.cm.registerSetting(Settings.CONDENSER_OUTPUT, CondenserOutput.TRASH);

        LimitedFixedItemInv limitedOutput = this.outputSlot.createLimitedFixedInv();
        limitedOutput.getAllRule().disallowInsertion();
        externalCombinedInv = new WrapperChainedItemHandler(this.internalInputSlot, limitedOutput);
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
        final ItemStack is = this.storageSlot.getInvStack(0);
        if (!is.isEmpty() && is.getItem() instanceof IStorageComponent) {
            final IStorageComponent sc = (IStorageComponent) is.getItem();
            if (sc.isStorageComponent(is)) {
                return sc.getBytes(is) * BYTE_MULTIPLIER;
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
        return this.outputSlot.getInsertable().wouldAccept(output);
    }

    /**
     * make sure you validate with canAddOutput prior to this.
     *
     * @param output to be added output
     */
    private void addOutput(final ItemStack output) {
        this.outputSlot.getInsertable().insert(output);
    }

    FixedItemInv getOutputSlot() {
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
    public FixedItemInv getInternalInventory() {
        return this.combinedInv;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
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

    @Override
    public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
        // Do not call super here since it would offer up the internal inventory

        to.offer(externalCombinedInv);
        to.offer(externalFluidInput);
        to.offer(meHandler);
    }

    private class CondenseItemHandler implements FixedItemInv {
        @Override
        public int getSlotCount() {
            return 1;
        }

        @Override
        public ItemStack getInvStack(int i) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            return true;
        }

        @Override
        public boolean setInvStack(int i, ItemStack itemStack, Simulation simulation) {
            if (simulation == Simulation.ACTION && !itemStack.isEmpty()) {
                CondenserTileEntity.this.addPower(itemStack.getCount());
            }
            return true;
        }

        @Override
        public int getChangeValue() {
            return 0;
        }

        @Nullable
        @Override
        public ListenerToken addListener(InvMarkDirtyListener invMarkDirtyListener,
                ListenerRemovalToken listenerRemovalToken) {
            return null;
        }
    }

    /**
     * A fluid handler that exposes a 1 bucket tank that can only be filled, and - when filled - will add power to this
     * condenser.
     */
    private class FluidHandler implements FluidInsertable {

        @Override
        public FluidVolume attemptInsertion(FluidVolume fluid, Simulation simulation) {
            if (simulation == Simulation.ACTION) {
                final IStorageChannel<IAEFluidStack> chan = Api.instance().storage()
                        .getStorageChannel(IFluidStorageChannel.class);
                CondenserTileEntity.this
                        .addPower((fluid.isEmpty() ? 0.0 : fluid.getAmount_F().asInexactDouble() * 1000.0)
                                / chan.transferFactor());
            }

            return FluidVolumeUtil.EMPTY;
        }

        @Nullable
        @Override
        public FluidAmount getMinimumAcceptedAmount() {
            return FluidAmount.of(1, 1000); // 1 millibucket
        }
    }

    /**
     * This is used to expose a fake ME subnetwork that is only composed of this condenser tile. The purpose of this is
     * to enable the condenser to override the {@link appeng.api.storage.IMEInventoryHandler#validForPass(int)} method
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
