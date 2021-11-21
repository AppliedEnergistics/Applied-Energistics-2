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

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeInventory;
import appeng.api.implementations.IUpgradeableObject;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.data.AEItemKey;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.CombinedInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class InscriberBlockEntity extends AENetworkPowerBlockEntity implements IGridTickable, IUpgradeableObject {
    private final int maxProcessingTime = 100;

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

    // The externally visible inventories (with filters applied)
    private final InternalInventory topItemHandlerExtern;
    private final InternalInventory bottomItemHandlerExtern;
    private final InternalInventory sideItemHandlerExtern;

    private InscriberRecipe cachedTask = null;

    private final InternalInventory inv = new CombinedInternalInventory(this.topItemHandler,
            this.bottomItemHandler, this.sideItemHandler);

    public InscriberBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);

        this.getMainNode()
                .setExposedOnSides(EnumSet.noneOf(Direction.class))
                .setIdlePowerUsage(0)
                .addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600);

        this.upgrades = new DefinitionUpgradeInventory(AEBlocks.INSCRIBER, this, 3);

        this.sideItemHandler.setMaxStackSize(1, 64);

        var filter = new ItemHandlerFilter();
        this.topItemHandlerExtern = new FilteredInternalInventory(this.topItemHandler, filter);
        this.bottomItemHandlerExtern = new FilteredInternalInventory(this.bottomItemHandler, filter);
        this.sideItemHandlerExtern = new FilteredInternalInventory(this.sideItemHandler, filter);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
    }

    @Override
    public void loadTag(final CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
    }

    @Override
    protected boolean readFromStream(final FriendlyByteBuf data) {
        final boolean c = super.readFromStream(data);
        final int slot = data.readByte();

        final boolean oldSmash = this.isSmash();
        final boolean newSmash = (slot & 64) == 64;

        if (oldSmash != newSmash && newSmash) {
            this.setSmash(true);
            this.setClientStart(System.currentTimeMillis());
        }

        for (int num = 0; num < this.inv.size(); num++) {
            if ((slot & 1 << num) > 0) {
                this.inv.setItemDirect(num, AEItemKey.fromPacket(data).toStack());
            } else {
                this.inv.setItemDirect(num, ItemStack.EMPTY);
            }
        }
        this.cachedTask = null;

        return c;
    }

    @Override
    protected void writeToStream(final FriendlyByteBuf data) {
        super.writeToStream(data);
        int slot = this.isSmash() ? 64 : 0;

        for (int num = 0; num < this.inv.size(); num++) {
            if (!this.inv.getStackInSlot(num).isEmpty()) {
                slot |= 1 << num;
            }
        }

        data.writeByte(slot);
        for (int num = 0; num < this.inv.size(); num++) {
            if ((slot & 1 << num) > 0) {
                var st = AEItemKey.of(this.inv.getStackInSlot(num));
                st.writeToPacket(data);
            }
        }
    }

    @Override
    public void onReady() {
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        super.onReady();
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getMainNode().setExposedOnSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        this.setPowerSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
    }

    @Override
    public void getDrops(final Level level, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(level, pos, drops);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final InternalInventory inv, final int slot,
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
                    final ItemStack outputCopy = out.getResultItem().copy();

                    if (this.sideItemHandler.insertItem(1, outputCopy, false).isEmpty()) {
                        this.setProcessingTime(0);
                        if (out.getProcessType() == InscriberProcessType.PRESS) {
                            this.topItemHandler.setItemDirect(0, ItemStack.EMPTY);
                            this.bottomItemHandler.setItemDirect(0, ItemStack.EMPTY);
                        }
                        this.sideItemHandler.setItemDirect(0, ItemStack.EMPTY);
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
                    final ItemStack outputCopy = out.getResultItem().copy();
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
    public InternalInventory getExposedInventoryForSide(@Nonnull Direction facing) {
        if (facing == this.getUp()) {
            return this.topItemHandlerExtern;
        } else if (facing == this.getUp().getOpposite()) {
            return this.bottomItemHandlerExtern;
        } else {
            return this.sideItemHandlerExtern;
        }
    }

    @Nonnull
    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
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
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            if (InscriberBlockEntity.this.isSmash()) {
                return false;
            }

            return inv == InscriberBlockEntity.this.topItemHandler || inv == InscriberBlockEntity.this.bottomItemHandler
                    || slot == 1;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            // output slot
            if (slot == 1) {
                return false;
            }

            if (InscriberBlockEntity.this.isSmash()) {
                return false;
            }

            if (inv == InscriberBlockEntity.this.topItemHandler || inv == InscriberBlockEntity.this.bottomItemHandler) {
                if (AEItems.NAME_PRESS.isSameAs(stack)) {
                    return true;
                }
                return InscriberRecipes.isValidOptionalIngredient(getLevel(), stack);
            }
            return true;
        }
    }
}
