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

package appeng.init.internal;

import java.util.Arrays;
import java.util.Collection;

import net.minecraft.resources.ResourceLocation;

import appeng.api.client.StorageCellModels;
import appeng.api.storage.StorageCells;
import appeng.core.definitions.AEItems;
import appeng.core.registries.cell.BasicFluidCellGuiHandler;
import appeng.core.registries.cell.BasicItemCellGuiHandler;
import appeng.me.cells.BasicCellHandler;
import appeng.me.cells.CreativeCellHandler;

public final class InitStorageCells {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(
            "ae2:block/drive/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = new ResourceLocation(
            "ae2:block/drive/drive_cell_empty");
    private static final ResourceLocation MODEL_CELL_ITEMS_1K = new ResourceLocation(
            "ae2:block/drive/cells/1k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_4K = new ResourceLocation(
            "ae2:block/drive/cells/4k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_16K = new ResourceLocation(
            "ae2:block/drive/cells/16k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_64K = new ResourceLocation(
            "ae2:block/drive/cells/64k_item_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_1K = new ResourceLocation(
            "ae2:block/drive/cells/1k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_4K = new ResourceLocation(
            "ae2:block/drive/cells/4k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_16K = new ResourceLocation(
            "ae2:block/drive/cells/16k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_64K = new ResourceLocation(
            "ae2:block/drive/cells/64k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_CREATIVE = new ResourceLocation(
            "ae2:block/drive/cells/creative_cell");

    private static final ResourceLocation[] MODELS = { MODEL_BASE, MODEL_CELL_EMPTY,
            StorageCellModels.getDefaultModel(),
            MODEL_CELL_ITEMS_1K, MODEL_CELL_ITEMS_4K, MODEL_CELL_ITEMS_16K, MODEL_CELL_ITEMS_64K, MODEL_CELL_FLUIDS_1K,
            MODEL_CELL_FLUIDS_4K, MODEL_CELL_FLUIDS_16K, MODEL_CELL_FLUIDS_64K, MODEL_CELL_CREATIVE };

    public static Collection<ResourceLocation> getModels() {
        return Arrays.asList(MODELS);
    }

    private InitStorageCells() {
    }

    public static void init() {
        StorageCells.addCellHandler(BasicCellHandler.INSTANCE);
        StorageCells.addCellHandler(new CreativeCellHandler());
        StorageCells.addCellGuiHandler(new BasicItemCellGuiHandler());
        StorageCells.addCellGuiHandler(new BasicFluidCellGuiHandler());

        StorageCellModels.registerModel(AEItems.CELL1K, MODEL_CELL_ITEMS_1K);
        StorageCellModels.registerModel(AEItems.CELL4K, MODEL_CELL_ITEMS_4K);
        StorageCellModels.registerModel(AEItems.CELL16K, MODEL_CELL_ITEMS_16K);
        StorageCellModels.registerModel(AEItems.CELL64K, MODEL_CELL_ITEMS_64K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL1K, MODEL_CELL_FLUIDS_1K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL4K, MODEL_CELL_FLUIDS_4K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL16K, MODEL_CELL_FLUIDS_16K);
        StorageCellModels.registerModel(AEItems.FLUID_CELL64K, MODEL_CELL_FLUIDS_64K);
        StorageCellModels.registerModel(AEItems.ITEM_CELL_CREATIVE, MODEL_CELL_CREATIVE);
        StorageCellModels.registerModel(AEItems.FLUID_CELL_CREATIVE, MODEL_CELL_CREATIVE);
    }

}
