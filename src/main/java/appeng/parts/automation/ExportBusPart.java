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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.UpgradeableContainer;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.helpers.MultiCraftingTracker;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ExportBusPart extends SharedItemBusPart implements ICraftingRequester {

    public static final Identifier MODEL_BASE = new Identifier(AppEng.MOD_ID, "part/export_bus_base");

    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/export_bus_off"));

    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/export_bus_on"));

    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new Identifier(AppEng.MOD_ID, "part/export_bus_has_channel"));

    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, 9);
    private final IActionSource mySrc;
    private long itemToSend = 1;
    private boolean didSomething = false;
    private int nextSlot = 0;

    public ExportBusPart(final ItemStack is) {
        super(is);

        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        this.getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        this.mySrc = new MachineSource(this);
    }

    @Override
    public void readFromNBT(final CompoundTag extra) {
        super.readFromNBT(extra);
        this.craftingTracker.readFromNBT(extra);
        this.nextSlot = extra.getInt("nextSlot");
    }

    @Override
    public void writeToNBT(final CompoundTag extra) {
        super.writeToNBT(extra);
        this.craftingTracker.writeToNBT(extra);
        extra.putInt("nextSlot", this.nextSlot);
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.getProxy().isActive() || !this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        this.itemToSend = this.calculateItemsToSend();
        this.didSomething = false;

        try {
            final InventoryAdaptor destination = this.getHandler();
            final IMEMonitor<IAEItemStack> inv = this.getProxy().getStorage()
                    .getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            final IEnergyGrid energy = this.getProxy().getEnergy();
            final ICraftingGrid cg = this.getProxy().getCrafting();
            final FuzzyMode fzMode = (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE);
            final SchedulingMode schedulingMode = (SchedulingMode) this.getConfigManager()
                    .getSetting(Settings.SCHEDULING_MODE);

            if (destination != null) {
                int x = 0;

                for (x = 0; x < this.availableSlots() && this.itemToSend > 0; x++) {
                    final int slotToExport = this.getStartingSlot(schedulingMode, x);

                    final IAEItemStack ais = this.getConfig().getAEStackInSlot(slotToExport);

                    if (ais == null || this.itemToSend <= 0 || this.craftOnly()) {
                        if (this.isCraftingEnabled()) {
                            this.didSomething = this.craftingTracker.handleCrafting(slotToExport, this.itemToSend, ais,
                                    destination, this.getTile().getWorld(), this.getProxy().getGrid(), cg, this.mySrc)
                                    || this.didSomething;
                        }
                        continue;
                    }

                    final long before = this.itemToSend;

                    if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                        for (final IAEItemStack o : ImmutableList.copyOf(inv.getStorageList().findFuzzy(ais, fzMode))) {
                            this.pushItemIntoTarget(destination, energy, inv, o);
                            if (this.itemToSend <= 0) {
                                break;
                            }
                        }
                    } else {
                        this.pushItemIntoTarget(destination, energy, inv, ais);
                    }

                    if (this.itemToSend == before && this.isCraftingEnabled()) {
                        this.didSomething = this.craftingTracker.handleCrafting(slotToExport, this.itemToSend, ais,
                                destination, this.getTile().getWorld(), this.getProxy().getGrid(), cg, this.mySrc)
                                || this.didSomething;
                    }
                }

                this.updateSchedulingMode(schedulingMode, x);
            } else {
                return TickRateModulation.SLEEP;
            }
        } catch (final GridAccessException e) {
            // :P
        }

        return this.didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5;
    }

    @Override
    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            ContainerOpener.openContainer(UpgradeableContainer.TYPE, player, ContainerLocator.forPart(this));
        }
        return true;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.ExportBus.getMin(), TickRates.ExportBus.getMax(), this.isSleeping(), false);
    }

    @Override
    public RedstoneMode getRSMode() {
        return (RedstoneMode) this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.doBusWork();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(final ICraftingLink link, final IAEItemStack items, final Actionable mode) {
        final InventoryAdaptor d = this.getHandler();

        try {
            if (d != null && this.getProxy().isActive()) {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                final double power = items.getStackSize();

                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    if (mode == Actionable.MODULATE) {
                        return AEItemStack.fromItemStack(d.addItems(items.createItemStack()));
                    }
                    return AEItemStack.fromItemStack(d.simulateAdd(items.createItemStack()));
                }
            }
        } catch (final GridAccessException e) {
            AELog.debug(e);
        }

        return items;
    }

    @Override
    public void jobStateChange(final ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
    }

    @Override
    protected boolean isSleeping() {
        return this.getHandler() == null || super.isSleeping();
    }

    private boolean craftOnly() {
        return this.getConfigManager().getSetting(Settings.CRAFT_ONLY) == YesNo.YES;
    }

    private boolean isCraftingEnabled() {
        return this.getInstalledUpgrades(Upgrades.CRAFTING) > 0;
    }

    private void pushItemIntoTarget(final InventoryAdaptor d, final IEnergyGrid energy,
            final IMEInventory<IAEItemStack> inv, IAEItemStack ais) {
        final ItemStack is = ais.createItemStack();
        is.setCount((int) this.itemToSend);

        final ItemStack o = d.simulateAdd(is);
        final long canFit = o.isEmpty() ? this.itemToSend : this.itemToSend - o.getCount();

        if (canFit > 0) {
            ais = ais.copy();
            ais.setStackSize(canFit);
            final IAEItemStack itemsToAdd = Platform.poweredExtraction(energy, inv, ais, this.mySrc);

            if (itemsToAdd != null) {
                this.itemToSend -= itemsToAdd.getStackSize();

                final ItemStack failed = d.addItems(itemsToAdd.createItemStack());
                if (!failed.isEmpty()) {
                    ais.setStackSize(failed.getCount());
                    inv.injectItems(ais, Actionable.MODULATE, this.mySrc);
                } else {
                    this.didSomething = true;
                }
            }
        }
    }

    private int getStartingSlot(final SchedulingMode schedulingMode, final int x) {
        if (schedulingMode == SchedulingMode.RANDOM) {
            return Platform.getRandom().nextInt(this.availableSlots());
        }

        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            return (this.nextSlot + x) % this.availableSlots();
        }

        return x;
    }

    private void updateSchedulingMode(final SchedulingMode schedulingMode, final int x) {
        if (schedulingMode == SchedulingMode.ROUNDROBIN) {
            this.nextSlot = (this.nextSlot + x) % this.availableSlots();
        }
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

}
