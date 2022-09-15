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

package appeng.client.render.cablebus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.networking.CableBusBlock;
import appeng.client.render.model.AEModelData;
import appeng.thirdparty.fabric.MeshBuilderImpl;

public class CableBusBakedModel implements IDynamicBakedModel {

    // The number of quads overall that will be cached
    private static final int CACHE_QUAD_COUNT = 5000;

    /**
     * Lookup table to match the spin of a part with an up direction.
     * <p>
     * DUNSWE for the facing index, 4 spin values per facing.
     */
    private static final Direction[] SPIN_TO_DIRECTION = new Direction[] {
            Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST, // DOWN
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, // UP
            Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST, // NORTH
            Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST, // SOUTH
            Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH, // WEST
            Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH // EAST
    };

    /**
     * Used to hold extra ModelData for facade rendering.
     * <p>
     * We can't directly query it in {@link #getQuads(BlockState, Direction, RandomSource, ModelData, RenderType)} as we
     * need a {@link BlockAndTintGetter}, so we query it in {@link #getModelData} and store it in a model property.
     */
    // TODO: now that we're storing the level anyway, might as well query it
    private static final ModelProperty<FacadeModelData> FACADE_DATA = new ModelProperty<>();

    private record FacadeModelData(EnumMap<Direction, ModelData> facadeData, BlockAndTintGetter level) {
    }

    private final LoadingCache<CableBusRenderState, List<BakedQuad>> cableModelCache;

    private final CableBuilder cableBuilder;

    private final FacadeBuilder facadeBuilder;

    private final Map<ResourceLocation, BakedModel> partModels;

    private final TextureAtlasSprite particleTexture;

    CableBusBakedModel(CableBuilder cableBuilder, FacadeBuilder facadeBuilder,
            Map<ResourceLocation, BakedModel> partModels, TextureAtlasSprite particleTexture) {
        this.cableBuilder = cableBuilder;
        this.facadeBuilder = facadeBuilder;
        this.partModels = partModels;
        this.particleTexture = particleTexture;
        this.cableModelCache = CacheBuilder.newBuilder()//
                .maximumWeight(CACHE_QUAD_COUNT)//
                .weigher((Weigher<CableBusRenderState, List<BakedQuad>>) (key, value) -> value.size())//
                .build(new CacheLoader<CableBusRenderState, List<BakedQuad>>() {
                    @Override
                    public List<BakedQuad> load(CableBusRenderState renderState) {
                        final List<BakedQuad> model = new ArrayList<>();
                        addCableQuads(renderState, model);
                        return model;
                    }
                });
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
            @NotNull BlockState state, @NotNull ModelData data) {
        CableBusRenderState renderState = data.get(CableBusRenderState.PROPERTY);
        if (renderState == null || renderState.getFacades().isEmpty()) {
            return data;
        }

        var dispatcher = Minecraft.getInstance().getBlockRenderer();

        EnumMap<Direction, ModelData> facadeModelData = new EnumMap<>(Direction.class);
        for (var entry : renderState.getFacades().entrySet()) {
            var side = entry.getKey();
            CableBusBlock.RENDERING_FACADE_DIRECTION.set(side);
            try {
                var blockState = entry.getValue().getSourceBlock();
                var model = dispatcher.getBlockModel(blockState);
                facadeModelData.put(side, model.getModelData(level, pos, blockState, data));
            } finally {
                CableBusBlock.RENDERING_FACADE_DIRECTION.set(null);
            }
        }
        return data.derive().with(FACADE_DATA, new FacadeModelData(facadeModelData, level)).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData data, RenderType renderType) {
        CableBusRenderState renderState = data.get(CableBusRenderState.PROPERTY);

        if (renderState == null || side != null) {
            return Collections.emptyList();
        }

        List<BakedQuad> quads = new ArrayList<>();

        // The core parts of the cable will only be rendered in the CUTOUT layer.
        // Facades will add themselves to what ever the block would be rendered with,
        // except when transparent facades are enabled, they are forced to TRANSPARENT.
        if (renderType == null || renderType == RenderType.cutout()) {

            // First, handle the cable at the center of the cable bus
            final List<BakedQuad> cableModel = cableModelCache.getUnchecked(renderState);
            quads.addAll(cableModel);

            var meshBuilder = new MeshBuilderImpl();
            var emitter = meshBuilder.getEmitter();

            // Then handle attachments
            for (Direction facing : Direction.values()) {
                final IPartModel partModel = renderState.getAttachments().get(facing);
                if (partModel == null) {
                    continue;
                }

                ModelData partModelData = renderState.getPartModelData().get(facing);
                if (partModelData == null) {
                    partModelData = ModelData.EMPTY;
                }

                for (var model : partModel.getModels()) {
                    BakedModel bakedModel = this.partModels.get(model);

                    if (bakedModel == null) {
                        throw new IllegalStateException("Trying to use an unregistered part model: " + model);
                    }

                    List<BakedQuad> partQuads = bakedModel.getQuads(state, null, rand, partModelData, renderType);

                    Direction spinDirection = getPartSpin(facing, partModelData);

                    // Rotate quads accordingly
                    var rotator = QuadRotator.get(facing, spinDirection);

                    for (var partQuad : partQuads) {
                        emitter.fromVanilla(partQuad, null);
                        rotator.transform(emitter);
                        quads.add(emitter.toBakedQuad(0, partQuad.getSprite(), false));
                    }
                }
            }
        }

        FacadeModelData facadeData = data.get(FACADE_DATA);
        if (facadeData != null) {
            this.facadeBuilder
                    .getFacadeMesh(renderState, () -> rand, facadeData.level, facadeData.facadeData, renderType)
                    .forEach(qv -> quads.add(qv.toBlockBakedQuad()));
        }

        return quads;
    }

    // Determines whether a cable is connected to exactly two sides that are
    // opposite each other
    private static boolean isStraightLine(AECableType cableType, EnumMap<Direction, AECableType> sides) {
        final Iterator<Entry<Direction, AECableType>> it = sides.entrySet().iterator();
        if (!it.hasNext()) {
            return false; // No connections
        }

        final Entry<Direction, AECableType> nextConnection = it.next();
        final Direction firstSide = nextConnection.getKey();
        final AECableType firstType = nextConnection.getValue();

        if (!it.hasNext()) {
            return false; // Only a single connection
        }
        if (firstSide.getOpposite() != it.next().getKey()) {
            return false; // Connected to two sides that are not opposite each other
        }
        if (it.hasNext()) {
            return false; // Must not have any other connection points
        }

        final AECableType secondType = sides.get(firstSide.getOpposite());

        return firstType == secondType && cableType == firstType && cableType == secondType;
    }

    private static Direction getPartSpin(Direction facing, ModelData partModelData) {
        var spin = partModelData.get(AEModelData.SPIN);
        if (spin != null) {
            return SPIN_TO_DIRECTION[facing.ordinal() * 4 + spin];
        }

        return Direction.UP;
    }

    private void addCableQuads(CableBusRenderState renderState, List<BakedQuad> quadsOut) {
        AECableType cableType = renderState.getCableType();
        if (cableType == AECableType.NONE) {
            return;
        }

        AEColor cableColor = renderState.getCableColor();
        EnumMap<Direction, AECableType> connectionTypes = renderState.getConnectionTypes();

        // If the connection is straight, no busses are attached, and no covered core
        // has been forced (in case of glass
        // cables), then render the cable as a simplified straight line.
        boolean noAttachments = !renderState.getAttachments().values().stream()
                .anyMatch(IPartModel::requireCableConnection);
        if (noAttachments && isStraightLine(cableType, connectionTypes)) {
            Direction facing = connectionTypes.keySet().iterator().next();

            switch (cableType) {
                case GLASS:
                    this.cableBuilder.addStraightGlassConnection(facing, cableColor, quadsOut);
                    break;
                case COVERED:
                    this.cableBuilder.addStraightCoveredConnection(facing, cableColor, quadsOut);
                    break;
                case SMART:
                    this.cableBuilder.addStraightSmartConnection(facing, cableColor,
                            renderState.getChannelsOnSide().get(facing), quadsOut);
                    break;
                case DENSE_COVERED:
                    this.cableBuilder.addStraightDenseCoveredConnection(facing, cableColor, quadsOut);
                    break;
                case DENSE_SMART:
                    this.cableBuilder.addStraightDenseSmartConnection(facing, cableColor,
                            renderState.getChannelsOnSide().get(facing), quadsOut);
                    break;
                default:
                    break;
            }

            return; // Don't render the other form of connection
        }

        this.cableBuilder.addCableCore(renderState.getCoreType(), cableColor, quadsOut);

        // Render all internal connections to attachments
        EnumMap<Direction, Integer> attachmentConnections = renderState.getAttachmentConnections();
        for (Direction facing : attachmentConnections.keySet()) {
            int distance = attachmentConnections.get(facing);
            int channels = renderState.getChannelsOnSide().get(facing);

            switch (cableType) {
                case GLASS:
                    this.cableBuilder.addConstrainedGlassConnection(facing, cableColor, distance, quadsOut);
                    break;
                case COVERED:
                    this.cableBuilder.addConstrainedCoveredConnection(facing, cableColor, distance, quadsOut);
                    break;
                case SMART:
                    this.cableBuilder.addConstrainedSmartConnection(facing, cableColor, distance, channels, quadsOut);
                    break;
                case DENSE_COVERED:
                case DENSE_SMART:
                    // Dense cables do not render connections to parts since none can be attached
                    break;
                default:
                    break;
            }
        }

        // Render all outgoing connections using the appropriate type
        for (Entry<Direction, AECableType> connection : connectionTypes.entrySet()) {
            final Direction facing = connection.getKey();
            final AECableType connectionType = connection.getValue();
            final boolean cableBusAdjacent = renderState.getCableBusAdjacent().contains(facing);
            final int channels = renderState.getChannelsOnSide().get(facing);

            switch (cableType) {
                case GLASS:
                    this.cableBuilder.addGlassConnection(facing, cableColor, connectionType, cableBusAdjacent,
                            quadsOut);
                    break;
                case COVERED:
                    this.cableBuilder.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent,
                            quadsOut);
                    break;
                case SMART:
                    this.cableBuilder.addSmartConnection(facing, cableColor, connectionType, cableBusAdjacent, channels,
                            quadsOut);
                    break;
                case DENSE_COVERED:
                    this.cableBuilder.addDenseCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent,
                            quadsOut);
                    break;
                case DENSE_SMART:
                    this.cableBuilder.addDenseSmartConnection(facing, cableColor, connectionType, cableBusAdjacent,
                            channels, quadsOut);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Gets a list of texture sprites appropriate for particles (digging, etc.) given the render state for a cable bus.
     */
    public List<TextureAtlasSprite> getParticleTextures(CableBusRenderState renderState) {
        CableCoreType coreType = CableCoreType.fromCableType(renderState.getCableType());
        AEColor cableColor = renderState.getCableColor();

        List<TextureAtlasSprite> result = new ArrayList<>();

        if (coreType != null) {
            result.add(this.cableBuilder.getCoreTexture(coreType, cableColor));
        }

        // If no core is present, just use the first part that comes into play
        for (Direction side : renderState.getAttachments().keySet()) {
            IPartModel partModel = renderState.getAttachments().get(side);

            for (ResourceLocation model : partModel.getModels()) {
                BakedModel bakedModel = this.partModels.get(model);

                if (bakedModel == null) {
                    throw new IllegalStateException("Trying to use an unregistered part model: " + model);
                }

                TextureAtlasSprite particleTexture = bakedModel.getParticleIcon();

                // If a part sub-model has no particle texture (indicated by it being the
                // missing texture),
                // don't add it, so we don't get ugly missing texture break particles.
                if (!isMissingTexture(particleTexture)) {
                    result.add(particleTexture);
                }
            }
        }

        return result;
    }

    private boolean isMissingTexture(TextureAtlasSprite particleTexture) {
        return particleTexture instanceof MissingTextureAtlasSprite;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false; // This model is never used in an UI
    }

    @Override
    public boolean usesBlockLight() {
        return false; // This model is never used in an UI
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleTexture;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

}
