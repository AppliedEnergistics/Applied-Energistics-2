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

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.QuadTransformers;

import appeng.client.render.ItemBaseModelWrapper;
import appeng.core.AppEng;
import appeng.hooks.CompassManager;

/**
 * This baked model combines the quads of a compass base and the quads of a compass pointer, which will be rotated
 * around the Y-axis to get the compass to point in the right direction.
 */
public class MeteoriteCompassModel implements ItemModel {

    private static final ResourceLocation MODEL_POINTER = ResourceLocation.parse(
            "ae2:item/meteorite_compass_pointer");

    private final ItemBaseModelWrapper pointer;

    private final RotatedPointerRenderer rotatedPointerRenderer;

    public MeteoriteCompassModel(ItemBaseModelWrapper pointer) {
        this.pointer = pointer;
        this.rotatedPointerRenderer = new RotatedPointerRenderer(pointer);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver,
            ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        Float target = null;
        if (level != null && entity != null) {
            target = getAnimatedRotation(entity.position(), entity.getViewVector(0f));
        }

        var pointerLayer = renderState.newLayer();
        pointerLayer.setTransform(pointer.renderProperties().transforms().getTransform(displayContext));
        pointerLayer.setupSpecialModel(rotatedPointerRenderer, target);

    }

    /**
     * Gets the effective, animated rotation for the compass given the current position of the compass.
     */
    public static Float getAnimatedRotation(Vec3 pos, Vec3 viewVector) {

        // Only query for a meteor position if we know our own position
        var ourChunkPos = new ChunkPos(BlockPos.containing(pos));
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
        public void render(@Nullable Float target,
                ItemDisplayContext displayContext,
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                int packedLight,
                int packedOverlay,
                boolean hasFoilType) {
            if (target == null) {
                target = getSlowSpinningRotation();
            }

            // PI/4 is 45° to correct for the pointer pointing to the "left" (positive Z in the model)
            // and not "up"
            var quaternion = new Quaternionf().rotationY((float) (target - Math.PI / 4));
            var transformation = new Matrix4f();
            transformation.rotateAround(quaternion, 0.5f, 0.5f, 0.5f);

            var transformer = QuadTransformers.applying(new Transformation(transformation));

            // Pre-compute the quad count to avoid list resizes
            // We'll add the pointer as "sideless" to the item rendering when state is null
            var buffer = bufferSource
                    .getBuffer(Objects.requireNonNullElse(pointer.renderType(), Sheets.translucentItemSheet()));
            var pose = poseStack.last();
            for (var bakedQuad : this.pointer.quads()) {
                bakedQuad = transformer.process(bakedQuad);
                buffer.putBulkData(pose, bakedQuad, 1f, 1f, 1f, 1f, packedLight, packedOverlay);
            }
        }

        @Override
        public Float extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("meteorite_compass");
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public ItemModel bake(BakingContext context) {
            var pointerModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), MODEL_POINTER);
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
