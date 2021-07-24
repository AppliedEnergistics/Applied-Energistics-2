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
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import appeng.hooks.CompassManager;
import appeng.hooks.CompassResult;

/**
 * This baked model combines the quads of a compass base and the quads of a compass pointer, which will be rotated
 * around the Y-axis to get the compass to point in the right direction.
 */
public class SkyCompassBakedModel implements IDynamicBakedModel {
    // Rotation is expressed as radians
    public static final ModelProperty<Float> ROTATION = new ModelProperty<>();

    private final BakedModel base;

    private final BakedModel pointer;

    private float fallbackRotation = 0;

    public SkyCompassBakedModel(BakedModel base, BakedModel pointer) {
        this.base = base;
        this.pointer = pointer;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand,
                                    IModelData extraData) {
        float rotation = 0;
        // Get rotation from the special block state
        Float rotationFromData = extraData.getData(ROTATION);
        if (rotationFromData != null) {
            rotation = rotationFromData;
        } else {
            // This is used to render a compass pointing in a specific direction when being
            // held in hand
            rotation = this.fallbackRotation;
        }

        // Pre-compute the quad count to avoid list resizes
        List<BakedQuad> quads = new ArrayList<>(this.base.getQuads(state, side, rand, extraData));
        // We'll add the pointer as "sideless"
        if (side == null) {
            // Set up the rotation around the Y-axis for the pointer
            Matrix4f matrix = new Matrix4f();
            matrix.setIdentity();
            matrix.multiply(new Quaternion(0, rotation, 0, false));

            MatrixVertexTransformer transformer = new MatrixVertexTransformer(matrix);
            for (BakedQuad bakedQuad : this.pointer.getQuads(state, side, rand, extraData)) {
                BakedQuadBuilder builder = new BakedQuadBuilder();

                transformer.setParent(builder);
                transformer.setVertexFormat(builder.getVertexFormat());
                bakedQuad.pipe(transformer);
                // FIXME: This entire code is no longer truly valid...
                // FIXME builder.setQuadOrientation( null ); // After rotation, facing a
                // specific side cannot be guaranteed
                // anymore
                BakedQuad q = builder.build();
                quads.add(q);
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
    public boolean isCustomRenderer() {
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

    @Override
    public ItemOverrides getOverrides() {
        /*
         * This handles setting the rotation of the compass when being held in hand. If it's not held in hand, it'll
         * animate using the spinning animation.
         */
        return new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world,
                                      @Nullable LivingEntity entity, int seed) {
                // FIXME: This check prevents compasses being held by OTHERS from getting the
                // rotation, BUT do we actually still need this???
                if (world != null && entity instanceof LocalPlayer) {
                    Player player = (Player) entity;

                    float offRads = (float) (player.getYRot() / 180.0f * (float) Math.PI + Math.PI);

                    SkyCompassBakedModel.this.fallbackRotation = offRads
                            + getAnimatedRotation(player.blockPosition(), true);
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
