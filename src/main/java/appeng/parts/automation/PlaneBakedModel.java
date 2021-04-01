/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import appeng.api.parts.IDynamicPartBakedModel;
import appeng.client.render.cablebus.CubeBuilder;

/**
 * Built-in model for annihilation planes that supports connected textures.
 */
public class PlaneBakedModel implements IBakedModel, IDynamicPartBakedModel {

    private static final PlaneConnections DEFAULT_PERMUTATION = PlaneConnections.of(false, false, false, false);

    private final TextureAtlasSprite frontTexture;

    private final Map<PlaneConnections, Mesh> meshes;

    PlaneBakedModel(TextureAtlasSprite frontTexture, TextureAtlasSprite sidesTexture, TextureAtlasSprite backTexture) {
        this.frontTexture = frontTexture;

        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        meshes = new HashMap<>(PlaneConnections.PERMUTATIONS.size());
        // Create all possible permutations (16)
        for (PlaneConnections permutation : PlaneConnections.PERMUTATIONS) {

            MeshBuilder meshBuilder = renderer.meshBuilder();

            CubeBuilder builder = new CubeBuilder(meshBuilder.getEmitter());

            builder.setTextures(sidesTexture, sidesTexture, frontTexture, backTexture, sidesTexture, sidesTexture);

            // Keep the orientation of the X axis in mind here. When looking at a quad
            // facing north from the front,
            // The X-axis points left
            int minX = permutation.isRight() ? 0 : 1;
            int maxX = permutation.isLeft() ? 16 : 15;
            int minY = permutation.isDown() ? 0 : 1;
            int maxY = permutation.isUp() ? 16 : 15;

            builder.addCube(minX, minY, 0, maxX, maxY, 1);

            this.meshes.put(permutation, meshBuilder.build());
        }
    }

    @Override
    public void emitQuads(IBlockDisplayReader blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier,
            RenderContext context, Direction partSide, @Nullable Object modelData) {
        PlaneConnections connections = DEFAULT_PERMUTATION;

        if (modelData instanceof PlaneConnections) {
            connections = (PlaneConnections) modelData;
        }

        context.meshConsumer().accept(this.meshes.get(connections));
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.frontTexture;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
