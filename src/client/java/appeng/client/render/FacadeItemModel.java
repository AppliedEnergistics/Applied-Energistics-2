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

package appeng.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.implementations.items.IFacadeItem;
import appeng.client.render.cablebus.FacadeBuilder;
import appeng.core.AppEng;
import appeng.items.parts.FacadeItem;
import appeng.util.Platform;

/**
 * The model class for facades. Since facades wrap existing models, they don't declare any dependencies here other than
 * the cable anchor.
 */
public class FacadeItemModel implements ItemModel {

    // We use this to get the default item transforms and make our lives easier
    private static final Identifier MODEL_BASE = AppEng.makeId("item/facade_base");

    private final ItemBaseModelWrapper baseModel;
    private final ItemModel missingItemModel;
    private final FacadeBuilder facadeBuilder;

    public FacadeItemModel(ItemBaseModelWrapper baseModel, ModelBaker modelBaker, ItemModel missingItemModel) {
        this.baseModel = baseModel;
        this.missingItemModel = missingItemModel;
        this.facadeBuilder = modelBaker.compute(FacadeBuilder.SHARED_KEY);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver,
            ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {

        if (!(stack.getItem() instanceof FacadeItem itemFacade)) {
            missingItemModel.update(renderState, stack, itemModelResolver, displayContext, level, owner, seed);
            return;
        }

        var facadeBlockState = itemFacade.getTextureBlockState(stack);
        if (facadeBlockState.isEmpty()) {
            missingItemModel.update(renderState, stack, itemModelResolver, displayContext, level, owner, seed);
            return;
        }

        renderState.appendModelIdentityElement(this);
        renderState.appendModelIdentityElement(facadeBlockState);

        var facadeLayer = renderState.newLayer();
        baseModel.renderProperties().applyToLayer(facadeLayer, displayContext);
        facadeLayer.setupSpecialModel(new FacadeSpecialRender(), facadeBlockState);
        // We use the extents of the stem, which is certainly not quite correct.
        facadeLayer.setExtents(baseModel.extents());
    }

    public class FacadeSpecialRender implements SpecialModelRenderer<BlockState> {
        @Override
        public void submit(@Nullable BlockState blockState,
                           PoseStack poseStack,
                           SubmitNodeCollector submitNodeCollector,
                           int lightCoords,
                           int overlayCoords,
                           boolean hasFoil,
                           final int outlineColor) {
            if (blockState == null) {
                return;
            }

            // This is the actual layer showing the facade itself
            var blockModelParts = new ArrayList<BlockStateModelPart>();
            facadeBuilder.collectFacadePartsInternal(
                    false,
                    direction -> direction == Direction.NORTH ? blockState : null,
                    BlockAndTintGetter.EMPTY,
                    List.of(),
                    Set.of(),
                    BlockPos.ZERO,
                    blockModelParts::add);

            var qi = new QuadInstance();
            qi.setOverlayCoords(overlayCoords);
            qi.setLightCoords(lightCoords);
            for (var blockModelPart : blockModelParts) {
                // TODO 26.1: Probably incorrect
                var renderType = (blockModelPart.materialFlags() & BakedQuad.FLAG_TRANSLUCENT) != 0
                        ? Sheets.translucentBlockItemSheet() : Sheets.cutoutItemSheet();

                submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
                    for (var cullFace : Platform.CULL_FACES) {
                        for (var quad : blockModelPart.getQuads(cullFace)) {
                            var shade = (quad.materialInfo().shade() && quad.direction() != null) ? getShade(quad.direction()) : 1f;
                            qi.setColor(-1);
                            qi.scaleColor(shade);
                            consumer.putBakedQuad(
                                    pose, quad, qi);
                        }
                    }
                });
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
            for (var point : baseModel.extents().get()) {
                extents.accept(point);
            }
        }

        private float getShade(Direction side) {
            return switch (side) {
                case DOWN -> 0.5F;
                case NORTH, SOUTH -> 0.8F;
                case WEST, EAST -> 0.6F;
                default -> 1.0F;
            };
        }

        @Override
        public @Nullable BlockState extractArgument(ItemStack itemStack) {
            if (itemStack.getItem() instanceof IFacadeItem facadeItem) {
                return facadeItem.getTextureBlockState(itemStack);
            } else {
                return null;
            }
        }
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final Identifier ID = AppEng.makeId("facade");

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(MODEL_BASE);
            FacadeBuilder.resolveDependencies(resolver);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {

            return new FacadeItemModel(
                    ItemBaseModelWrapper.bake(context.blockModelBaker(), MODEL_BASE, transform),
                    context.blockModelBaker(),
                    context.missingItemModel());
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
