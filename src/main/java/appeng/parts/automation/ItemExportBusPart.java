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

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.ItemTransfer;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartModel;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.IOBusMenu;
import appeng.parts.PartModel;
import appeng.util.Platform;

public class ItemExportBusPart extends ExportBusPart<Storage<ItemVariant>> {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/item_export_bus_base");

    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_export_bus_off"));

    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_export_bus_on"));

    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_export_bus_has_channel"));

    private long itemToSend = 1;
    private boolean didSomething = false;

    public ItemExportBusPart(final ItemStack is) {
        super(TickRates.ItemExportBus, is, ItemStorage.SIDED, AEItemKey.filter());
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.ITEM_EXPORT_TYPE;
    }

    protected ItemTransfer getHandler() {
        final BlockEntity self = this.getHost().getBlockEntity();
        final BlockEntity target = Platform.getTickingBlockEntity(getLevel(),
                self.getBlockPos().relative(this.getSide()));

        return InternalInventory.wrapExternal(target, this.getSide().getOpposite());
    }

    @Override
    protected TickRateModulation doBusWork(IGrid grid) {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        this.itemToSend = this.getOperationsPerTick();
        this.didSomething = false;

        var destination = this.getHandler();
        var inv = grid.getStorageService().getInventory();
        var energy = grid.getEnergyService();
        var cg = grid.getCraftingService();
        var fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
        var schedulingMode = this.getConfigManager().getSetting(Settings.SCHEDULING_MODE);

        if (destination != null) {
            int x = 0;

            for (x = 0; x < this.availableSlots() && this.itemToSend > 0; x++) {
                final int slotToExport = this.getStartingSlot(schedulingMode, x);

                if (!(this.getConfig().getKey(slotToExport) instanceof AEItemKey what)) {
                    continue;
                }

                if (this.itemToSend <= 0 || this.craftOnly()) {
                    if (this.isCraftingEnabled()) {
                        requestCrafting(cg, slotToExport, what);
                    }
                    continue;
                }

                final long before = this.itemToSend;

                if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                    for (var o : ImmutableList
                            .copyOf(inv.getCachedAvailableStacks().findFuzzy(what, fzMode))) {
                        if (o.getKey() instanceof AEItemKey itemKey) {
                            this.pushItemIntoTarget(destination, energy, inv, itemKey);
                        }
                        if (this.itemToSend <= 0) {
                            break;
                        }
                    }
                } else {
                    this.pushItemIntoTarget(destination, energy, inv, what);
                }

                if (this.itemToSend == before && this.isCraftingEnabled()) {
                    requestCrafting(cg, slotToExport, what);
                }
            }

            this.updateSchedulingMode(schedulingMode, x);
        } else {
            return TickRateModulation.SLEEP;
        }

        return this.didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    private void requestCrafting(ICraftingService cg, int slotToExport, AEItemKey what) {
        // don't bother crafting / checking or result, if target cannot accept at least 1 of requested item
        if (!getHandler().simulateAdd(what.toStack()).isEmpty()) {
            return;
        }
        didSomething |= super.requestCrafting(cg, slotToExport, what, itemToSend);
    }

    @Override
    public long insertCraftedItems(final ICraftingLink link, final AEKey what, long amount, final Actionable mode) {
        if (!(what instanceof AEItemKey itemKey)) {
            return 0;
        }

        var d = this.getHandler();

        var grid = getMainNode().getGrid();
        if (grid != null) {
            if (d != null && this.getMainNode().isActive()) {
                var toInsert = itemKey.toStack((int) amount);
                var energy = grid.getEnergyService();
                var power = toInsert.getCount();

                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    ItemStack overflow;
                    if (mode == Actionable.MODULATE) {
                        overflow = d.addItems(toInsert);
                    }
                    overflow = d.simulateAdd(toInsert);
                    return toInsert.getCount() - overflow.getCount();
                }
            }
        }

        return 0;
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

    private void pushItemIntoTarget(ItemTransfer d, IEnergyService energy, MEStorage inv, AEItemKey ais) {
        var is = ais.toStack((int) this.itemToSend);

        final ItemStack o = d.simulateAdd(is);
        final long canFit = o.isEmpty() ? this.itemToSend : this.itemToSend - o.getCount();

        if (canFit > 0) {
            var itemsToAdd = StorageHelper.poweredExtraction(energy, inv, ais, canFit, this.source);

            if (itemsToAdd > 0) {
                this.itemToSend -= itemsToAdd;

                var inserted = d.addItems(ais.toStack((int) itemsToAdd));
                if (inserted.getCount() < itemsToAdd) {
                    inv.insert(ais, itemsToAdd - inserted.getCount(), Actionable.MODULATE, this.source);
                } else {
                    this.didSomething = true;
                }
            }
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
