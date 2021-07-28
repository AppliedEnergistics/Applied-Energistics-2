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
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.features.InscriberProcessType;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.tile.grid.AENetworkPowerTileEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import appeng.util.item.AEItemStack;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class InscriberTileEntity extends AENetworkPowerTileEntity
        implements IGridTickable, IUpgradeableHost, IConfigManagerHost {
    private final int maxProcessingTime = 100;

    private final IConfigManager settings;
    private final UpgradeInventory upgrades;
    private int processingTime = 0;
    // cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the
    // normal routine.
    private boolean smash;
    private int finalStep;
    private long clientStart;
    private final AppEngInternalInventory topItemHandler = new AppEngInternalInventory(this, 1, 1);
    private final AppEngInternalInventory bottomItemHandler = new AppEngInternalInventory(this, 1, 1);
    private final AppEngInternalInventory sideItemHandler = new AppEngInternalInventory(this, 2, 1);

    private final IItemHandler topItemHandlerExtern;
    private final IItemHandler bottomItemHandlerExtern;
    private final IItemHandler sideItemHandlerExtern;

    private InscriberRecipe cachedTask = null;

    private final IItemHandlerModifiable inv = new WrapperChainedItemHandler(this.topItemHandler,
            this.bottomItemHandler, this.sideItemHandler);

    public InscriberTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);

        this.getMainNode()
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setIdlePowerUsage(0)
                .addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600);
        this.settings = new ConfigManager(this);

        this.upgrades = new DefinitionUpgradeInventory(AEBlocks.INSCRIBER, this, this.getUpgradeSlots());

        this.sideItemHandler.setMaxStackSize(1, 64);

        final IAEItemFilter filter = new ItemHandlerFilter();
        this.topItemHandlerExtern = new WrapperFilteredItemHandler(this.topItemHandler, filter);
        this.bottomItemHandlerExtern = new WrapperFilteredItemHandler(this.bottomItemHandler, filter);
        this.sideItemHandlerExtern = new WrapperFilteredItemHandler(this.sideItemHandler, filter);
    }

    private int getUpgradeSlots() {
        return 3;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        this.upgrades.writeToNBT(data, "upgrades");
        this.settings.writeToNBT(data);
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.settings.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int slot = data.readByte();

        final boolean oldSmash = this.isSmash();
        final boolean newSmash = (slot & 64) == 64;

        if (oldSmash != newSmash && newSmash) {
            this.setSmash(true);
            this.setClientStart(System.currentTimeMillis());
        }

        for (int num = 0; num < this.inv.getSlots(); num++) {
            if ((slot & 1 << num) > 0) {
                this.inv.setStackInSlot(num, AEItemStack.fromPacket(data).createItemStack());
            } else {
                this.inv.setStackInSlot(num, ItemStack.EMPTY);
            }
        }
        this.cachedTask = null;

        return c;
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) throws IOException {
        super.writeToStream(data);
        int slot = this.isSmash() ? 64 : 0;

        for (int num = 0; num < this.inv.getSlots(); num++) {
            if (!this.inv.getStackInSlot(num).isEmpty()) {
                slot |= 1 << num;
            }
        }

        data.writeByte(slot);
        for (int num = 0; num < this.inv.getSlots(); num++) {
            if ((slot & 1 << num) > 0) {
                final AEItemStack st = AEItemStack.fromItemStack(this.inv.getStackInSlot(num));
                st.writeToPacket(data);
            }
        }
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        this.setPowerSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
    }

    @Override
    public void getDrops(final Level w, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(w, pos, drops);

        for (int h = 0; h < this.upgrades.getSlots(); h++) {
            final ItemStack is = this.upgrades.getStackInSlot(h);
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        if (slot == 0) {
            this.setProcessingTime(0);
        }

        if (!this.isSmash()) {
            this.markForUpdate();
        }

        this.cachedTask = null;
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    //
    // @Override
    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.Inscriber.getMin(), TickRates.Inscriber.getMax(), !this.hasWork(), false);
    }

    private boolean hasWork() {
        if (this.getTask() != null) {
            return true;
        }

        this.setProcessingTime(0);
        return this.isSmash();
    }

    @Nullable
    public InscriberRecipe getTask() {
        if (this.cachedTask == null && level != null) {
            ItemStack input = this.sideItemHandler.getStackInSlot(0);
            ItemStack plateA = this.topItemHandler.getStackInSlot(0);
            ItemStack plateB = this.bottomItemHandler.getStackInSlot(0);
            if (input.isEmpty()) {
                return null; // No input to handle
            }

            // If the player somehow managed to insert more than one item, we bail here
            if (input.getCount() > 1 || plateA.getCount() > 1 || plateB.getCount() > 1) {
                return null;
            }

            this.cachedTask = InscriberRecipes.findRecipe(level, input, plateA, plateB, true);
        }
        return this.cachedTask;
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.isSmash()) {
            this.finalStep++;
            if (this.finalStep == 8) {
                final InscriberRecipe out = this.getTask();
                if (out != null) {
                    final ItemStack outputCopy = out.getOutput().copy();

                    if (this.sideItemHandler.insertItem(1, outputCopy, false).isEmpty()) {
                        this.setProcessingTime(0);
                        if (out.getProcessType() == InscriberProcessType.PRESS) {
                            this.topItemHandler.setStackInSlot(0, ItemStack.EMPTY);
                            this.bottomItemHandler.setStackInSlot(0, ItemStack.EMPTY);
                        }
                        this.sideItemHandler.setStackInSlot(0, ItemStack.EMPTY);
                    }
                }
                this.saveChanges();
            } else if (this.finalStep == 16) {
                this.finalStep = 0;
                this.setSmash(false);
                this.markForUpdate();
            }
        } else {
            getMainNode().ifPresent(grid -> {
                final IEnergyService eg = grid.getEnergyService();
                IEnergySource src = this;

                // Base 1, increase by 1 for each card
                final int speedFactor = 1 + this.upgrades.getInstalledUpgrades(Upgrades.SPEED);
                final int powerConsumption = 10 * speedFactor;
                final double powerThreshold = powerConsumption - 0.01;
                double powerReq = this.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);

                if (powerReq <= powerThreshold) {
                    src = eg;
                    powerReq = eg.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                }

                if (powerReq > powerThreshold) {
                    src.extractAEPower(powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG);

                    if (this.getProcessingTime() == 0) {
                        this.setProcessingTime(this.getProcessingTime() + speedFactor);
                    } else {
                        this.setProcessingTime(this.getProcessingTime() + ticksSinceLastCall * speedFactor);
                    }
                }
            });

            if (this.getProcessingTime() > this.getMaxProcessingTime()) {
                this.setProcessingTime(this.getMaxProcessingTime());
                final InscriberRecipe out = this.getTask();
                if (out != null) {
                    final ItemStack outputCopy = out.getOutput().copy();
                    if (this.sideItemHandler.insertItem(1, outputCopy, true).isEmpty()) {
                        this.setSmash(true);
                        this.finalStep = 0;
                        this.markForUpdate();
                    }
                }
            }
        }

        return this.hasWork() ? TickRateModulation.URGENT : TickRateModulation.SLEEP;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.settings;
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("inv")) {
            return this.getInternalInventory();
        }

        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        return null;
    }

    @Override
    protected IItemHandler getItemHandlerForSide(@Nonnull Direction facing) {
        if (facing == this.getUp()) {
            return this.topItemHandlerExtern;
        } else if (facing == this.getUp().getOpposite()) {
            return this.bottomItemHandlerExtern;
        } else {
            return this.sideItemHandlerExtern;
        }
    }

    @Override
    public int getInstalledUpgrades(final Upgrades u) {
        return this.upgrades.getInstalledUpgrades(u);
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
    }

    public long getClientStart() {
        return this.clientStart;
    }

    private void setClientStart(final long clientStart) {
        this.clientStart = clientStart;
    }

    public boolean isSmash() {
        return this.smash;
    }

    public void setSmash(final boolean smash) {
        this.smash = smash;
    }

    public int getMaxProcessingTime() {
        return this.maxProcessingTime;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    private void setProcessingTime(final int processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * This is an item handler that exposes the inscribers inventory while providing simulation capabilities that do not
     * reset the progress if there's already an item in a slot. Previously, the progress of the inscriber was reset when
     * another mod attempted insertion of items when there were already items in the slot.
     */
    private class ItemHandlerFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            if (InscriberTileEntity.this.isSmash()) {
                return false;
            }

            return inv == InscriberTileEntity.this.topItemHandler || inv == InscriberTileEntity.this.bottomItemHandler
                    || slot == 1;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            // output slot
            if (slot == 1) {
                return false;
            }

            if (InscriberTileEntity.this.isSmash()) {
                return false;
            }

            if (inv == InscriberTileEntity.this.topItemHandler || inv == InscriberTileEntity.this.bottomItemHandler) {
                if (AEItems.NAME_PRESS.isSameAs(stack)) {
                    return true;
                }
                return InscriberRecipes.isValidOptionalIngredient(getLevel(), stack);
            }
            return true;
        }
    }
}
