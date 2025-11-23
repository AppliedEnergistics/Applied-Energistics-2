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

package appeng.client.render.model;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.ComposedModelState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import appeng.api.client.StorageCellModels;
import appeng.block.storage.DriveModelData;
import appeng.client.model.SpinnableVariant;
import appeng.core.AppEng;

public class DriveModel implements DynamicBlockStateModel {
    private static final Logger LOG = LoggerFactory.getLogger(DriveModel.class);

    private static final Identifier MODEL_BASE = Identifier.parse("ae2:block/drive_base");
    private static final Transformation[] BAY_TRANSLATIONS = buildBayTranslations();

    private final BlockModelPart baseModel;

    // Indices are the bay indices
    private final BlockModelPart[] defaultCellModels;
    private final Map<Item, BlockModelPart[]> cellModels;

    public DriveModel(BlockModelPart baseModel,
            Map<Item, BlockModelPart[]> cellModels,
            BlockModelPart[] defaultCellModels) {
        this.baseModel = baseModel;
        this.defaultCellModels = defaultCellModels;
        this.cellModels = cellModels;
    }

    /**
     * Pre-computes a {@link Transformation} from the base cell model into each respective bay. This assumes no further
     * rotation of the drive.
     */
    private static Transformation[] buildBayTranslations() {
        var result = new Transformation[5 * 2];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {
                Vector3f translation = new Vector3f();
                getSlotOrigin(row, col, translation);
                result[getBayIndex(row, col)] = new Transformation(translation, null, null, null);
            }
        }

        return result;
    }

    /**
     * Calculates the origin of a drive slot for positioning a cell model into it.
     */
    public static void getSlotOrigin(int row, int col, Vector3f translation) {
        // Position this drive model copy at the correct slot. The transform is based on
        // the cell-model being in slot 0,0,0 while the upper left slot's origin is at
        // 9,13,1
        float xOffset = (9 - col * 8) / 16.0f;
        float yOffset = (13 - row * 3) / 16.0f;
        float zOffset = 1 / 16.0f;
        translation.set(xOffset, yOffset, zOffset);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockModelPart> parts) {
        parts.add(baseModel);

        var cells = level.getModelData(pos).get(DriveModelData.STATE);

        // Add cell models on top of the base model, if possible
        if (cells != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 2; col++) {
                    int slot = getBayIndex(row, col);

                    // Add the cell chassis
                    Item cell = slot < cells.length ? cells[slot] : null;
                    if (cell != null && cell != Items.AIR) {
                        parts.add(getCellChassisModel(cell, row, col));
                    }
                }
            }
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return baseModel.particleIcon();
    }

    // Determine which drive chassis to show based on the used cell
    @Nullable
    public BlockModelPart getCellChassisModel(Item cell, int row, int col) {
        if (cell == null) {
            return null;
        }

        var bayIndex = getBayIndex(row, col);
        var models = cellModels.get(cell);
        return models != null ? models[bayIndex] : defaultCellModels[bayIndex];
    }

    private static int getBayIndex(int row, int col) {
        return row * 2 + col;
    }

    public record Unbaked(SpinnableVariant variant) implements CustomUnbakedBlockStateModel {
        public static final Identifier ID = AppEng.makeId("drive");
        public static MapCodec<Unbaked> MAP_CODEC = SpinnableVariant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            final Map<Item, BlockModelPart[]> cellModels = new IdentityHashMap<>();
            var modelState = variant.modelState().asModelState();

            ModelState[] bayTransforms = new ModelState[BAY_TRANSLATIONS.length];
            for (int i = 0; i < BAY_TRANSLATIONS.length; i++) {
                bayTransforms[i] = new ComposedModelState(modelState, BAY_TRANSLATIONS[i]);
            }

            // Load the base model and the model for each cell model.
            for (var entry : StorageCellModels.models().entrySet()) {
                var location = entry.getValue();
                // TODO: This might now be validated independently in vanilla
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    var slots = baker.getModel(location).getTopTextureSlots();
                    if (slots.getMaterial("particle") == null) {
                        LOG.error("Storage cell model {} is missing a 'particle' texture", location);
                    }
                }

                cellModels.put(entry.getKey(), preBakeCellInBays(baker, bayTransforms, location));
            }
            var defaultCells = preBakeCellInBays(baker, bayTransforms, StorageCellModels.getDefaultModel());

            var baseModel = SimpleModelWrapper.bake(baker, MODEL_BASE, modelState);

            return new DriveModel(baseModel, cellModels, defaultCells);
        }

        private BlockModelPart @NotNull [] preBakeCellInBays(ModelBaker baker, ModelState[] bayTransforms,
                Identifier location) {
            // Bake each cell pre-translated into each of the bays
            var cellsInBay = new BlockModelPart[BAY_TRANSLATIONS.length];
            for (int i = 0; i < bayTransforms.length; i++) {
                cellsInBay[i] = SimpleModelWrapper.bake(baker, location, bayTransforms[i]);
            }
            return cellsInBay;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(MODEL_BASE);
            resolver.markDependency(StorageCellModels.getDefaultModel());
            StorageCellModels.models().values().forEach(resolver::markDependency);
        }
    }

}
