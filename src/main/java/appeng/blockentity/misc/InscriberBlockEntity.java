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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import appeng.api.config.PowerUnits;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.orientation.BlockOrientation;
import appeng.api.orientation.RelativeSide;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkPowerBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.util.ConfigManager;
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
public class InscriberBlockEntity extends AENetworkPowerBlockEntity
        implements IGridTickable, IUpgradeableObject, IConfigurableObject {
    private static final int MAX_PROCESSING_STEPS = 200;

    private final IUpgradeInventory upgrades;
    private final ConfigManager configManager;
    private int processingTime = 0;
    // cycles from 0 - 16, at 8 it preforms the action, at 16 it re-enables the
    // normal routine.
    private boolean smash;
    private int finalStep;
    private long clientStart;

    // Internally visible inventories
    private final IAEItemFilter baseFilter = new BaseFilter();
    private final AppEngInternalInventory topItemHandler = new AppEngInternalInventory(this, 1, 64, baseFilter);
    private final AppEngInternalInventory bottomItemHandler = new AppEngInternalInventory(this, 1, 64, baseFilter);
    private final AppEngInternalInventory sideItemHandler = new AppEngInternalInventory(this, 2, 64, baseFilter);
    // Combined internally visible inventories
    private final InternalInventory inv = new CombinedInternalInventory(this.topItemHandler,
            this.bottomItemHandler, this.sideItemHandler);

    // "Hack" to see if active recipe changed.
    private final Map<InternalInventory, ItemStack> lastStacks = new IdentityHashMap<>(Map.of(
            topItemHandler, ItemStack.EMPTY, bottomItemHandler, ItemStack.EMPTY,
            sideItemHandler, ItemStack.EMPTY));

    // The externally visible inventories (with filters applied)
    private final InternalInventory topItemHandlerExtern;
    private final InternalInventory bottomItemHandlerExtern;
    private final InternalInventory sideItemHandlerExtern;
    // Combined externally visible inventories
    private final InternalInventory combinedItemHandlerExtern;

    private InscriberRecipe cachedTask = null;

    public InscriberBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);

        this.getMainNode()
                .setIdlePowerUsage(0)
                .addService(IGridTickable.class, this);
        this.setInternalMaxPower(1600);

        this.upgrades = UpgradeInventories.forMachine(AEBlocks.INSCRIBER, 4, this::saveChanges);
        this.configManager = new ConfigManager(this::onConfigChanged);
        this.configManager.registerSetting(Settings.INSCRIBER_SEPARATE_SIDES, YesNo.NO);
        this.configManager.registerSetting(Settings.AUTO_EXPORT, YesNo.NO);

        var automationFilter = new AutomationFilter();
        this.topItemHandlerExtern = new FilteredInternalInventory(this.topItemHandler, automationFilter);
        this.bottomItemHandlerExtern = new FilteredInternalInventory(this.bottomItemHandler, automationFilter);
        this.sideItemHandlerExtern = new FilteredInternalInventory(this.sideItemHandler, automationFilter);

        this.combinedItemHandlerExtern = new CombinedInternalInventory(topItemHandlerExtern, bottomItemHandlerExtern,
                sideItemHandlerExtern);

        this.setPowerSides(getGridConnectableSides(getOrientation()));
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
        this.configManager.writeToNBT(data);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.upgrades.readFromNBT(data, "upgrades");
        this.configManager.readFromNBT(data);
    }

    @Override
    protected boolean readFromStream(FriendlyByteBuf data) {
        var c = super.readFromStream(data);

        var oldSmash = isSmash();
        var newSmash = data.readBoolean();

        if (oldSmash != newSmash && newSmash) {
            setSmash(true);
        }

        for (int i = 0; i < this.inv.size(); i++) {
            this.inv.setItemDirect(i, data.readItem());
        }
        this.cachedTask = null;

        return c;
    }

    @Override
    protected void writeToStream(FriendlyByteBuf data) {
        super.writeToStream(data);

        data.writeBoolean(isSmash());
        for (int i = 0; i < this.inv.size(); i++) {
            data.writeItem(inv.getStackInSlot(i));
        }
    }

    @Override
    protected void saveVisualState(CompoundTag data) {
        super.saveVisualState(data);

        data.putBoolean("smash", isSmash());
    }

    @Override
    protected void loadVisualState(CompoundTag data) {
        super.loadVisualState(data);

        setSmash(data.getBoolean("smash"));
    }

    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return EnumSet.complementOf(EnumSet.of(orientation.getSide(RelativeSide.FRONT)));
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);

        this.setPowerSides(getGridConnectableSides(orientation));
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops, boolean remove) {
        super.addAdditionalDrops(level, pos, drops, remove);

        for (var upgrade : upgrades) {
            drops.add(upgrade);
        }
        if (remove) {
            upgrades.clear();
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.inv;
    }

    @Override
    public void onChangeInventory(InternalInventory inv, int slot) {
        if (slot == 0) {
            boolean isEmpty = inv.getStackInSlot(0).isEmpty();
            boolean wasEmpty = lastStacks.get(inv).isEmpty();
            lastStacks.put(inv, inv.getStackInSlot(0).copy());
            if (isEmpty == wasEmpty) {
                return; // Don't care if it's just a count change
            }

            // Reset recipe
            this.setProcessingTime(0);
            this.cachedTask = null;
        }

        // Update displayed stacks on the client
        if (!this.isSmash()) {
            this.markForUpdate();
        }

        getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
    }

    //
    // @Override
    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Inscriber, !hasAutoExportWork() && !this.hasCraftWork(), false);
    }

    private boolean hasAutoExportWork() {
        return !this.sideItemHandler.getStackInSlot(1).isEmpty()
                && configManager.getSetting(Settings.AUTO_EXPORT) == YesNo.YES;
    }

    private boolean hasCraftWork() {
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

            this.cachedTask = InscriberRecipes.findRecipe(level, input, plateA, plateB, true);
        }
        return this.cachedTask;
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.isSmash()) {
            this.finalStep++;
            if (this.finalStep == 8) {
                final InscriberRecipe out = this.getTask();
                if (out != null) {
                    final ItemStack outputCopy = out.getResultItem().copy();

                    if (this.sideItemHandler.insertItem(1, outputCopy, false).isEmpty()) {
                        this.setProcessingTime(0);
                        if (out.getProcessType() == InscriberProcessType.PRESS) {
                            this.topItemHandler.extractItem(0, 1, false);
                            this.bottomItemHandler.extractItem(0, 1, false);
                        }
                        this.sideItemHandler.extractItem(0, 1, false);
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
                IEnergyService eg = grid.getEnergyService();
                IEnergySource src = this;

                // Note: required ticks = 16 + ceil(MAX_PROCESSING_STEPS / speedFactor)
                final int speedFactor = switch (this.upgrades.getInstalledUpgrades(AEItems.SPEED_CARD)) {
                    default -> 2; // 116 ticks
                    case 1 -> 3; // 83 ticks
                    case 2 -> 5; // 56 ticks
                    case 3 -> 10; // 36 ticks
                    case 4 -> 50; // 20 ticks
                };
                final int powerConsumption = 10 * speedFactor;
                final double powerThreshold = powerConsumption - 0.01;
                double powerReq = this.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);

                if (powerReq <= powerThreshold) {
                    src = eg;
                    powerReq = eg.extractAEPower(powerConsumption, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                }

                if (powerReq > powerThreshold) {
                    src.extractAEPower(powerConsumption, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    this.setProcessingTime(this.getProcessingTime() + speedFactor);
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

        if (this.pushOutResult()) {
            return TickRateModulation.URGENT;
        }

        return this.hasCraftWork() ? TickRateModulation.URGENT
                : this.hasAutoExportWork() ? TickRateModulation.SLOWER : TickRateModulation.SLEEP;
    }

    /**
     * @return true if something was pushed, false otherwise
     */
    private boolean pushOutResult() {
        if (!this.hasAutoExportWork()) {
            return false;
        }

        var pushSides = EnumSet.allOf(Direction.class);
        if (isSeparateSides()) {
            pushSides.remove(this.getTop());
            pushSides.remove(this.getTop().getOpposite());
        }

        for (var dir : pushSides) {
            var target = InternalInventory.wrapExternal(level, getBlockPos().relative(dir), dir.getOpposite());

            if (target != null) {
                int startItems = this.sideItemHandler.getStackInSlot(1).getCount();
                this.sideItemHandler.insertItem(1, target.addItems(this.sideItemHandler.extractItem(1, 64, false)),
                        false);
                int endItems = this.sideItemHandler.getStackInSlot(1).getCount();

                if (startItems != endItems) {
                    return true;
                }
            }
        }

        return false;
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

    private boolean isSeparateSides() {
        return this.configManager.getSetting(Settings.INSCRIBER_SEPARATE_SIDES) == YesNo.YES;
    }

    @Override
    public InternalInventory getExposedInventoryForSide(Direction facing) {
        if (isSeparateSides()) {
            if (facing == this.getTop()) {
                return this.topItemHandlerExtern;
            } else if (facing == this.getTop().getOpposite()) {
                return this.bottomItemHandlerExtern;
            } else {
                return this.sideItemHandlerExtern;
            }
        } else {
            return this.combinedItemHandlerExtern;
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    private void onConfigChanged(IConfigManager manager, Setting<?> setting) {
        if (setting == Settings.AUTO_EXPORT) {
            getMainNode().ifPresent((grid, node) -> grid.getTickManager().wakeDevice(node));
        }

        if (setting == Settings.INSCRIBER_SEPARATE_SIDES) {
            // Send a block update since our exposed inventory changed...
            // In theory this shouldn't be necessary, but we do it just in case...
            markForUpdate();
        }

        saveChanges();
    }

    public long getClientStart() {
        return this.clientStart;
    }

    private void setClientStart(long clientStart) {
        this.clientStart = clientStart;
    }

    public boolean isSmash() {
        return this.smash;
    }

    public void setSmash(boolean smash) {
        if (smash && !this.smash) {
            setClientStart(System.currentTimeMillis());
        }
        this.smash = smash;
    }

    public int getMaxProcessingTime() {
        return this.MAX_PROCESSING_STEPS;
    }

    public int getProcessingTime() {
        return this.processingTime;
    }

    private void setProcessingTime(int processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Allow cranking from any side other than the front.
     */
    @org.jetbrains.annotations.Nullable
    public ICrankable getCrankable(Direction direction) {
        if (direction != getFront()) {
            return new Crankable();
        }
        return null;
    }

    public class BaseFilter implements IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            // output slot
            if (slot == 1) {
                // slots and automation prevent insertion into the output,
                // we need it here for the inscriber's own internal logic
                return true;
            }

            // always allow name press
            if (inv == topItemHandler || inv == bottomItemHandler) {
                if (AEItems.NAME_PRESS.isSameAs(stack)) {
                    return true;
                }
            }

            if (inv == sideItemHandler && (AEItems.NAME_PRESS.isSameAs(topItemHandler.getStackInSlot(0))
                    || AEItems.NAME_PRESS.isSameAs(bottomItemHandler.getStackInSlot(0)))) {
                // can always rename anything
                return true;
            }

            // only allow if is a proper recipe match
            ItemStack bot = bottomItemHandler.getStackInSlot(0);
            ItemStack middle = sideItemHandler.getStackInSlot(0);
            ItemStack top = topItemHandler.getStackInSlot(0);

            if (inv == bottomItemHandler)
                bot = stack;
            if (inv == sideItemHandler)
                middle = stack;
            if (inv == topItemHandler)
                top = stack;

            for (var recipe : InscriberRecipes.getRecipes(getLevel())) {
                if (!middle.isEmpty() && !recipe.getMiddleInput().test(middle)) {
                    continue;
                }

                if (bot.isEmpty() && top.isEmpty()) {
                    return true;
                } else if (bot.isEmpty()) {
                    if (recipe.getTopOptional().test(top) || recipe.getBottomOptional().test(top)) {
                        return true;
                    }
                } else if (top.isEmpty()) {
                    if (recipe.getBottomOptional().test(bot) || recipe.getTopOptional().test(bot)) {
                        return true;
                    }
                } else {
                    if ((recipe.getTopOptional().test(top) && recipe.getBottomOptional().test(bot))
                            || (recipe.getBottomOptional().test(top) && recipe.getTopOptional().test(bot))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public class AutomationFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            if (isSmash()) {
                return false;
            }

            return inv == InscriberBlockEntity.this.topItemHandler || inv == InscriberBlockEntity.this.bottomItemHandler
                    || slot == 1;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            if (slot == 1) {
                return false; // No inserting into the output slot
            }
            return !isSmash();
        }
    }

    class Crankable implements ICrankable {
        @Override
        public boolean canTurn() {
            return getInternalCurrentPower() < getInternalMaxPower();
        }

        @Override
        public void applyTurn() {
            injectExternalPower(PowerUnits.AE, CrankBlockEntity.POWER_PER_CRANK_TURN, Actionable.MODULATE);
        }
    }
}
