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

import java.util.function.Predicate;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.ItemTransfer;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartModel;
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEItemKey;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.IOBusMenu;
import appeng.parts.PartModel;
import appeng.util.Platform;

public class ItemImportBusPart extends ImportBusPart<AEItemKey, Storage<ItemVariant>> {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/item_import_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_import_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_import_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/item_import_bus_has_channel"));

    private int itemsToSend; // used in tickingRequest
    private boolean worked; // used in tickingRequest
    // Used for extract calls to InventoryAdaptor.
    private final Predicate<ItemStack> insertionPredicate = this::canInsert;

    public ItemImportBusPart(final ItemStack is) {
        super(TickRates.ItemExportBus, is, ItemStorage.SIDED, AEItemKey.filter());
    }

    @Override
    protected AEKeySpace getChannel() {
        return AEKeySpace.items();
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.ITEM_IMPORT_TYPE;
    }

    public boolean canInsert(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return canInsert(AEItemKey.of(stack), stack.getCount());
    }

    public boolean canInsert(AEItemKey what, long amount) {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            var inv = grid.getStorageService()
                    .getInventory();

            var inserted = inv.insert(
                    what,
                    amount,
                    Actionable.SIMULATE, this.source);
            return inserted > 0;
        } else {
            return false;
        }
    }

    protected ItemTransfer getHandler() {
        final BlockEntity self = this.getHost().getBlockEntity();
        final BlockEntity target = Platform.getTickingBlockEntity(getLevel(),
                self.getBlockPos().relative(this.getSide()));

        return InternalInventory.wrapExternal(target, this.getSide().getOpposite());
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        this.worked = false;

        var myAdaptor = this.getHandler();
        final FuzzyMode fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);

        if (myAdaptor != null) {
            getMainNode().ifPresent(grid -> {
                this.itemsToSend = this.calculateAmountPerTick();

                var inv = grid.getStorageService()
                        .getInventory();
                var energy = grid.getEnergyService();

                boolean configured = false;
                for (int x = 0; x < this.availableSlots(); x++) {
                    var what = this.getConfig().getKey(x);
                    if (what instanceof AEItemKey itemKey && this.itemsToSend > 0) {
                        configured = true;
                        while (this.itemsToSend > 0) {
                            if (this.importStuff(myAdaptor, itemKey, inv, energy, fzMode)) {
                                break;
                            }
                        }
                    }
                }

                if (!configured) {
                    while (this.itemsToSend > 0) {
                        if (this.importStuff(myAdaptor, null, inv, energy, fzMode)) {
                            break;
                        }
                    }
                }
            });
        } else {
            return TickRateModulation.SLEEP;
        }

        return this.worked ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    private boolean importStuff(ItemTransfer srcInv, AEItemKey whatToImport,
            final MEMonitorStorage inv, final IEnergySource energy, final FuzzyMode fzMode) {
        final int toSend = this.calculateMaximumAmountToImport(srcInv, whatToImport, inv, fzMode);
        final ItemStack newItems;

        if (getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            newItems = srcInv.removeSimilarItems(toSend,
                    whatToImport == null ? ItemStack.EMPTY : whatToImport.toStack(), fzMode, insertionPredicate);
        } else {
            newItems = srcInv.removeItems(toSend,
                    whatToImport == null ? ItemStack.EMPTY : whatToImport.toStack(), insertionPredicate);
        }

        if (!newItems.isEmpty()) {
            var what = AEItemKey.of(newItems);
            var amount = newItems.getCount();
            var inserted = StorageHelper.poweredInsert(energy, inv, what, amount, this.source);

            if (inserted < amount) {
                // try unpowered insert, better be a bit lenient then void items
                inserted += inv.insert(what, amount - inserted, Actionable.MODULATE, this.source);
                if (inserted < amount) {
                    // last resort try to put it back .. lets hope it's a chest type of thing
                    srcInv.addItems(what.toStack((int) (amount - inserted)));
                }
                return true;
            } else {
                this.itemsToSend -= newItems.getCount();
                this.worked = true;
            }
        } else {
            return true;
        }

        return false;
    }

    private int calculateMaximumAmountToImport(ItemTransfer srcInv, AEItemKey whatToImport,
            MEMonitorStorage inv, FuzzyMode fzMode) {
        int toSend = Math.min(this.itemsToSend, 64);
        ItemStack itemStackToImport;

        if (whatToImport == null) {
            itemStackToImport = ItemStack.EMPTY;
        } else {
            itemStackToImport = whatToImport.toStack();
        }

        ItemStack simResult;
        if (getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            simResult = srcInv.simulateSimilarRemove(toSend, itemStackToImport, fzMode, insertionPredicate);
        } else {
            simResult = srcInv.simulateRemove(toSend, itemStackToImport, insertionPredicate);
        }
        return (int) inv.insert(AEItemKey.of(simResult), simResult.getCount(), Actionable.SIMULATE,
                this.source);
    }

    @Override
    protected boolean isSleeping() {
        return this.getHandler() == null || super.isSleeping();
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
