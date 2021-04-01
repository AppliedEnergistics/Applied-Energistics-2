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
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;

/**
 * This baked model combines the quads of a compass base and the quads of a compass pointer, which will be rotated
 * around the Y-axis to get the compass to point in the right direction.
 */
public class SkyCompassBakedModel implements IBakedModel, FabricBakedModel {

    // Rotation is expressed as radians

    private final IBakedModel base;

    private final IBakedModel pointer;

    private float fallbackRotation = 0;

    public SkyCompassBakedModel(IBakedModel base, IBakedModel pointer) {
        this.base = base;
        this.pointer = pointer;
    }

    public IBakedModel getBase() {
        return base;
    }

    public IBakedModel getPointer() {
        return pointer;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(IBlockDisplayReader blockView, BlockState state, BlockPos pos,
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
                pos.transform(quaternion);
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
    public boolean isAmbientOcclusion() {
        return this.base.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.base.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.base.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        /*
         * This handles setting the rotation of the compass when being held in hand. If it's not held in hand, it'll
         * animate using the spinning animation.
         */
        return new ItemOverrideList(null, null, null, Collections.emptyList()) {
            @Override
            public IBakedModel getOverrideModel(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world,
                    @Nullable LivingEntity entity) {
                // FIXME: This check prevents compasses being held by OTHERS from getting the
                // rotation, BUT do we actually still need this???
                if (world != null && entity instanceof ClientPlayerEntity) {
                    PlayerEntity player = (PlayerEntity) entity;

                    float offRads = (float) (player.rotationYaw / 180.0f * (float) Math.PI + Math.PI);

                    SkyCompassBakedModel.this.fallbackRotation = offRads
                            + getAnimatedRotation(player.getPosition(), true);
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
