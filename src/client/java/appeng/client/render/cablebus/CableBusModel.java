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
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
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

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.block.networking.CableBusRenderState;
import appeng.block.networking.CableCoreType;
import appeng.blockentity.AEModelData;
import appeng.client.AppEngClient;
import appeng.client.api.model.parts.PartModel;
import appeng.client.model.FacingModelState;
import appeng.core.AppEng;

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
    private static final Direction[] SPIN_TO_DIRECTION = new Direction[] {
            Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST, // DOWN
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, // UP
            Direction.UP, Direction.WEST, Direction.DOWN, Direction.EAST, // NORTH
            Direction.UP, Direction.EAST, Direction.DOWN, Direction.WEST, // SOUTH
            Direction.UP, Direction.SOUTH, Direction.DOWN, Direction.NORTH, // WEST
            Direction.UP, Direction.NORTH, Direction.DOWN, Direction.SOUTH // EAST
    };

    private final LoadingCache<CableBusRenderState, SimpleModelWrapper> cableModelCache;

    private final CableBuilder cableBuilder;

    private final FacadeBuilder facadeBuilder;

    private final Map<IPartItem<?>, PartModel[]> partModels;

    private final TextureAtlasSprite particleTexture;

    private final SimpleModelWrapper emptyCableModel;

    private CableBusModel(CableBuilder cableBuilder, FacadeBuilder facadeBuilder,
            Map<IPartItem<?>, PartModel[]> partModels, TextureAtlasSprite particleTexture) {
        this.cableBuilder = cableBuilder;
        this.facadeBuilder = facadeBuilder;
        this.particleTexture = particleTexture;
        this.partModels = partModels;
        this.emptyCableModel = new SimpleModelWrapper(QuadCollection.EMPTY, false, particleTexture, null);
        this.cableModelCache = CacheBuilder.newBuilder()//
                .maximumWeight(CACHE_QUAD_COUNT)//
                .weigher((Weigher<CableBusRenderState, SimpleModelWrapper>) (key, value) -> value.quads().getAll()
                        .size())//
                .build(new CacheLoader<>() {
                    @Override
                    public SimpleModelWrapper load(CableBusRenderState renderState) {
                        return createCableModel(renderState);
                    }
                });
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
            List<BlockModelPart> parts) {
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

        // Then handle attachments
        for (var side : IPart.ATTACHMENT_POINTS) {
            var attachedPart = renderState.getAttachments().get(side);
            if (attachedPart == null) {
                continue;
            }

            var modelsBySide = partModels.get(attachedPart.partItem());
            if (modelsBySide == null) {
                // TODO: Show a missing block model?
                continue;
            }

            modelsBySide[side.ordinal()].collectParts(
                    level,
                    pos,
                    attachedPart.modelData(),
                    random,
                    parts);
        }

        this.facadeBuilder.collectFacadeParts(
                renderState,
                level,
                parts::add);
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
        boolean noAttachments = false; /*
                                        * TODO !renderState.getAttachments().values().stream()
                                        * .anyMatch(IPartModel::requireCableConnection);
                                        */
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
        for (var entry : renderState.getAttachments().entrySet()) {
            var side = entry.getKey();
            var partState = entry.getValue();
            var modelsBySide = partModels.get(partState.partItem());
            if (modelsBySide == null) {
                continue; // TODO How to handle this?
            }

            var model = modelsBySide[side.ordinal()];
            // TODO model.particleTexture();
        }

        return result;
    }

    private boolean isMissingTexture(TextureAtlasSprite particleTexture) {
        return particleTexture.contents().name().equals(MissingTextureAtlasSprite.getLocation());
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final ResourceLocation ID = AppEng.makeId("cable_bus");
        public static final MapCodec<CableBusModel.Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            var spriteGetter = baker.sprites();

            var cableBuilder = new CableBuilder(spriteGetter);

            FacadeBuilder facadeBuilder = baker.compute(FacadeBuilder.SHARED_KEY);

            // This should normally not be used, but we *have* to provide a particle texture
            // or otherwise damage models will crash
            var particleTexture = cableBuilder.getCoreTexture(CableCoreType.GLASS, AEColor.TRANSPARENT);

            var unbakedPartModels = AppEngClient.instance().getPartModels().getUnbaked();
            var bakedPartModels = new IdentityHashMap<IPartItem<?>, PartModel[]>(unbakedPartModels.size());
            var attachmentPoints = IPart.ATTACHMENT_POINTS;
            for (var entry : unbakedPartModels.entrySet()) {
                var unbaked = entry.getValue();
                var partModelsBySide = new PartModel[attachmentPoints.size()];

                // Bake for all 6 attachment points
                for (int i = 0; i < attachmentPoints.size(); i++) {
                    var side = attachmentPoints.get(i);
                    partModelsBySide[i] = unbaked.bake(baker, FacingModelState.fromFacing(side));
                }

                bakedPartModels.put(entry.getKey(), partModelsBySide);
            }

            return new CableBusModel(cableBuilder, facadeBuilder, bakedPartModels, particleTexture);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            AppEngClient.instance().getPartModels().resolveDependencies(resolver);

            FacadeBuilder.resolveDependencies(resolver);
        }

        @Override
        public MapCodec<CableBusModel.Unbaked> codec() {
            return MAP_CODEC;
        }
    }
}
