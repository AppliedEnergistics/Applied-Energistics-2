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

import javax.annotation.Nullable;

import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Defines properties for how the sky in the spatial storage world is rendered.
 */
@OnlyIn(Dist.CLIENT)
public class SpatialStorageSkyProperties {

    // See the fabric version of this to get any idea what its doing
    public static final DimensionRenderInfo INSTANCE = new DimensionRenderInfo(Float.NaN /* disables clouds */, false,
            DimensionRenderInfo.FogType.NONE /* we use a custom render mixin */, true, false) {

        @Override
        public Vector3d func_230494_a_(Vector3d p_230494_1_, float p_230494_2_) {
            return Vector3d.ZERO;
        }

        @Override
        public boolean func_230493_a_(int p_230493_1_, int p_230493_2_) {
            return false;
        }

        @Nullable
        @Override
        public float[] func_230492_a_(float p_230492_1_, float p_230492_2_) {
            return null;
        }
    };

}
