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

package appeng.parts.automation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.util.AECableType;
import appeng.core.settings.TickRates;
import appeng.helpers.IConfigInvHost;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.util.ConfigInventory;
import appeng.util.Platform;

public abstract class IOBusPart<T extends AEKey> extends UpgradeablePart implements IGridTickable, IConfigInvHost {

    private final ConfigInventory<T> config = ConfigInventory.configTypes(getChannel(), 9, this::updateState);

    private final TickRates tickRates;

    protected final IActionSource source;

    private boolean lastRedstone = false;

    public IOBusPart(TickRates tickRates, ItemStack is) {
        super(is);
        this.tickRates = tickRates;
        this.source = new MachineSource(this);
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

    protected abstract IStorageChannel<T> getChannel();

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
    }

    @Override
    public void writeToNBT(final CompoundTag extra) {
        super.writeToNBT(extra);
        config.writeToChildTag(extra, "config");
    }

    @Override
    public ConfigInventory<T> getConfig() {
        return config;
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        this.updateState();
        if (this.lastRedstone != this.getHost().hasRedstone(this.getSide())) {
            this.lastRedstone = !this.lastRedstone;
            if (this.lastRedstone && this.getRSMode() == RedstoneMode.SIGNAL_PULSE) {
                this.doBusWork();
            }
        }
    }

    protected int availableSlots() {
        return Math.min(1 + getInstalledUpgrades(Upgrades.CAPACITY) * 4, this.getConfig().size());
    }

    protected int calculateAmountPerTick() {
        var multiplier = switch (getInstalledUpgrades(Upgrades.SPEED)) {
            default -> 1;
            case 1 -> 8;
            case 2 -> 32;
            case 3 -> 64;
            case 4 -> 96;
        };
        return multiplier * getChannel().transferFactor();
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.doBusWork();
    }

    /**
     * Checks if the bus can actually do something.
     * <p>
     * Currently this tests if the chunk for the target is actually loaded, and if the main node has it's channel and
     * power requirements fulfilled.
     *
     * @return true, if the the bus should do its work.
     */
    protected boolean canDoBusWork() {
        if (!getMainNode().isActive()) {
            return false;
        }

        var self = this.getHost().getBlockEntity();
        var targetPos = self.getBlockPos().relative(getSide());

        return Platform.areBlockEntitiesTicking(self.getLevel(), targetPos);
    }

    private void updateState() {
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

    protected abstract TickRateModulation doBusWork();

}
