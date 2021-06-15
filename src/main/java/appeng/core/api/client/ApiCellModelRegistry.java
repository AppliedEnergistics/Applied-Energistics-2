/*
 * This file is part of Applied Energistics 2.
 *
 * Copyright (c) 2020, TeamAppliedEnergistics, All rights reserved.
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

package appeng.core.api.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import appeng.api.client.ICellModelRegistry;
import appeng.core.api.definitions.ApiItems;

public class ApiCellModelRegistry implements ICellModelRegistry {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(
            "appliedenergistics2:block/drive/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = new ResourceLocation(
            "appliedenergistics2:block/drive/drive_cell_empty");
    private static final ResourceLocation MODEL_CELL_DEFAULT = new ResourceLocation(
            "appliedenergistics2:block/drive/drive_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_1K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/1k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_4K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/4k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_16K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/16k_item_cell");
    private static final ResourceLocation MODEL_CELL_ITEMS_64K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/64k_item_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_1K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/1k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_4K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/4k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_16K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/16k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_FLUIDS_64K = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/64k_fluid_cell");
    private static final ResourceLocation MODEL_CELL_CREATIVE = new ResourceLocation(
            "appliedenergistics2:block/drive/cells/creative_cell");

    private static final ResourceLocation[] MODELS = { MODEL_BASE, MODEL_CELL_EMPTY, MODEL_CELL_DEFAULT,
            MODEL_CELL_ITEMS_1K, MODEL_CELL_ITEMS_4K, MODEL_CELL_ITEMS_16K, MODEL_CELL_ITEMS_64K, MODEL_CELL_FLUIDS_1K,
            MODEL_CELL_FLUIDS_4K, MODEL_CELL_FLUIDS_16K, MODEL_CELL_FLUIDS_64K, MODEL_CELL_CREATIVE };

    public static Collection<ResourceLocation> getModels() {
        return Arrays.asList(MODELS);
    }

    private final Map<Item, ResourceLocation> registry;

    public ApiCellModelRegistry() {
        this.registry = new IdentityHashMap<>();
        this.registry.put(ApiItems.cell1k().item(), MODEL_CELL_ITEMS_1K);
        this.registry.put(ApiItems.cell4k().item(), MODEL_CELL_ITEMS_4K);
        this.registry.put(ApiItems.cell16k().item(), MODEL_CELL_ITEMS_16K);
        this.registry.put(ApiItems.cell64k().item(), MODEL_CELL_ITEMS_64K);
        this.registry.put(ApiItems.fluidCell1k().item(), MODEL_CELL_FLUIDS_1K);
        this.registry.put(ApiItems.fluidCell4k().item(), MODEL_CELL_FLUIDS_4K);
        this.registry.put(ApiItems.fluidCell16k().item(), MODEL_CELL_FLUIDS_16K);
        this.registry.put(ApiItems.fluidCell64k().item(), MODEL_CELL_FLUIDS_64K);
        this.registry.put(ApiItems.fluidCell64k().item(), MODEL_CELL_FLUIDS_64K);
        this.registry.put(ApiItems.cellCreative().item(), MODEL_CELL_CREATIVE);
    }

    @Override
    public void registerModel(Item item, ResourceLocation model) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(model);
        Preconditions.checkArgument(!this.registry.containsKey(item), "Cannot register an item twice.");

        this.registry.put(item, model);
    }

    @Override
    @Nullable
    public ResourceLocation model(@Nonnull Item item) {
        Preconditions.checkNotNull(item);

        return this.registry.get(item);
    }

    @Override
    @Nonnull
    public Map<Item, ResourceLocation> models() {
        return Collections.unmodifiableMap(this.registry);
    }

    @Override
    @Nonnull
    public ResourceLocation getDefaultModel() {
        return MODEL_CELL_DEFAULT;
    }

}
