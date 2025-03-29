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

import appeng.client.render.ItemBaseModelWrapper;
import appeng.core.AppEng;
import appeng.hooks.CompassManager;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * This baked model combines the quads of a compass base and the quads of a compass pointer, which will be rotated
 * around the Y-axis to get the compass to point in the right direction.
 */
public class MeteoriteCompassModel implements ItemModel {

    private static final ResourceLocation MODEL_BASE = ResourceLocation.parse(
            "ae2:item/meteorite_compass_base");

    private static final ResourceLocation MODEL_POINTER = ResourceLocation.parse(
            "ae2:item/meteorite_compass_pointer");

    // Rotation is expressed as radians
    public static final ModelProperty<Float> ROTATION = new ModelProperty<>();

    private final ItemBaseModelWrapper base;

    private final ItemBaseModelWrapper pointer;

    private final RotatedPointerRenderer rotatedPointerRenderer;

    public MeteoriteCompassModel(ItemBaseModelWrapper base, ItemBaseModelWrapper pointer) {
        this.base = base;
        this.pointer = pointer;
        this.rotatedPointerRenderer = new RotatedPointerRenderer(pointer);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {

        base.applyToLayer(renderState.newLayer(), displayContext);

        float rotation;
        if (level != null && entity != null) {
            rotation = getAnimatedRotation(entity.position(), true, 0);
        } else {
            rotation = getAnimatedRotation(null, false, 0);
        }

        renderState.newLayer().setupSpecialModel(rotatedPointerRenderer, rotation);

    }

    /**
     * Gets the effective, animated rotation for the compass given the current position of the compass.
     */
    public static float getAnimatedRotation(@Nullable Vec3 pos, boolean prefetch, float playerRotation) {

        // Only query for a meteor position if we know our own position
        if (pos != null) {
            var ourChunkPos = new ChunkPos(BlockPos.containing(pos));
            var closestMeteorite = CompassManager.INSTANCE.getClosestMeteorite(ourChunkPos, prefetch);

            // No close meteorite was found -> spin slowly
            if (closestMeteorite == null) {
                long timeMillis = System.currentTimeMillis();
                // .5 seconds per full rotation
                timeMillis %= 500;
                return timeMillis / 500.f * (float) Math.PI * 2;
            } else {
                var dx = pos.x - closestMeteorite.getX();
                var dz = pos.z - closestMeteorite.getZ();
                var distanceSq = dx * dx + dz * dz;
                if (distanceSq > 6 * 6) {
                    var x = closestMeteorite.getX();
                    var z = closestMeteorite.getZ();
                    return (float) rad(pos.x(), pos.z(), x, z) + playerRotation;
                }
            }
        }

        long timeMillis = System.currentTimeMillis();
        // 3 seconds per full rotation
        timeMillis %= 3000;
        return timeMillis / 3000.f * (float) Math.PI * 2;
    }

    private static double rad(double ax, double az, double bx, double bz) {
        var up = bz - az;
        var side = bx - ax;

        return Math.atan2(-up, side) - Math.PI / 2.0;
    }

    private record RotatedPointerRenderer(ItemBaseModelWrapper pointer) implements SpecialModelRenderer<Float> {
        @Override
        public void render(@Nullable Float rotation, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {

            // In the pointer model, it is pointing towards z+
            // Apply the camera and item transform to determine where in world coordinates it is now pointing
            var pointerNormal = poseStack.last().transformNormal(0, 0, 1, new Vector3f());
            pointerNormal.y = 0; // Project onto x/z plane
            pointerNormal.normalize();

            // The angle around Y that the pointer is rotated just due to the current camera transform
            var d = Mth.atan2(pointerNormal.z, pointerNormal.x) - Mth.atan2(1, 0);

            // GUI obviously will not include the players view rotation
            if (displayContext == ItemDisplayContext.GUI) {
                if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
                    var player = Minecraft.getInstance().player;
                    float offRads = (float) (player.getYRot() / 180.0f * (float) Math.PI + Math.PI);
                    d += offRads;
                }
            }

            float effectiveRotation = Objects.requireNonNullElse(rotation, 0.0f) + (float) d;

            // This is used to render a compass pointing in a specific direction when being
            // held in hand
            // Set up the rotation around the Y-axis for the pointer
            RenderContext.QuadTransform transform = quad -> {
                Quaternionf quaternion = new Quaternionf().rotationY(effectiveRotation);
                Vector3f pos = new Vector3f();
                for (int i = 0; i < 4; i++) {
                    quad.copyPos(i, pos);
                    pos.add(-0.5f, -0.5f, -0.5f);
                    pos.rotate(quaternion);
                    pos.add(0.5f, 0.5f, 0.5f);
                    quad.pos(i, pos);
                }
                return true;
            };

            // Pre-compute the quad count to avoid list resizes
            // We'll add the pointer as "sideless" to the item rendering when state is null
            var buffer = bufferSource.getBuffer(RenderType.solid());

            var pose = poseStack.last();
            var quadView = MutableQuadView.getInstance();
            for (BakedQuad bakedQuad : this.pointer.quads()) {
                quadView.fromVanilla(bakedQuad, null);
                transform.transform(quadView);

                buffer.putBulkData(pose, quadView.toBlockBakedQuad(), 1f, 1f, 1f, 1f, packedLight, packedOverlay);
            }
        }

        @Override
        public Float extractArgument(ItemStack stack) {
            return 0f;
        }
    }

    public record Unbaked() implements ItemModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("meteorite_compass");
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public ItemModel bake(BakingContext context) {
            var baseModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), MODEL_BASE);
            var pointerModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), MODEL_POINTER);
            return new MeteoriteCompassModel(baseModel, pointerModel);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
