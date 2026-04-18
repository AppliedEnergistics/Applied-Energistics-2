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

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3fc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.quad.QuadTransforms;

import appeng.client.render.ItemBaseModelWrapper;
import appeng.core.AppEng;
import appeng.hooks.CompassManager;

/**
 * This baked model combines the quads of a compass base and the quads of a compass pointer, which will be rotated
 * around the Y-axis to get the compass to point in the right direction.
 */
public class MeteoriteCompassModel implements ItemModel {

    private static final Identifier MODEL_POINTER = AppEng.makeId("item/meteorite_compass_pointer");

    private final ItemBaseModelWrapper pointer;

    private final RotatedPointerRenderer rotatedPointerRenderer;

    public MeteoriteCompassModel(ItemBaseModelWrapper pointer) {
        this.pointer = pointer;
        this.rotatedPointerRenderer = new RotatedPointerRenderer(pointer);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver,
            ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        Float target = null;
        if (level != null && owner != null) {
            float ownerRotation = owner.getVisualRotationYInDegrees();
            var lookVector = Vec3.Z_AXIS.yRot(-Mth.DEG_TO_RAD * ownerRotation);
            target = getAnimatedRotation(owner.position(), lookVector);
        }

        var pointerLayer = renderState.newLayer();
        pointerLayer.setLocalTransform(pointer.transformation());
        pointer.renderProperties().applyToLayer(pointerLayer, displayContext);
        pointerLayer.setupSpecialModel(rotatedPointerRenderer, target);
        renderState.setAnimated();
        renderState.appendModelIdentityElement(this);
    }

    /**
     * Gets the effective, animated rotation for the compass given the current position of the compass.
     */
    public static Float getAnimatedRotation(Vec3 pos, Vec3 viewVector) {

        // Only query for a meteor position if we know our own position
        var ourChunkPos = ChunkPos.containing(BlockPos.containing(pos));
        var closestMeteorite = CompassManager.INSTANCE.getClosestMeteorite(ourChunkPos, true);

        // No close meteorite was found -> spin slowly
        if (closestMeteorite != null) {
            var dx = pos.x - closestMeteorite.getX();
            var dz = pos.z - closestMeteorite.getZ();
            var distanceSq = dx * dx + dz * dz;
            if (distanceSq <= 6 * 6) {
                return getFastSpinningRotation(); // We're on it
            }

            // Calculate the angle on the 2D plane based on the entities look direction
            var lookVector = new Vector2f((float) viewVector.x, (float) viewVector.z);
            var dirVector = new Vector2f(
                    (float) (closestMeteorite.getX() - pos.x),
                    (float) (closestMeteorite.getZ() - pos.z));
            return dirVector.angle(lookVector);
        } else {
            return getSlowSpinningRotation();
        }
    }

    private static float getSlowSpinningRotation() {
        long timeMillis = System.currentTimeMillis();
        // 3 seconds per full rotation
        timeMillis %= 3000;
        return timeMillis / 3000.f * (float) Math.PI * 2;
    }

    private static float getFastSpinningRotation() {
        long timeMillis = System.currentTimeMillis();
        // .5 seconds per full rotation
        timeMillis %= 500;
        return timeMillis / 500.f * (float) Math.PI * 2;
    }

    private record RotatedPointerRenderer(ItemBaseModelWrapper pointer) implements SpecialModelRenderer<Float> {
        @Override
        public void submit(@Nullable Float target,
                PoseStack poseStack,
                SubmitNodeCollector nodes,
                int packedLight,
                int packedOverlay,
                boolean hasFoilType,
                int seed) {
            if (target == null) {
                target = getSlowSpinningRotation();
            }

            // PI/4 is 45° to correct for the pointer pointing to the "left" (positive Z in the model)
            // and not "up"
            var quaternion = new Quaternionf().rotationY((float) (target - Math.PI / 4));
            var transformation = new Matrix4f();
            transformation.rotateAround(quaternion, 0.5f, 0.5f, 0.5f);

            UnaryOperator<BakedQuad> transformer = q -> QuadTransforms.applyTransformation(q,
                    new Transformation(transformation));

            var qi = new QuadInstance();
            qi.setLightCoords(packedLight);
            qi.setOverlayCoords(packedOverlay);
            var renderType = Sheets.translucentItemSheet();
            nodes.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
                for (var bakedQuad : this.pointer.quads()) {
                    bakedQuad = transformer.apply(bakedQuad);
                    buffer.putBakedQuad(pose, bakedQuad, qi);
                }
            });
        }

        @Override
        public void getExtents(Consumer<Vector3fc> extents) {
        }

        @Override
        public Float extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final Identifier ID = AppEng.makeId("meteorite_compass");
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transform) {
            var pointerModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), MODEL_POINTER, transform);
            return new MeteoriteCompassModel(pointerModel);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(MODEL_POINTER);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
