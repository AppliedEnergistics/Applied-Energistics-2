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

import appeng.block.storage.DriveSlotsState;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

public class DriveBakedModel extends ForwardingBakedModel implements FabricBakedModel {
    private final Map<Item, BakedModel> cellModels;
    private final Map<Item, Mesh> bakedCells;
    private final BakedModel defaultCellModel;
    private final Mesh defaultCell;

    private final RenderContext.QuadTransform[] slotTransforms;

    public DriveBakedModel(BakedModel bakedBase, Map<Item, BakedModel> cellModels, BakedModel defaultCell) {
        this.wrapped = bakedBase;
        this.defaultCellModel = defaultCell;
        this.defaultCell = convertCellModel(defaultCell);
        this.slotTransforms = buildSlotTransforms();
        this.bakedCells = convertCellModels(cellModels);
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
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {

        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);

        // Add cell models on top of the base model, if possible
        DriveSlotsState slotsState = getDriveSlotsState(blockView, pos);
        if (slotsState != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 2; col++) {
                    int slot = getSlotIndex(row, col);

                    // Add the cell chassis
                    Item cell = slotsState.getCell(slot);
                    BakedModel cellChassisModel = getCellChassisModel(cell);

                    context.pushTransform(slotTransforms[slot]);
                    context.fallbackConsumer().accept(cellChassisModel);
                    context.meshConsumer().accept(getCellChassisMesh(cell));
                    context.popTransform();
                }
            }
        }

    }

    private static DriveSlotsState getDriveSlotsState(BlockRenderView blockView, BlockPos pos) {
        if (!(blockView instanceof RenderAttachedBlockView)) {
            return null;
        }
        Object attachedData = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        if (!(attachedData instanceof DriveModelData)) {
            return null;
        }

        return ((DriveModelData) attachedData).getSlotsState();
    }

    @Override
    public boolean useAmbientOcclusion() {
        // We have faces inside the chassis that are facing east, but should not receive
        // ambient occlusion from the east-side, but sadly this cannot be fine-tuned on
        // a face-by-face basis.
        return false;
    }

    // Determine which drive chassis to show based on the used cell
    public Mesh getCellChassisMesh(Item cell) {
        if (cell == null) {
            return bakedCells.get(Items.AIR);
        }
        final Mesh model = bakedCells.get(cell);

        return model != null ? model : defaultCell;
    }

    public BakedModel getCellChassisModel(Item cell) {
        if (cell == null) {
            return cellModels.get(Items.AIR);
        }
        final BakedModel model = cellModels.get(cell);

        return model != null ? model : defaultCellModel;
    }

    private RenderContext.QuadTransform[] buildSlotTransforms() {
        RenderContext.QuadTransform[] result = new RenderContext.QuadTransform[5 * 2];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {

                Vector3f translation = new Vector3f();
                getSlotOrigin(row, col, translation);

                result[getSlotIndex(row, col)] = new QuadTranslator(translation.getX(), translation.getY(), translation.getZ());
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

    private Map<Item, Mesh> convertCellModels(Map<Item, BakedModel> cellModels) {
        Map<Item, Mesh> result = new IdentityHashMap<>();

        for (Map.Entry<Item, BakedModel> entry : cellModels.entrySet()) {
            result.put(entry.getKey(), convertCellModel(entry.getValue()));
        }

        return result;
    }

    private Mesh convertCellModel(BakedModel bakedModel) {
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();
        Random random = new Random();
        MeshBuilder meshBuilder = renderer.meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();
        emitter.material(renderer.materialFinder().disableDiffuse(0, false).disableAo(0, true).find());

        for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
            Direction face = ModelHelper.faceFromIndex(i);
            List<BakedQuad> quads = bakedModel.getQuads(null, face, random);
            for (BakedQuad quad : quads) {
                emitter.fromVanilla(quad.getVertexData(), 0, false);
                emitter.cullFace(face);
                emitter.nominalFace(face);
                emitter.emit();
            }
        }
        return meshBuilder.build();
    }

}
