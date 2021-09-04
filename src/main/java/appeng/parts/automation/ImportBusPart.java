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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.helpers.ItemTransfer;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.ItemIOBusMenu;
import appeng.parts.PartModel;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class ImportBusPart extends SharedItemBusPart {

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

    private final IActionSource source;
    private int itemsToSend; // used in tickingRequest
    private boolean worked; // used in tickingRequest
    // Used for extract calls to InventoryAdaptor.
    private final Predicate<ItemStack> insertionPredicate = this::canInsert;

    public ImportBusPart(final ItemStack is) {
        super(is);

        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.source = new MachineSource(this);
    }

    public boolean canInsert(final ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == Items.AIR) {
            return false;
        }

        var grid = getMainNode().getGrid();
        if (grid != null) {
            var inv = grid.getStorageService()
                    .getInventory(StorageChannels.items());

            final IAEItemStack out = inv.injectItems(
                    StorageChannels.items().createStack(stack),
                    Actionable.SIMULATE, this.source);
            if (out == null) {
                return true;
            }
            return out.getStackSize() != stack.getCount();
        } else {
            return false;
        }
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(ItemIOBusMenu.IMPORT_TYPE, player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.ImportBus.getMin(), TickRates.ImportBus.getMax(), this.isSleeping(), false);
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
                this.itemsToSend = this.calculateItemsToSend();

                var inv = grid.getStorageService()
                        .getInventory(StorageChannels.items());
                var energy = grid.getEnergyService();

                boolean Configured = false;
                for (int x = 0; x < this.availableSlots(); x++) {
                    final IAEItemStack ais = this.getConfig().getAEStackInSlot(x);
                    if (ais != null && this.itemsToSend > 0) {
                        Configured = true;
                        while (this.itemsToSend > 0) {
                            if (this.importStuff(myAdaptor, ais, inv, energy, fzMode)) {
                                break;
                            }
                        }
                    }
                }

                if (!Configured) {
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

    private boolean importStuff(InternalInventory srcInv, final IAEItemStack whatToImport,
            final IMEMonitor<IAEItemStack> inv, final IEnergySource energy, final FuzzyMode fzMode) {
        final int toSend = this.calculateMaximumAmountToImport(srcInv, whatToImport, inv, fzMode);
        final ItemStack newItems;

        if (getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            newItems = ItemTransfer.removeSimilarItems(srcInv, toSend,
                    whatToImport == null ? ItemStack.EMPTY : whatToImport.getDefinition(), fzMode, insertionPredicate);
        } else {
            newItems = ItemTransfer.removeItems(srcInv, toSend,
                    whatToImport == null ? ItemStack.EMPTY : whatToImport.getDefinition(), insertionPredicate);
        }

        if (!newItems.isEmpty()) {
            final IAEItemStack aeStack = StorageChannels.items()
                    .createStack(newItems);
            final IAEItemStack failed = Platform.poweredInsert(energy, inv, aeStack, this.source);

            if (failed != null) {
                // try unpowered insert, better be a bit lenient then void items
                final IAEItemStack spill = inv.injectItems(failed, Actionable.MODULATE, this.source);
                if (spill != null) {
                    // last resort try to put it back .. lets hope it's a chest type of thing
                    srcInv.addItems(spill.createItemStack());
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

    private int calculateMaximumAmountToImport(InternalInventory srcInv, final IAEItemStack whatToImport,
            final IMEMonitor<IAEItemStack> inv, final FuzzyMode fzMode) {
        final int toSend = Math.min(this.itemsToSend, 64);
        final ItemStack itemStackToImport;

        if (whatToImport == null) {
            itemStackToImport = ItemStack.EMPTY;
        } else {
            itemStackToImport = whatToImport.getDefinition();
        }

        final IAEItemStack itemAmountNotStorable;
        final ItemStack simResult;
        if (getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            simResult = ItemTransfer.simulateSimilarRemove(srcInv, toSend, itemStackToImport, fzMode,
                    insertionPredicate);
        } else {
            simResult = ItemTransfer.simulateRemove(srcInv, toSend, itemStackToImport, insertionPredicate);
        }
        itemAmountNotStorable = inv.injectItems(AEItemStack.fromItemStack(simResult), Actionable.SIMULATE,
                this.source);

        if (itemAmountNotStorable != null) {
            return (int) Math.min(simResult.getCount() - itemAmountNotStorable.getStackSize(), toSend);
        }

        return toSend;
    }

    @Override
    protected boolean isSleeping() {
        return this.getHandler() == null || super.isSleeping();
    }

    @Override
    public RedstoneMode getRSMode() {
        return this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
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
