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

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.LimitedFixedItemInv;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.definitions.ITileDefinition;
import appeng.api.features.InscriberProcessType;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.core.Api;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.tile.grid.AENetworkPowerBlockEntity;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.item.AEItemStack;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class InscriberBlockEntity extends AENetworkPowerBlockEntity
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

    private final LimitedFixedItemInv topItemHandlerExtern;
    private final LimitedFixedItemInv bottomItemHandlerExtern;
    private final LimitedFixedItemInv sideItemHandlerExtern;

    private InscriberRecipe cachedTask = null;

    private final FixedItemInv inv = new WrapperChainedItemHandler(this.topItemHandler, this.bottomItemHandler,
            this.sideItemHandler);

    private final FixedItemInv externalInv;

    public InscriberBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);

        this.getProxy().setValidSides(EnumSet.noneOf(Direction.class));
        this.setInternalMaxPower(1600);
        this.getProxy().setIdlePowerUsage(0);
        this.settings = new ConfigManager(this);

        final ITileDefinition inscriberDefinition = Api.instance().definitions().blocks().inscriber();
        this.upgrades = new DefinitionUpgradeInventory(inscriberDefinition, this, this.getUpgradeSlots());

        this.sideItemHandler.setMaxStackSize(1, 64);

        this.topItemHandlerExtern = this.topItemHandler.createLimitedFixedInv();
        this.topItemHandlerExtern.getAllRule().filterInserts(this::canInsertIntoTopOrBottom)
                .filterExtracts(stack -> !isSmash());

        this.bottomItemHandlerExtern = this.bottomItemHandler.createLimitedFixedInv();
        this.bottomItemHandlerExtern.getAllRule().filterInserts(this::canInsertIntoTopOrBottom)
                .filterExtracts(stack -> !isSmash());

        this.sideItemHandlerExtern = this.sideItemHandler.createLimitedFixedInv();
        this.sideItemHandlerExtern.getRule(0).disallowExtraction().filterInserts(stack -> !isSmash());
        this.sideItemHandlerExtern.getRule(1).filterExtracts(stack -> !isSmash()).disallowInsertion();

        this.externalInv = new WrapperChainedItemHandler(this.topItemHandlerExtern, this.bottomItemHandlerExtern,
                this.sideItemHandlerExtern);
    }

    private boolean canInsertIntoTopOrBottom(ItemStack stack) {
        if (isSmash()) {
            return false;
        }
        if (Api.instance().definitions().materials().namePress().isSameAs(stack)) {
            return true;
        }
        return InscriberRecipes.isValidOptionalIngredient(getWorld(), stack);
    }

    private int getUpgradeSlots() {
        return 3;
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.COVERED;
    }

    @Override
    public CompoundNBT write(final CompoundNBT data) {
        super.write(data);
        this.upgrades.writeToNBT(data, "upgrades");
        this.settings.writeToNBT(data);
        return data;
    }

    @Override
    public void read(BlockState state, final CompoundNBT data) {
        super.read(state, data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.settings.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(final PacketBuffer data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int slot = data.readByte();

        final boolean oldSmash = this.isSmash();
        final boolean newSmash = (slot & 64) == 64;

        if (oldSmash != newSmash && newSmash) {
            this.setSmash(true);
            this.setClientStart(System.currentTimeMillis());
        }

        for (int num = 0; num < this.inv.getSlotCount(); num++) {
            if ((slot & (1 << num)) > 0) {
                this.inv.forceSetInvStack(num, AEItemStack.fromPacket(data).createItemStack());
            } else {
                this.inv.forceSetInvStack(num, ItemStack.EMPTY);
            }
        }
        this.cachedTask = null;

        return c;
    }

    @Override
    protected void writeToStream(final PacketBuffer data) throws IOException {
        super.writeToStream(data);
        int slot = this.isSmash() ? 64 : 0;

        for (int num = 0; num < this.inv.getSlotCount(); num++) {
            if (!this.inv.getInvStack(num).isEmpty()) {
                slot |= (1 << num);
            }
        }

        data.writeByte(slot);
        for (int num = 0; num < this.inv.getSlotCount(); num++) {
            if ((slot & (1 << num)) > 0) {
                final AEItemStack st = AEItemStack.fromItemStack(this.inv.getInvStack(num));
                st.writeToPacket(data);
            }
        }
    }

    @Override
    public void setOrientation(final Direction inForward, final Direction inUp) {
        super.setOrientation(inForward, inUp);
        this.getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        this.setPowerSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(w, pos, drops);

        for (int h = 0; h < this.upgrades.getSlotCount(); h++) {
            final ItemStack is = this.upgrades.getInvStack(h);
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public FixedItemInv getInternalInventory() {
        return this.inv;
    }

    @NotNull
    @Override
    public FixedItemInv getExternalInventory() {
        return this.externalInv;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removed, final ItemStack added) {
        try {
            if (slot == 0) {
                this.setProcessingTime(0);
            }

            if (!this.isSmash()) {
                this.markForUpdate();
            }

            this.cachedTask = null;
            this.getProxy().getTick().wakeDevice(this.getProxy().getNode());
        } catch (final GridAccessException e) {
            // :P
        }
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
        if (this.cachedTask == null && world != null) {
            ItemStack input = this.sideItemHandler.getInvStack(0);
            ItemStack plateA = this.topItemHandler.getInvStack(0);
            ItemStack plateB = this.bottomItemHandler.getInvStack(0);
            if (input.isEmpty()) {
                return null; // No input to handle
            }

            // If the player somehow managed to insert more than one item, we bail here
            if (input.getCount() > 1 || plateA.getCount() > 1 || plateB.getCount() > 1) {
                return null;
            }

            this.cachedTask = InscriberRecipes.findRecipe(world, input, plateA, plateB, true);
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
                    final ItemStack outputCopy = out.getRecipeOutput().copy();

                    if (this.sideItemHandler.getSlot(1).insert(outputCopy).isEmpty()) {
                        this.setProcessingTime(0);
                        if (out.getProcessType() == InscriberProcessType.PRESS) {
                            this.topItemHandler.setInvStack(0, ItemStack.EMPTY, Simulation.ACTION);
                            this.bottomItemHandler.setInvStack(0, ItemStack.EMPTY, Simulation.ACTION);
                        }
                        this.sideItemHandler.setInvStack(0, ItemStack.EMPTY, Simulation.ACTION);
                    }
                }
                this.saveChanges();
            } else if (this.finalStep == 16) {
                this.finalStep = 0;
                this.setSmash(false);
                this.markForUpdate();
            }
        } else {
            try {
                final IEnergyGrid eg = this.getProxy().getEnergy();
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
            } catch (final GridAccessException e) {
                // :P
            }

            if (this.getProcessingTime() > this.getMaxProcessingTime()) {
                this.setProcessingTime(this.getMaxProcessingTime());
                final InscriberRecipe out = this.getTask();
                if (out != null) {
                    final ItemStack outputCopy = out.getRecipeOutput().copy();
                    if (this.sideItemHandler.getSlot(1).wouldAccept(outputCopy)) {
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
    public FixedItemInv getInventoryByName(final String name) {
        if (name.equals("inv")) {
            return this.getInternalInventory();
        }

        if (name.equals("upgrades")) {
            return this.upgrades;
        }

        return null;
    }

    @Override
    protected FixedItemInv getItemHandlerForSide(@Nonnull Direction facing) {
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

}
