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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.helpers.IConfigInvHost;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
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

    public IOBusPart(TickRates tickRates, ItemStack is) {
        super(is);
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
    public void readFromNBT(final CompoundTag extra) {
        super.readFromNBT(extra);
        config.readFromChildTag(extra, "config");
        // Ensure the filter is rebuilt
        filter = null;
    }

    @Override
    public void writeToNBT(final CompoundTag extra) {
        super.writeToNBT(extra);
        config.writeToChildTag(extra, "config");
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    protected final IPartitionList getFilter() {
        if (filter == null) {
            var filterBuilder = IPartitionList.builder();
            filterBuilder.addAll(getConfig().keySet());
            if (getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                filterBuilder.fuzzyMode(this.getConfigManager().getSetting(Settings.FUZZY_MODE));
            }
            filter = filterBuilder.build();
        }
        return filter;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        this.updateState();
        if (this.lastRedstone != this.getHost().hasRedstone(this.getSide())) {
            this.lastRedstone = !this.lastRedstone;
            if (this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE) {
                getMainNode().ifPresent(this::doBusWork);
            }
        }
    }

    protected int availableSlots() {
        return Math.min(1 + getInstalledUpgrades(Upgrades.CAPACITY) * 4, this.getConfig().size());
    }

    protected int getOperationsPerTick() {
        return switch (getInstalledUpgrades(Upgrades.SPEED)) {
            default -> 1;
            case 1 -> 8;
            case 2 -> 32;
            case 3 -> 64;
            case 4 -> 96;
        };
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.doBusWork(node.getGrid());
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

        getMainNode().ifPresent((grid, node) -> {
            if (!this.isSleeping()) {
                grid.getTickManager().wakeDevice(node);
            } else {
                grid.getTickManager().sleepDevice(node);
            }
        });
    }

    @Override
    public final boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isClientSide()) {
            MenuOpener.open(getMenuType(), player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public final TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(tickRates.getMin(), tickRates.getMax(), isSleeping(), false);
    }

    protected abstract TickRateModulation doBusWork(IGrid grid);

}
