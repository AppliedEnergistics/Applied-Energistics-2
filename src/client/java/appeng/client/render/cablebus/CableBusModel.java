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

import appeng.api.parts.IPartModel;
import appeng.api.parts.PartModelsInternal;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.networking.CableBusBlock;
import appeng.block.networking.CableBusRenderState;
import appeng.block.networking.CableCoreType;
import appeng.blockentity.AEModelData;
import appeng.core.AppEng;
import appeng.thirdparty.fabric.MeshBuilderImpl;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * The built-in model for the cable bus block.
 */
public class CableBusModel implements DynamicBlockStateModel {

    private static final Logger LOG = LoggerFactory.getLogger(CableBusModel.class);

    // The number of quads overall that will be cached
    private static final int CACHE_QUAD_COUNT = 5000;

    /**
     * Lookup table to match the spin of a part with an up direction.
     * <p>
     * DUNSWE for the facing index, 4 spin values per facing.
     */
    private static final Direction[] SPIN_TO_DIRECTION = new Direction[]{
            Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST, // DOWN
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, // UP
            Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST, // NORTH
            Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST, // SOUTH
            Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH, // WEST
            Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH // EAST
    };

    // TODO: now that we're storing the level anyway, might as well query it
    private static final ModelProperty<FacadeModelData> FACADE_DATA = new ModelProperty<>();

    private record FacadeModelData(EnumMap<Direction, ModelData> facadeData, BlockAndTintGetter level) {
    }

    private final LoadingCache<CableBusRenderState, SimpleModelWrapper> cableModelCache;

    private final CableBuilder cableBuilder;

    private final FacadeBuilder facadeBuilder;

    private final Map<ResourceLocation, SimpleModelWrapper> partModels;

    private final TextureAtlasSprite particleTexture;

    private final SimpleModelWrapper emptyCableModel;

    CableBusModel(CableBuilder cableBuilder, FacadeBuilder facadeBuilder,
                  Map<ResourceLocation, SimpleModelWrapper> partModels, TextureAtlasSprite particleTexture) {
        this.cableBuilder = cableBuilder;
        this.facadeBuilder = facadeBuilder;
        this.partModels = partModels;
        this.particleTexture = particleTexture;
        this.emptyCableModel = new SimpleModelWrapper(QuadCollection.EMPTY, false, particleTexture, null);
        this.cableModelCache = CacheBuilder.newBuilder()//
                .maximumWeight(CACHE_QUAD_COUNT)//
                .weigher((Weigher<CableBusRenderState, SimpleModelWrapper>) (key, value) -> value.quads().getAll().size())//
                .build(new CacheLoader<>() {
                    @Override
                    public SimpleModelWrapper load(CableBusRenderState renderState) {
                        return createCableModel(renderState);
                    }
                });
    }

    private ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
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
                var blockState = entry.getValue().sourceBlock();
                var model = dispatcher.getBlockModel(blockState);
                // TODO facadeModelData.put(side, model.getModelData(level, pos, blockState, data));
            } finally {
                CableBusBlock.RENDERING_FACADE_DIRECTION.remove();
            }
        }
        return data.derive().with(FACADE_DATA, new FacadeModelData(facadeModelData, level)).build();
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        var data = level.getModelData(pos);

        var renderState = data.get(CableBusRenderState.PROPERTY);
        if (renderState == null) {
            return;
        }

        // The core parts of the cable will only be rendered in the CUTOUT layer.
        // Facades will add themselves to what ever the block would be rendered with,
        // except when transparent facades are enabled, they are forced to TRANSPARENT.

        // First, handle the cable at the center of the cable bus
        var cableModel = cableModelCache.getUnchecked(renderState);
        if (cableModel != emptyCableModel) {
            parts.add(cableModel);
        }

        var attachedPartQuads = new QuadCollection.Builder();

        var meshBuilder = new MeshBuilderImpl();
        var emitter = meshBuilder.getEmitter();

        // Then handle attachments
        for (var facing : Direction.values()) {
            var partModel = renderState.getAttachments().get(facing);
            if (partModel == null) {
                continue;
            }

            ModelData partModelData = renderState.getPartModelData().get(facing);
            if (partModelData == null) {
                partModelData = ModelData.EMPTY;
            }

            for (var model : partModel.getModels()) {
                var bakedModel = this.partModels.get(model);

                if (bakedModel == null) {
                    throw new IllegalStateException("Trying to use an unregistered part model: " + model);
                }

                List<BakedQuad> partQuads = bakedModel.getQuads(null); // TODO: This is all bogus since it doesn't account for model data
                // bakedModel.getQuads(state, null, rand, partModelData, renderType);

                var spin = getPartSpin(partModelData);

                // Rotate quads accordingly
                var rotator = QuadRotator.get(facing, spin);

                for (var partQuad : partQuads) {
                    emitter.fromVanilla(partQuad, null);
                    rotator.transform(emitter);
                    attachedPartQuads.addUnculledFace(emitter.toBakedQuad(partQuad.sprite()));
                }
            }
        }

        // TODO Obviously bogus
        var quads = attachedPartQuads.build();
        if (!quads.getAll().isEmpty()) {
            parts.add(new SimpleModelWrapper(quads, true, particleTexture, null));
        }

        FacadeModelData facadeData = data.get(FACADE_DATA);
        if (facadeData != null) {
            this.facadeBuilder.collectFacadeParts(renderState, () -> random, facadeData.level, facadeData.facadeData, parts::add);
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return particleTexture;
    }

    @Override
    public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        // TODO: Determine dynamic particle icon based on attached parts
        return DynamicBlockStateModel.super.particleIcon(level, pos, state);
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

    private static int getPartSpin(ModelData partModelData) {
        var spin = partModelData.get(AEModelData.SPIN);
        if (spin != null) {
            return spin;
        }

        return 0;
    }

    private SimpleModelWrapper createCableModel(CableBusRenderState renderState) {
        var quads = new QuadCollection.Builder();
        getCableQuads(renderState, quads::addUnculledFace);

        var particleTexture = getCableParticleTexture(renderState);
        if (particleTexture == null) {
            particleTexture = this.particleTexture;
        }

        var quadCollection = quads.build();
        if (quadCollection.getAll().isEmpty()) {
            return emptyCableModel;
        }
        return new SimpleModelWrapper(quadCollection, true, particleTexture, RenderType.cutout());
    }

    private void getCableQuads(CableBusRenderState renderState, Consumer<BakedQuad> quadsOut) {
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

    @Nullable
    private TextureAtlasSprite getCableParticleTexture(CableBusRenderState renderState) {
        CableCoreType coreType = CableCoreType.fromCableType(renderState.getCableType());
        AEColor cableColor = renderState.getCableColor();
        if (coreType != null) {
            return this.cableBuilder.getCoreTexture(coreType, cableColor);
        }
        return null;
    }

    /**
     * Gets a list of texture sprites appropriate for particles (digging, etc.) given the render state for a cable bus.
     */
    public List<TextureAtlasSprite> getParticleTextures(CableBusRenderState renderState) {
        List<TextureAtlasSprite> result = new ArrayList<>();

        var cableParticleTexture = getCableParticleTexture(renderState);
        if (cableParticleTexture != null) {
            result.add(cableParticleTexture);
        }

        // If no core is present, just use the first part that comes into play
        for (Direction side : renderState.getAttachments().keySet()) {
            IPartModel partModel = renderState.getAttachments().get(side);

            for (ResourceLocation modelId : partModel.getModels()) {
                var model = this.partModels.get(modelId);

                if (model == null) {
                    throw new IllegalStateException("Trying to use an unregistered part model: " + modelId);
                }

                var particleTexture = model.particleIcon();

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
        return particleTexture.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<CableBusModel.Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            var spriteGetter = baker.sprites();

            var partModels = this.loadPartModels(baker);

            var cableBuilder = new CableBuilder(spriteGetter);

            FacadeBuilder facadeBuilder = baker.compute(FacadeBuilder.SHARED_KEY);

            // This should normally not be used, but we *have* to provide a particle texture
            // or otherwise damage models will crash
            var particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

            return new CableBusModel(cableBuilder, facadeBuilder, partModels, particleTexture);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            PartModelsInternal.freeze();
            PartModelsInternal.getModels().forEach(resolver::markDependency);
            FacadeBuilder.resolveDependencies(resolver);
        }

        @Override
        public MapCodec<CableBusModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        private Map<ResourceLocation, SimpleModelWrapper> loadPartModels(ModelBaker baker) {
            ImmutableMap.Builder<ResourceLocation, SimpleModelWrapper> result = ImmutableMap.builder();

            for (var location : PartModelsInternal.getModels()) {
                // TODO: This might now be validated independently in vanilla
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    var slots = baker.getModel(location).getTopTextureSlots();
                    if (slots.getMaterial("particle") == null) {
                        LOG.error("Part model {} is missing a 'particle' texture", location);
                    }
                }

                result.put(location, SimpleModelWrapper.bake(baker, location, BlockModelRotation.X0_Y0));
            }

            return result.build();
        }
    }
}
