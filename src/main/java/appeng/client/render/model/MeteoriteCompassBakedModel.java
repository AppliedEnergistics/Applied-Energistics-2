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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.resources.model.DelegateBakedModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import appeng.hooks.CompassManager;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;

/**
 * This baked model combines the quads of a compass base and the quads of a compass pointer, which will be rotated
 * around the Y-axis to get the compass to point in the right direction.
 */
public class MeteoriteCompassBakedModel implements IDynamicBakedModel {
    // Rotation is expressed as radians
    public static final ModelProperty<Float> ROTATION = new ModelProperty<>();

    private final BakedModel base;

    private final BakedModel pointer;

    public MeteoriteCompassBakedModel(BakedModel base, BakedModel pointer) {
        this.base = base;
        this.pointer = pointer;
    }

    public BakedModel getPointer() {
        return pointer;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, RenderType renderType) {
        // Get rotation from the special block state
        var rotation = Objects.requireNonNullElse(extraData.get(ROTATION), 0.0f);

        // This is used to render a compass pointing in a specific direction when being
        // held in hand
        // Set up the rotation around the Y-axis for the pointer
        RenderContext.QuadTransform transform = quad -> {
            Quaternionf quaternion = new Quaternionf().rotationY(rotation);
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
        List<BakedQuad> quads = new ArrayList<>(this.base.getQuads(state, side, rand, extraData, renderType));
        // We'll add the pointer as "sideless" to the item rendering when state is null
        if (side == null && state == null) {
            var quadView = MutableQuadView.getInstance();
            for (BakedQuad bakedQuad : this.pointer.getQuads(state, side, rand, extraData, renderType)) {
                quadView.fromVanilla(bakedQuad, null);
                transform.transform(quadView);
                quads.add(quadView.toBlockBakedQuad());
            }
        }

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.base.useAmbientOcclusion();
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
        return this.base.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.base.getTransforms();
    }

    /*
     * The entity is given when an item rendered on the hotbar, or when held in hand.
     */
// TODO 1.21.4    @Override
// TODO 1.21.4    public List<BakedModel> getRenderPasses(ItemStack itemStack) {
// TODO 1.21.4        float rotation;
// TODO 1.21.4        if (level != null && entity != null) {
// TODO 1.21.4            rotation = getAnimatedRotation(entity.position(), true, 0);
// TODO 1.21.4        } else {
// TODO 1.21.4            rotation = getAnimatedRotation(null, false, 0);
// TODO 1.21.4        }
// TODO 1.21.4
// TODO 1.21.4        return new FixedRotationModel(rotation);
// TODO 1.21.4    }

    // Model wrapper that allows us to pass a rotation along to the baked model
    class FixedRotationModel extends DelegateBakedModel {
        private final float rotation;

        public FixedRotationModel(float rotation) {
            super(MeteoriteCompassBakedModel.this);
            this.rotation = rotation;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            var modelData = ModelData.builder().with(ROTATION, rotation).build();
            return parent.getQuads(state, side, rand, modelData, null);
        }

        @Override
        public void applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack,
                boolean applyLeftHandTransform) {
            super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);

            // In the pointer model, it is pointing towards z+
            // Apply the camera and item transform to determine where in world coordinates it is now pointing
            var pointerNormal = poseStack.last().transformNormal(0, 0, 1, new Vector3f());
            pointerNormal.y = 0; // Project onto x/z plane
            pointerNormal.normalize();

            // The angle around Y that the pointer is rotated just due to the current camera transform
            var d = Mth.atan2(pointerNormal.z, pointerNormal.x) - Mth.atan2(1, 0);

            // GUI obviously will not include the players view rotation
            if (cameraTransformType == ItemDisplayContext.GUI) {
                if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null) {
                    var player = Minecraft.getInstance().player;
                    float offRads = (float) (player.getYRot() / 180.0f * (float) Math.PI + Math.PI);
                    d += offRads;
                }
            }

            // TODO 1.21.4 return new FixedRotationModel((float) d + rotation);
        }
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
}
