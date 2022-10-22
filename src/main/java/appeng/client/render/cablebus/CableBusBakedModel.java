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
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.inventories.IDynamicPartBakedModel;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.parts.reporting.ReportingModelData;

@Environment(EnvType.CLIENT)
public class CableBusBakedModel implements BakedModel, FabricBakedModel {

    private static final Mesh EMPTY_MESH = consumer -> {
    };

    private static final Renderer RENDERER = RendererAccess.INSTANCE.getRenderer();

    // The number of meshes overall that will be cached
    private static final int CACHE_MESH_COUNT = 100;

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

    private final LoadingCache<CableBusRenderState, Mesh> cableModelCache;

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
                .maximumSize(CACHE_MESH_COUNT)//
                .build(new CacheLoader<CableBusRenderState, Mesh>() {
                    @Override
                    public Mesh load(CableBusRenderState renderState) {
                        Mesh mesh = buildCableModel(renderState);
                        return mesh != null ? mesh : EMPTY_MESH;
                    }
                });
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // This model will only ever be used for blocks
    }

    private CableBusRenderState getRenderState(BlockAndTintGetter blockView, BlockPos pos) {

        RenderAttachedBlockView renderAttachedBlockView = (RenderAttachedBlockView) blockView;
        Object renderAttachment = renderAttachedBlockView.getBlockEntityRenderAttachment(pos);
        if (renderAttachment instanceof CableBusRenderState) {
            return (CableBusRenderState) renderAttachment;
        }
        return null;

    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos,
            Supplier<RandomSource> randomSupplier, RenderContext context) {

        CableBusRenderState renderState = getRenderState(blockView, pos);

        if (renderState == null) {
            return;
        }

        // First, handle the cable at the center of the cable bus
        final Mesh cableModel = cableModelCache.getUnchecked(renderState);
        if (cableModel != EMPTY_MESH) {
            context.meshConsumer().accept(cableModel);
        }

        // Then handle attachments
        for (Direction facing : Direction.values()) {
            final IPartModel partModel = renderState.getAttachments().get(facing);
            if (partModel == null) {
                continue;
            }

            Object partModelData = renderState.getPartModelData().get(facing);

            for (ResourceLocation model : partModel.getModels()) {
                BakedModel bakedModel = this.partModels.get(model);

                if (bakedModel == null) {
                    throw new IllegalStateException("Trying to use an unregistered part model: " + model);
                }

                Direction spinDirection = getPartSpin(facing, partModelData);

                context.pushTransform(QuadRotator.get(facing, spinDirection));
                if (bakedModel instanceof IDynamicPartBakedModel dynamicPartBakedModel) {
                    dynamicPartBakedModel.emitQuads(blockView, state, pos, randomSupplier, context,
                            facing, partModelData);
                } else if (bakedModel instanceof FabricBakedModel) {
                    ((FabricBakedModel) bakedModel).emitBlockQuads(blockView, state, pos, randomSupplier, context);
                } else {
                    context.fallbackConsumer().accept(bakedModel);
                }
                context.popTransform();
            }
        }

        Mesh mesh = this.facadeBuilder.getFacadeMesh(renderState, randomSupplier, blockView, context);
        context.meshConsumer().accept(mesh);
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

    private static Direction getPartSpin(Direction facing, Object partModelData) {
        if (partModelData instanceof ReportingModelData) {
            byte spin = ((ReportingModelData) partModelData).getSpin();
            return SPIN_TO_DIRECTION[facing.ordinal() * 4 + spin];
        }

        return Direction.UP;
    }

    private Mesh buildCableModel(CableBusRenderState renderState) {
        AECableType cableType = renderState.getCableType();
        if (cableType == AECableType.NONE) {
            return null;
        }

        AEColor cableColor = renderState.getCableColor();
        EnumMap<Direction, AECableType> connectionTypes = renderState.getConnectionTypes();

        MeshBuilder builder = RENDERER.meshBuilder();
        QuadEmitter emitter = builder.getEmitter();

        // If the connection is straight, no busses are attached, and no covered core
        // has been forced (in case of glass
        // cables), then render the cable as a simplified straight line.
        boolean noAttachments = !renderState.getAttachments().values().stream()
                .anyMatch(IPartModel::requireCableConnection);
        if (noAttachments && isStraightLine(cableType, connectionTypes)) {
            Direction facing = connectionTypes.keySet().iterator().next();

            switch (cableType) {
                case GLASS:
                    this.cableBuilder.addStraightGlassConnection(facing, cableColor, emitter);
                    break;
                case COVERED:
                    this.cableBuilder.addStraightCoveredConnection(facing, cableColor, emitter);
                    break;
                case SMART:
                    this.cableBuilder.addStraightSmartConnection(facing, cableColor,
                            renderState.getChannelsOnSide().get(facing), emitter);
                    break;
                case DENSE_COVERED:
                    this.cableBuilder.addStraightDenseCoveredConnection(facing, cableColor, emitter);
                    break;
                case DENSE_SMART:
                    this.cableBuilder.addStraightDenseSmartConnection(facing, cableColor,
                            renderState.getChannelsOnSide().get(facing), emitter);
                    break;
                default:
                    break;
            }

            return builder.build(); // Don't render the other form of connection
        }

        this.cableBuilder.addCableCore(renderState.getCoreType(), cableColor, emitter);

        // Render all internal connections to attachments
        EnumMap<Direction, Integer> attachmentConnections = renderState.getAttachmentConnections();
        for (Direction facing : attachmentConnections.keySet()) {
            int distance = attachmentConnections.get(facing);
            int channels = renderState.getChannelsOnSide().get(facing);

            switch (cableType) {
                case GLASS:
                    this.cableBuilder.addConstrainedGlassConnection(facing, cableColor, distance, emitter);
                    break;
                case COVERED:
                    this.cableBuilder.addConstrainedCoveredConnection(facing, cableColor, distance, emitter);
                    break;
                case SMART:
                    this.cableBuilder.addConstrainedSmartConnection(facing, cableColor, distance, channels, emitter);
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
                    this.cableBuilder.addGlassConnection(facing, cableColor, connectionType, cableBusAdjacent, emitter);
                    break;
                case COVERED:
                    this.cableBuilder.addCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent,
                            emitter);
                    break;
                case SMART:
                    this.cableBuilder.addSmartConnection(facing, cableColor, connectionType, cableBusAdjacent, channels,
                            emitter);
                    break;
                case DENSE_COVERED:
                    this.cableBuilder.addDenseCoveredConnection(facing, cableColor, connectionType, cableBusAdjacent,
                            emitter);
                    break;
                case DENSE_SMART:
                    this.cableBuilder.addDenseSmartConnection(facing, cableColor, connectionType, cableBusAdjacent,
                            channels, emitter);
                    break;
                default:
                    break;
            }
        }

        return builder.build();
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
        return particleTexture.atlasLocation().equals(MissingTextureAtlasSprite.getLocation());
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
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
        return Collections.emptyList();
    }

}
