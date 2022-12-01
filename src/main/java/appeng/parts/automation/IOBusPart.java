/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.automation;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.helpers.IConfigInvHost;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.util.ConfigInventory;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;

public abstract class IOBusPart extends UpgradeablePart implements IGridTickable, IConfigInvHost {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/import_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/import_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/import_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/import_bus_has_channel"));

    private final ConfigInventory config;
    // Filter derived from the config
    @Nullable
    private IPartitionList filter;
    private final TickRates tickRates;
    protected final IActionSource source;
    private boolean lastRedstone = false;
    /**
     * Indicates that an I/O bus in redstone pulse mode has observed a low to high redstone transition and is waiting to
     * act on this during its next tick.
     */
    private boolean pendingPulse = false;

    public IOBusPart(TickRates tickRates, IPartItem<?> partItem) {
        super(partItem);
        this.tickRates = tickRates;
        this.source = new MachineSource(this);
        this.config = ConfigInventory.configTypes(StackWorldBehaviors.hasImportStrategyFilter(), 9, this::updateState);
        getMainNode().addService(IGridTickable.class, this);

        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public RedstoneMode getRSMode() {
        return this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    private boolean isInPulseMode() {
        return getRSMode() == RedstoneMode.SIGNAL_PULSE;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5;
    }

    /**
     * All export and import bus parts have a configuration ui.
     */
    protected abstract MenuType<?> getMenuType();

    @Override
    public void upgradesChanged() {
        this.updateState();
    }

    @Override
    public void readFromNBT(CompoundTag extra) {
        super.readFromNBT(extra);
        config.readFromChildTag(extra, "config");
        // Ensure the filter is rebuilt
        filter = null;
        pendingPulse = isInPulseMode() && extra.getBoolean("pendingPulse");
    }

    @Override
    public void writeToNBT(CompoundTag extra) {
        super.writeToNBT(extra);
        config.writeToChildTag(extra, "config");
        if (isInPulseMode() && pendingPulse) {
            extra.putBoolean("pendingPulse", true);
        }
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    protected final IPartitionList getFilter() {
        if (filter == null) {
            var filterBuilder = IPartitionList.builder();
            filterBuilder.addAll(getConfig().keySet());
            if (isUpgradedWith(AEItems.FUZZY_CARD)) {
                filterBuilder.fuzzyMode(this.getConfigManager().getSetting(Settings.FUZZY_MODE));
            }
            filter = filterBuilder.build();
        }
        return filter;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (isInPulseMode()) {
            var hostIsPowered = this.getHost().hasRedstone();
            if (this.lastRedstone != hostIsPowered) {
                this.lastRedstone = hostIsPowered;
                if (this.lastRedstone && !this.pendingPulse) {
                    // Perform the action based on the pulse on the next tick
                    this.pendingPulse = true;
                    getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
                }
            }
        } else {
            // This handles waking up the bus if the adjacent redstone has changed
            updateRedstoneState();
        }
    }

    protected int availableSlots() {
        return Math.min(1 + getInstalledUpgrades(AEItems.CAPACITY_CARD) * 4, this.getConfig().size());
    }

    protected int getOperationsPerTick() {
        return switch (getInstalledUpgrades(AEItems.SPEED_CARD)) {
            default -> 1;
            case 1 -> 8;
            case 2 -> 32;
            case 3 -> 64;
            case 4 -> 96;
        };
    }

    @Override
    public final TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        // Sometimes between being woken up and actually doing work, the config/redstone mode may have changed
        // put us back to sleep if that was the case
        if (isSleeping()) {
            return TickRateModulation.SLEEP;
        }

        if (!canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        // Reset a potential redstone pulse trigger
        this.pendingPulse = false;

        var hasDoneWork = this.doBusWork(node.getGrid());

        // We may be back to sleep (i.e. in pulse mode)
        if (isSleeping()) {
            return TickRateModulation.SLEEP;
        } else {
            return hasDoneWork ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
        }
    }

    /**
     * Checks if the bus can actually do something.
     * <p>
     * Currently this tests if the chunk for the target is actually loaded, and if the main node has it's channel and
     * power requirements fulfilled.
     *
     * @return true, if the the bus should do its work.
     */
    protected final boolean canDoBusWork() {
        if (!getMainNode().isActive()) {
            return false;
        }

        var self = this.getHost().getBlockEntity();
        var targetPos = self.getBlockPos().relative(getSide());

        return Platform.areBlockEntitiesTicking(self.getLevel(), targetPos);
    }

    private void updateState() {
        filter = null; // rebuild the filter

        updateRedstoneState();
    }

    @Override
    protected void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        super.onSettingChanged(manager, setting);

        updateRedstoneState();

        // Ensure we have an up-to-date last redstone state when pulse mode is activated to
        // correctly detect subsequent pulses
        if (isInPulseMode()) {
            this.lastRedstone = getHost().hasRedstone();
        }
    }

    private void updateRedstoneState() {
        // Clear the pending pulse flag if the upgrade is removed or the config is toggled off
        if (!this.isInPulseMode()) {
            this.pendingPulse = false;
        }

        getMainNode().ifPresent((grid, node) -> {
            if (!this.isSleeping()) {
                grid.getTickManager().wakeDevice(node);
            } else {
                grid.getTickManager().sleepDevice(node);
            }
        });
    }

    @Override
    public final boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!isClientSide()) {
            MenuOpener.open(getMenuType(), player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public final TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(tickRates.getMin(), tickRates.getMax(), isSleeping(), true);
    }

    @Override
    protected boolean isSleeping() {
        if (isInPulseMode() && this.pendingPulse) {
            return false;
        } else {
            return super.isSleeping();
        }
    }

    protected abstract boolean doBusWork(IGrid grid);

    @Override
    public void addToWorld() {
        super.addToWorld();

        // To correctly detect pulses (changes from low to high), we need to know the current state when we
        // are added to the world.
        this.lastRedstone = this.getHost().hasRedstone();
        // We may have observed a redstone pulse before, but were unable to act on it due to being unloaded before
        // we ticked again. Ensure that we do act on this pulse as soon as possible.
        if (pendingPulse) {
            getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }
}
