/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.spatial;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.client.render.SpatialSkyRender;

/**
 * Defines properties for how the sky in the spatial storage level is rendered.
 */
@OnlyIn(Dist.CLIENT)
public class SpatialStorageSkyProperties {

    // See the fabric version of this to get any idea what its doing
    public static final DimensionSpecialEffects INSTANCE = new DimensionSpecialEffects(Float.NaN /* disables clouds */,
            false,
            SkyType.NONE /* we use a custom render mixin */, true, false) {

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
            return Vec3.ZERO;
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false;
        }

        @Nullable
        @Override
        public float[] getSunriseColor(float timeOfDay, float partialTicks) {
            return null;
        }

        @Override
        public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix,
                Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
            SpatialSkyRender.getInstance().render(modelViewMatrix, projectionMatrix);
            return true;
        }

        @Override
        public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX,
                double camY, double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
            return true; // Disables clouds
        }
    };

}
