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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;

public class QnbFormedModel implements DynamicBlockStateModel {
    private static final ResourceLocation MODEL_RING = AppEng.makeId("block/quantum_ring");

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

    private final SimpleModelWrapper baseModel;

    private final Block linkBlock;

    private final TextureAtlasSprite linkTexture;
    private final TextureAtlasSprite ringTexture;
    private final TextureAtlasSprite glassCableTexture;
    private final TextureAtlasSprite coveredCableTexture;
    private final TextureAtlasSprite lightTexture;
    private final TextureAtlasSprite lightCornerTexture;

    public QnbFormedModel(SimpleModelWrapper baseModel, SpriteGetter bakedTextureGetter) {
        ModelDebugName debugName = QnbFormedModel.class::toString;

        this.baseModel = baseModel;
        this.linkTexture = bakedTextureGetter.get(TEXTURE_LINK, debugName);
        this.ringTexture = bakedTextureGetter.get(TEXTURE_RING, debugName);
        this.glassCableTexture = bakedTextureGetter.get(TEXTURE_CABLE_GLASS, debugName);
        this.coveredCableTexture = bakedTextureGetter.get(TEXTURE_COVERED_CABLE, debugName);
        this.lightTexture = bakedTextureGetter.get(TEXTURE_RING_LIGHT, debugName);
        this.lightCornerTexture = bakedTextureGetter.get(TEXTURE_RING_LIGHT_CORNER, debugName);
        this.linkBlock = AEBlocks.QUANTUM_LINK.block();
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockModelPart> parts) {
        var modelData = level.getModelData(pos);
        var formedState = modelData.get(QuantumBridgeBlockEntity.FORMED_STATE);

        if (formedState == null) {
            parts.add(baseModel);
            return;
        }

        var renderType = state.is(AEBlocks.QUANTUM_LINK.block()) ? RenderType.CUTOUT : RenderType.SOLID;
        parts.add(new SimpleModelWrapper(
                getQuads(formedState, state),
                true,
                particleIcon(),
                renderType));
    }

    private QuadCollection getQuads(QnbFormedState formedState, BlockState state) {
        var result = new QuadCollection.Builder();
        CubeBuilder builder = new CubeBuilder(result::addUnculledFace);

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

        return result.build();
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
    public TextureAtlasSprite particleIcon() {
        return this.baseModel.particleIcon();
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final ResourceLocation ID = AppEng.makeId("qnb_formed");
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            var ring = SimpleModelWrapper.bake(baker, MODEL_RING, BlockModelRotation.X0_Y0);

            return new QnbFormedModel(ring, baker.sprites());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(MODEL_RING);
        }

        @Override
        public MapCodec<Unbaked> codec() {
            return MAP_CODEC;
        }
    }
}
