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

package appeng.block.qnb;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import appeng.block.paint.QnbFormedBakedModel;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;

class QnbFormedBakedModel implements DynamicBlockStateModel {
    private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of(RenderType.CUTOUT);
    private static final Material TEXTURE_LINK = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/quantum_link"));
    private static final Material TEXTURE_RING = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/quantum_ring"));
    private static final Material TEXTURE_RING_LIGHT = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/quantum_ring_light"));
    private static final Material TEXTURE_RING_LIGHT_CORNER = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/quantum_ring_light_corner"));
    private static final Material TEXTURE_CABLE_GLASS = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("part/cable/glass/transparent"));
    private static final Material TEXTURE_COVERED_CABLE = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("part/cable/covered/transparent"));

    private static final float DEFAULT_RENDER_MIN = 2.0f;
    private static final float DEFAULT_RENDER_MAX = 14.0f;

    private static final float CORNER_POWERED_RENDER_MIN = 3.9f;
    private static final float CORNER_POWERED_RENDER_MAX = 12.1f;

    private static final float CENTER_POWERED_RENDER_MIN = -0.01f;
    private static final float CENTER_POWERED_RENDER_MAX = 16.01f;

    private final BakedModel baseModel;

    private final Block linkBlock;

    private final TextureAtlasSprite linkTexture;
    private final TextureAtlasSprite ringTexture;
    private final TextureAtlasSprite glassCableTexture;
    private final TextureAtlasSprite coveredCableTexture;
    private final TextureAtlasSprite lightTexture;
    private final TextureAtlasSprite lightCornerTexture;

    public QnbFormedBakedModel(BakedModel baseModel, SpriteGetter bakedTextureGetter) {
        this.baseModel = baseModel;
        this.linkTexture = bakedTextureGetter.get(TEXTURE_LINK);
        this.ringTexture = bakedTextureGetter.get(TEXTURE_RING);
        this.glassCableTexture = bakedTextureGetter.get(TEXTURE_CABLE_GLASS);
        this.coveredCableTexture = bakedTextureGetter.get(TEXTURE_COVERED_CABLE);
        this.lightTexture = bakedTextureGetter.get(TEXTURE_RING_LIGHT);
        this.lightCornerTexture = bakedTextureGetter.get(TEXTURE_RING_LIGHT_CORNER);
        this.linkBlock = AEBlocks.QUANTUM_LINK.block();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData modelData, RenderType renderType) {
        QnbFormedState formedState = modelData.get(QuantumBridgeBlockEntity.FORMED_STATE);

        if (formedState == null) {
            return this.baseModel.getQuads(state, side, rand);
        }

        if (side != null) {
            return Collections.emptyList();
        }

        return this.getQuads(formedState, state);
    }

    private List<BakedQuad> getQuads(QnbFormedState formedState, BlockState state) {
        CubeBuilder builder = new CubeBuilder();

        if (state.getBlock() == this.linkBlock) {
            Set<Direction> sides = formedState.adjacentQuantumBridges();

            this.renderCableAt(builder, 0.11f * 16, this.glassCableTexture, 0.141f * 16, sides);

            this.renderCableAt(builder, 0.188f * 16, this.coveredCableTexture, 0.1875f * 16, sides);

            builder.setTexture(this.linkTexture);
            builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX,
                    DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);
        } else if (formedState.corner()) {
            this.renderCableAt(builder, 0.188f * 16, this.coveredCableTexture, 0.05f * 16,
                    formedState.adjacentQuantumBridges());

            builder.setTexture(this.ringTexture);
            builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX,
                    DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);

            if (formedState.powered()) {
                builder.setTexture(this.lightCornerTexture);
                builder.setEmissiveMaterial(true);
                for (Direction facing : Direction.values()) {
                    // Offset the face by a slight amount so that it is drawn over the already drawn
                    // ring texture
                    // (avoids z-fighting)
                    float xOffset = Math.abs(facing.getStepX() * 0.01f);
                    float yOffset = Math.abs(facing.getStepY() * 0.01f);
                    float zOffset = Math.abs(facing.getStepZ() * 0.01f);

                    builder.setDrawFaces(EnumSet.of(facing));
                    builder.addCube(DEFAULT_RENDER_MIN - xOffset, DEFAULT_RENDER_MIN - yOffset,
                            DEFAULT_RENDER_MIN - zOffset, DEFAULT_RENDER_MAX + xOffset,
                            DEFAULT_RENDER_MAX + yOffset, DEFAULT_RENDER_MAX + zOffset);
                }
                builder.setEmissiveMaterial(false);
            }
        } else {
            builder.setTexture(this.ringTexture);

            builder.addCube(0, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, 16, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);

            builder.addCube(DEFAULT_RENDER_MIN, 0, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, 16, DEFAULT_RENDER_MAX);

            builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, 0, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, 16);

            if (formedState.powered()) {
                builder.setTexture(this.lightTexture);
                builder.setEmissiveMaterial(true);
                for (Direction facing : Direction.values()) {
                    // Offset the face by a slight amount so that it is drawn over the already drawn
                    // ring texture
                    // (avoids z-fighting)
                    float xOffset = Math.abs(facing.getStepX() * 0.01f);
                    float yOffset = Math.abs(facing.getStepY() * 0.01f);
                    float zOffset = Math.abs(facing.getStepZ() * 0.01f);

                    builder.setDrawFaces(EnumSet.of(facing));
                    builder.addCube(-xOffset, -yOffset, -zOffset, 16 + xOffset, 16 + yOffset, 16 + zOffset);
                }
            }
        }

        return builder.getOutput();
    }

    private void renderCableAt(CubeBuilder builder, float thickness, TextureAtlasSprite texture, float pull,
            Set<Direction> connections) {
        builder.setTexture(texture);

        if (connections.contains(Direction.WEST)) {
            builder.addCube(0, 8 - thickness, 8 - thickness, 8 - thickness - pull, 8 + thickness, 8 + thickness);
        }

        if (connections.contains(Direction.EAST)) {
            builder.addCube(8 + thickness + pull, 8 - thickness, 8 - thickness, 16, 8 + thickness, 8 + thickness);
        }

        if (connections.contains(Direction.NORTH)) {
            builder.addCube(8 - thickness, 8 - thickness, 0, 8 + thickness, 8 + thickness, 8 - thickness - pull);
        }

        if (connections.contains(Direction.SOUTH)) {
            builder.addCube(8 - thickness, 8 - thickness, 8 + thickness + pull, 8 + thickness, 8 + thickness, 16);
        }

        if (connections.contains(Direction.DOWN)) {
            builder.addCube(8 - thickness, 0, 8 - thickness, 8 + thickness, 8 - thickness - pull, 8 + thickness);
        }

        if (connections.contains(Direction.UP)) {
            builder.addCube(8 - thickness, 8 + thickness + pull, 8 - thickness, 8 + thickness, 16, 8 + thickness);
        }
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.baseModel.getParticleIcon();
    }

    public static List<Material> getRequiredTextures() {
        return ImmutableList.of(TEXTURE_LINK, TEXTURE_RING, TEXTURE_CABLE_GLASS, TEXTURE_COVERED_CABLE,
                TEXTURE_RING_LIGHT, TEXTURE_RING_LIGHT_CORNER);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return RENDER_TYPES;
    }

    @Override
    public void applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        baseModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }

    @Deprecated
    @Override
    public ItemTransforms getTransforms() {
        return baseModel.getTransforms();
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<QnbFormedBakedModel.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group()
                .apply(instance, QnbFormedBakedModel.Unbaked::new));

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new QnbFormedBakedModel();
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<QnbFormedBakedModel.Unbaked> codec() {
            return MAP_CODEC;
        }
    }    
}
