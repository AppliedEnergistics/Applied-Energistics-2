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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.world.BlockRenderView;

import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;

/**
 * This baked model combines the quads of a compass base and the quads of a compass pointer, which will be rotated
 * around the Y-axis to get the compass to point in the right direction.
 */
public class SkyCompassBakedModel implements BakedModel, FabricBakedModel {

    // Rotation is expressed as radians

    private final BakedModel base;

    private final BakedModel pointer;

    private float fallbackRotation = 0;

    public SkyCompassBakedModel(BakedModel base, BakedModel pointer) {
        this.base = base;
        this.pointer = pointer;
    }

    public BakedModel getBase() {
        return base;
    }

    public BakedModel getPointer() {
        return pointer;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {
        // Pre-compute the quad count to avoid list resizes
        context.fallbackConsumer().accept(this.base);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        context.fallbackConsumer().accept(base);

        // This is used to render a compass pointing in a specific direction when being
        // held in hand
        // Set up the rotation around the Y-axis for the pointer
        context.pushTransform(quad -> {
            Quaternion quaternion = new Quaternion(0, this.fallbackRotation, 0, false);
            Vector3f pos = new Vector3f();
            for (int i = 0; i < 4; i++) {
                quad.copyPos(i, pos);
                pos.add(-0.5f, -0.5f, -0.5f);
                pos.rotate(quaternion);
                pos.add(0.5f, 0.5f, 0.5f);
                quad.pos(i, pos);
            }
            return true;
        });
        context.fallbackConsumer().accept(this.pointer);
        context.popTransform();
    }

    // this is used in the block entity renderer
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return base.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.base.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return this.base.getSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return this.base.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        /*
         * This handles setting the rotation of the compass when being held in hand. If it's not held in hand, it'll
         * animate using the spinning animation.
         */
        return new ModelOverrideList(null, null, null, Collections.emptyList()) {
            @Override
            public BakedModel apply(BakedModel originalModel, ItemStack stack, @Nullable ClientWorld world,
                    @Nullable LivingEntity entity) {
                // FIXME: This check prevents compasses being held by OTHERS from getting the
                // rotation, BUT do we actually still need this???
                if (world != null && entity instanceof ClientPlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entity;

                    float offRads = (float) (player.yaw / 180.0f * (float) Math.PI + Math.PI);

                    SkyCompassBakedModel.this.fallbackRotation = offRads
                            + getAnimatedRotation(player.getBlockPos(), true);
                } else {
                    SkyCompassBakedModel.this.fallbackRotation = getAnimatedRotation(null, false);
                }

                return originalModel;
            }
        };
    }

    /**
     * Gets the effective, animated rotation for the compass given the current position of the compass.
     */
    public static float getAnimatedRotation(@Nullable BlockPos pos, boolean prefetch) {

        // Only query for a meteor position if we know our own position
        if (pos != null) {
            CompassResult cr = CompassManager.INSTANCE.getCompassDirection(0, pos.getX(), pos.getY(), pos.getZ());

            // Prefetch meteor positions from the server for adjacent blocks so they are
            // available more quickly when
            // we're moving
            if (prefetch) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        CompassManager.INSTANCE.getCompassDirection(0, pos.getX() + i - 1, pos.getY(),
                                pos.getZ() + j - 1);
                    }
                }
            }

            if (cr.isValidResult()) {
                if (cr.isSpin()) {
                    long timeMillis = System.currentTimeMillis();
                    // .5 seconds per full rotation
                    timeMillis %= 500;
                    return timeMillis / 500.f * (float) Math.PI * 2;
                } else {
                    return (float) cr.getRad();
                }
            }
        }

        long timeMillis = System.currentTimeMillis();
        // 3 seconds per full rotation
        timeMillis %= 3000;
        return timeMillis / 3000.f * (float) Math.PI * 2;
    }
}
