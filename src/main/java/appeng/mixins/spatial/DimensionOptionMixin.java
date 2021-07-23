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

package appeng.mixins.spatial;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.Dimension;

import appeng.spatial.SpatialStorageDimensionIds;

/**
 * This Mixin tries to hide the screen that warns users about experimental features being used, but only if AE2's
 * dimension is the only added dimension.
 */
@Mixin(Dimension.class)
public class DimensionOptionMixin {

    /**
     * This injects right after the method makes a local copy of all present dimensions, and modifies the list by
     * removing our mod-provided dimension from it.
     * <p>
     * This means Vanilla will perform it's check whether it should display an experimental warning without considering
     * our dimension.
     */
    @ModifyVariable(method = "stable", at = @At(value = "INVOKE_ASSIGN", ordinal = 0, target = "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;", remap = false), allow = 1)
    private static List<Map.Entry<RegistryKey<Dimension>, Dimension>> overrideExperimentalCheck(
            List<Map.Entry<RegistryKey<Dimension>, Dimension>> dimensions) {
        // this only removes our dimension from the check
        dimensions.removeIf(e -> e.getKey() == SpatialStorageDimensionIds.DIMENSION_ID);
        return dimensions;
    }

}
