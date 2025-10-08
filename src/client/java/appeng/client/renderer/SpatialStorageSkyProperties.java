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

package appeng.client.renderer;

import org.joml.Matrix4f;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.DimensionSpecialEffects.SkyType;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * Defines properties for how the sky in the spatial storage level is rendered.
 */
public class SpatialStorageSkyProperties {

    // See the fabric version of this to get any idea what its doing
    public static final DimensionSpecialEffects INSTANCE = new DimensionSpecialEffects(
            SkyType.NONE /* we use a custom render mixin */, false, false) {

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
            return Vec3.ZERO;
        }

        @Override
        public boolean isFoggyAt(int x, int y) {
            return false;
        }

        @Override
        public boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState,
                Matrix4f modelViewMatrix, Runnable setupFog) {
            SpatialSkyRender.getInstance().render(modelViewMatrix);
            return true;
        }

        @Override
        public boolean renderClouds(LevelRenderState levelRenderState, Vec3 camPos, CloudStatus cloudStatus,
                int cloudColor, float cloudHeight, Matrix4f modelViewMatrix) {
            return true; // Disable clouds
        }
    };

}
