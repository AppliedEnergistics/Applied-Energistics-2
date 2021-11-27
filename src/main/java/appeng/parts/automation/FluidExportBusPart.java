/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import javax.annotation.Nonnull;

import appeng.api.networking.IGrid;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartModel;
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.storage.data.AEKey;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.IOBusMenu;
import appeng.parts.PartModel;

public class FluidExportBusPart extends ExportBusPart<Storage<FluidVariant>> {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE,
            new ResourceLocation(AppEng.MOD_ID, "part/fluid_export_bus_has_channel"));

    public FluidExportBusPart(ItemStack is) {
        super(TickRates.FluidExportBus, is, FluidStorage.SIDED, AEFluidKey.filter());
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.FLUID_EXPORT_TYPE;
    }

    @Override
    protected TickRateModulation doBusWork(IGrid grid) {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        var fh = adjacentExternalApi.find();
        if (fh == null) {
            return TickRateModulation.SLEEP;
        }

        var inv = grid.getStorageService().getInventory();

        for (int i = 0; i < this.getConfig().size(); i++) {
            var what = this.getConfig().getKey(i);
            if (what instanceof AEFluidKey fluidKey) {
                var amount = this.getOperationsPerTick();

                var extracted = inv.extract(what, amount, Actionable.SIMULATE, this.source);
                if (extracted > 0) {
                    try (var tx = Transaction.openOuter()) {
                        long wasInserted = fh.insert(fluidKey.toVariant(), extracted, tx);

                        if (wasInserted > 0) {
                            inv.extract(what, wasInserted, Actionable.MODULATE, this.source);
                            tx.commit();

                            return TickRateModulation.FASTER;
                        }
                    }
                }
            }
        }

        return TickRateModulation.SLOWER;

    }

    @Override
    public long insertCraftedItems(final ICraftingLink link, final AEKey what, long amount, final Actionable mode) {
        if (!(what instanceof AEFluidKey itemKey)) {
            return 0;
        }

        var d = adjacentExternalApi.find();

        var grid = getMainNode().getGrid();
        if (grid != null) {
            if (d != null && this.getMainNode().isActive()) {
                var toInsert = itemKey.toVariant();
                var energy = grid.getEnergyService();

                if (energy.extractAEPower(amount, mode, PowerMultiplier.CONFIG) > amount - 0.01) {
                    long inserted;
                    try (var tx = Transaction.openOuter()) {
                        inserted = d.insert(toInsert, amount, tx);
                        if (mode == Actionable.MODULATE) {
                            tx.commit();
                        }
                    }
                    return inserted;
                }
            }
        }

        return 0;
    }

    @Nonnull
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
