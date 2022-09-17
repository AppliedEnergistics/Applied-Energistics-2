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


import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
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
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.parts.automation.DefinitionUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.grid.AENetworkPowerTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.inv.WrapperChainedItemHandler;
import appeng.util.inv.WrapperFilteredItemHandler;
import appeng.util.inv.filter.IAEItemFilter;
import appeng.util.item.AEItemStack;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;


/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class TileInscriber extends AENetworkPowerTile implements IGridTickable, IUpgradeableHost, IConfigManagerHost {
    private final int maxProcessingTime = 100;

    private final IConfigManager settings;
    private final UpgradeInventory upgrades;
    private int processingTime = 0;
    // cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the normal routine.
    private boolean smash;
    private int finalStep;
    private long clientStart;
    private final AppEngInternalInventory topItemHandler = new AppEngInternalInventory(this, 1, 1);
    private final AppEngInternalInventory bottomItemHandler = new AppEngInternalInventory(this, 1, 1);
    private final AppEngInternalInventory sideItemHandler = new AppEngInternalInventory(this, 2, 1);

    private final IItemHandler topItemHandlerExtern;
    private final IItemHandler bottomItemHandlerExtern;
    private final IItemHandler sideItemHandlerExtern;

    private IInscriberRecipe cachedTask = null;

    private final IItemHandlerModifiable inv = new WrapperChainedItemHandler(this.topItemHandler, this.bottomItemHandler, this.sideItemHandler);

    public TileInscriber() {
        this.getProxy().setValidSides(EnumSet.noneOf(EnumFacing.class));
        this.setInternalMaxPower(1600);
        this.getProxy().setIdlePowerUsage(0);
        this.settings = new ConfigManager(this);

        final ITileDefinition inscriberDefinition = AEApi.instance().definitions().blocks().inscriber();
        this.upgrades = new DefinitionUpgradeInventory(inscriberDefinition, this, this.getUpgradeSlots());

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
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.COVERED;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        this.upgrades.writeToNBT(data, "upgrades");
        this.settings.writeToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.settings.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(final ByteBuf data) throws IOException {
        final boolean c = super.readFromStream(data);
        final int slot = data.readByte();

        final boolean oldSmash = this.isSmash();
        final boolean newSmash = (slot & 64) == 64;

        if (oldSmash != newSmash && newSmash) {
            this.setSmash(true);
            this.setClientStart(System.currentTimeMillis());
        }

        for (int num = 0; num < this.inv.getSlots(); num++) {
            if ((slot & (1 << num)) > 0) {
                this.inv.setStackInSlot(num, AEItemStack.fromPacket(data).createItemStack());
            } else {
                this.inv.setStackInSlot(num, ItemStack.EMPTY);
            }
        }
        this.cachedTask = null;

        return c;
    }

    @Override
    protected void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        int slot = this.isSmash() ? 64 : 0;

        for (int num = 0; num < this.inv.getSlots(); num++) {
            if (!this.inv.getStackInSlot(num).isEmpty()) {
                slot |= (1 << num);
            }
        }

        data.writeByte(slot);
        for (int num = 0; num < this.inv.getSlots(); num++) {
            if ((slot & (1 << num)) > 0) {
                final AEItemStack st = AEItemStack.fromItemStack(this.inv.getStackInSlot(num));
                st.writeToPacket(data);
            }
        }
    }

    @Override
    public void setOrientation(final EnumFacing inForward, final EnumFacing inUp) {
        super.setOrientation(inForward, inUp);
        this.getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
        this.setPowerSides(EnumSet.complementOf(EnumSet.of(this.getForward())));
    }

    @Override
    public void getDrops(final World w, final BlockPos pos, final List<ItemStack> drops) {
        super.getDrops(w, pos, drops);

        for (int h = 0; h < this.upgrades.getSlots(); h++) {
            final ItemStack is = this.upgrades.getStackInSlot(h);
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public boolean requiresTESR() {
        return true;
    }

    @Override
    public IItemHandler getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removed, final ItemStack added) {
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
    public IInscriberRecipe getTask() {
        if (this.cachedTask == null) {
            this.cachedTask = this.getTask(this.sideItemHandler.getStackInSlot(0), this.topItemHandler.getStackInSlot(0),
                    this.bottomItemHandler.getStackInSlot(0));
        }
        return this.cachedTask;
    }

    @Nullable
    private IInscriberRecipe getTask(final ItemStack input, final ItemStack plateA, final ItemStack plateB) {
        if (input.isEmpty() || input.getCount() > 1) {
            return null;
        }

        if (!plateA.isEmpty() && plateA.getCount() > 1) {
            return null;
        }

        if (!plateB.isEmpty() && plateB.getCount() > 1) {
            return null;
        }

        final IComparableDefinition namePress = AEApi.instance().definitions().materials().namePress();
        final boolean isNameA = namePress.isSameAs(plateA);
        final boolean isNameB = namePress.isSameAs(plateB);

        if ((isNameA && isNameB) || isNameA && plateB.isEmpty()) {
            return this.makeNamePressRecipe(input, plateA, plateB);
        } else if (plateA.isEmpty() && isNameB) {
            return this.makeNamePressRecipe(input, plateB, plateA);
        }

        for (final IInscriberRecipe recipe : AEApi.instance().registries().inscriber().getRecipes()) {

            final boolean matchA = (plateA.isEmpty() && !recipe.getTopOptional().isPresent()) || (Platform.itemComparisons()
                    .isSameItem(plateA,
                            recipe.getTopOptional().orElse(ItemStack.EMPTY))) && // and...
                    ((plateB.isEmpty() && !recipe.getBottomOptional().isPresent()) || (Platform.itemComparisons()
                            .isSameItem(plateB,
                                    recipe.getBottomOptional().orElse(ItemStack.EMPTY))));

            final boolean matchB = (plateB.isEmpty() && !recipe.getTopOptional().isPresent()) || (Platform.itemComparisons()
                    .isSameItem(plateB,
                            recipe.getTopOptional().orElse(ItemStack.EMPTY))) && // and...
                    ((plateA.isEmpty() && !recipe.getBottomOptional().isPresent()) || (Platform.itemComparisons()
                            .isSameItem(plateA,
                                    recipe.getBottomOptional().orElse(ItemStack.EMPTY))));

            if (matchA || matchB) {
                for (final ItemStack option : recipe.getInputs()) {
                    if (Platform.itemComparisons().isSameItem(input, option)) {
                        return recipe;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.isSmash()) {
            this.finalStep++;
            if (this.finalStep == 8) {
                final IInscriberRecipe out = this.getTask();
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
                final IInscriberRecipe out = this.getTask();
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
    protected IItemHandler getItemHandlerForSide(@Nonnull EnumFacing facing) {
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
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
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

    private IInscriberRecipe makeNamePressRecipe(ItemStack input, ItemStack plateA, ItemStack plateB) {
        String name = "";

        if (!plateA.isEmpty()) {
            final NBTTagCompound tag = Platform.openNbtData(plateA);
            name += tag.getString("InscribeName");
        }

        if (!plateB.isEmpty()) {
            final NBTTagCompound tag = Platform.openNbtData(plateB);
            name += " " + tag.getString("InscribeName");
        }

        final ItemStack startingItem = input.copy();
        final ItemStack renamedItem = input.copy();
        final NBTTagCompound tag = Platform.openNbtData(renamedItem);

        final NBTTagCompound display = tag.getCompoundTag("display");
        tag.setTag("display", display);

        if (name.length() > 0) {
            display.setString("Name", name);
        } else {
            display.removeTag("Name");
        }

        final List<ItemStack> inputs = Lists.newArrayList(startingItem);
        final InscriberProcessType type = InscriberProcessType.INSCRIBE;

        final IInscriberRecipeBuilder builder = AEApi.instance().registries().inscriber().builder();
        builder.withInputs(inputs).withOutput(renamedItem).withProcessType(type);

        if (!plateA.isEmpty()) {
            builder.withTopOptional(plateA);
        }

        if (!plateB.isEmpty()) {
            builder.withBottomOptional(plateB);
        }

        return builder.build();
    }

    /**
     * This is an item handler that exposes the inscribers inventory while providing simulation capabilities that do not
     * reset the progress if there's already an item in a slot. Previously, the progress of the inscriber was reset when
     * another mod attempted insertion of items when there were already items in the slot.
     */
    private class ItemHandlerFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(IItemHandler inv, int slot, int amount) {
            if (TileInscriber.this.isSmash()) {
                return false;
            }

            return inv == TileInscriber.this.topItemHandler || inv == TileInscriber.this.bottomItemHandler || slot == 1;
        }

        @Override
        public boolean allowInsert(IItemHandler inv, int slot, ItemStack stack) {
            // output slot
            if (slot == 1) {
                return false;
            }

            if (TileInscriber.this.isSmash()) {
                return false;
            }

            if (inv == TileInscriber.this.topItemHandler || inv == TileInscriber.this.bottomItemHandler) {
                if (AEApi.instance().definitions().materials().namePress().isSameAs(stack)) {
                    return true;
                }
                for (final ItemStack optionals : AEApi.instance().registries().inscriber().getOptionals()) {
                    if (Platform.itemComparisons().isSameItem(stack, optionals)) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }
    }
}
