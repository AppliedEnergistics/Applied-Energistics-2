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

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import appeng.block.storage.DriveModelData;
import appeng.client.model.SpinnableVariant;
import appeng.core.AppEng;
import appeng.thirdparty.fabric.ModelHelper;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import com.google.common.collect.ImmutableSet;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import appeng.api.client.StorageCellModels;
import appeng.client.render.BasicUnbakedModel;
import appeng.init.internal.InitStorageCells;

public class DriveModel implements DynamicBlockStateModel {
    private static final Logger LOG = LoggerFactory.getLogger(DriveModel.class);

    private static final ResourceLocation MODEL_BASE = ResourceLocation.parse("ae2:block/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = ResourceLocation.parse("ae2:block/drive_cell_empty");

    private final Map<Item, SimpleModelWrapper> cellModels;
    private final SimpleModelWrapper baseModel;
    private final SimpleModelWrapper defaultCellModel;

    private final RenderContext.QuadTransform[] slotTransforms;

    public DriveModel(Transformation rotation, SimpleModelWrapper baseModel, Map<Item, SimpleModelWrapper> cellModels,
                           SimpleModelWrapper defaultCell) {
        this.baseModel = baseModel;
        this.defaultCellModel = defaultCell;
        this.slotTransforms = buildSlotTransforms(rotation);
        this.cellModels = cellModels;
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
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        parts.add(baseModel);

        var cells = level.getModelData(pos).get(DriveModelData.STATE);

        // Add cell models on top of the base model, if possible
        if (cells != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 2; col++) {
                    int slot = getSlotIndex(row, col);

                    // Add the cell chassis
                    Item cell = slot < cells.length ? cells[slot] : null;
                    var cellChassisModel = getCellChassisModel(cell);

                    // TODO: We should pre-compile this
                    var unculledQuads = new ArrayList<BakedQuad>();
                    var quadView = MutableQuadView.getInstance();
                    for (BakedQuad quad : cellChassisModel.getQuads(null)) {
                        quadView.fromVanilla(quad, null);
                        slotTransforms[slot].transform(quadView);
                        unculledQuads.add(quadView.toBlockBakedQuad());
                    }
                    parts.add(new BlockModelPart() {
                        @Override
                        public List<BakedQuad> getQuads(@Nullable Direction side) {
                            return side == null ? unculledQuads : List.of();
                        }

                        @Override
                        public boolean useAmbientOcclusion() {
                            return cellChassisModel.useAmbientOcclusion();
                        }

                        @Override
                        public TextureAtlasSprite particleIcon() {
                            return cellChassisModel.particleIcon();
                        }
                    });
                }
            }
        }
    }

    // TODO 1.21.5 shuld be in the base model @Override
    // TODO 1.21.5 shuld be in the base model public boolean useAmbientOcclusion() {
    // TODO 1.21.5 shuld be in the base model     // We have faces inside the chassis that are facing east, but should not receive
    // TODO 1.21.5 shuld be in the base model     // ambient occlusion from the east-side, but sadly this cannot be fine-tuned on
    // TODO 1.21.5 shuld be in the base model     // a face-by-face basis.
    // TODO 1.21.5 shuld be in the base model     return false;
    // TODO 1.21.5 shuld be in the base model }

    @Override
    public TextureAtlasSprite particleIcon() {
        return baseModel.particleIcon();
    }

    // Determine which drive chassis to show based on the used cell
    public SimpleModelWrapper getCellChassisModel(Item cell) {
        if (cell == null) {
            return cellModels.get(Items.AIR);
        }
        final SimpleModelWrapper model = cellModels.get(cell);

        return model != null ? model : defaultCellModel;
    }

    private RenderContext.QuadTransform[] buildSlotTransforms(Transformation rotation) {
        RenderContext.QuadTransform[] result = new RenderContext.QuadTransform[5 * 2];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {

                Vector3f translation = new Vector3f();
                getSlotOrigin(row, col, translation);
                rotation.getLeftRotation().transform(translation);

                result[getSlotIndex(row, col)] = new QuadTranslator(translation.x(), translation.y(),
                        translation.z());
            }
        }

        return result;
    }

    private static int getSlotIndex(int row, int col) {
        return row * 2 + col;
    }

    private static class QuadTranslator implements RenderContext.QuadTransform {
        private final float x;
        private final float y;
        private final float z;

        public QuadTranslator(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            Vector3f target = new Vector3f();
            for (int i = 0; i < 4; i++) {
                quad.copyPos(i, target);
                target.add(x, y, z);
                quad.pos(i, target);
            }
            return true;
        }
    }

    public record Unbaked(SpinnableVariant variant) implements CustomUnbakedBlockStateModel {
        public static final ResourceLocation ID = AppEng.makeId("drive");
        public static MapCodec<Unbaked> MAP_CODEC = SpinnableVariant.MAP_CODEC.xmap(Unbaked::new, Unbaked::variant);

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            final Map<Item, SimpleModelWrapper> cellModels = new IdentityHashMap<>();

            var modelState = variant.modelState().asModelState();

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

                var cellModel = SimpleModelWrapper.bake(baker, location, modelState);
                cellModels.put(entry.getKey(), cellModel);
            }

            var baseModel = SimpleModelWrapper.bake(baker, MODEL_BASE, modelState);
            var defaultCell = SimpleModelWrapper.bake(baker, StorageCellModels.getDefaultModel(), modelState);
            cellModels.put(Items.AIR, SimpleModelWrapper.bake(baker, MODEL_CELL_EMPTY, modelState));

            return new DriveModel(modelState.transformation(), baseModel, cellModels, defaultCell);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(StorageCellModels.getDefaultModel());
            InitStorageCells.getModels().forEach(resolver::markDependency);
            StorageCellModels.models().values().forEach(resolver::markDependency);
        }
    }

}
