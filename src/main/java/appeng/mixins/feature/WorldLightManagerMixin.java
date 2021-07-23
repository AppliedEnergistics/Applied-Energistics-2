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

package appeng.mixins.feature;

import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;

/**
 * This fixes a bug in the Minecraft light-update code that runs after world-generation for
 * {@link ProtoChunk}. If a chunk-section contains a light-emitting block, and we clear the
 * entire chunk-section (i.e. as part of meteorite worldgen), the lighting-update will assume that the chunk section
 * exists when it runs through {@link LevelLightEngine#onBlockEmissionIncrease(net.minecraft.core.BlockPos, int)},
 * even though the light-level is now 0 for the block.
 * <p/>
 * This mixin will cancel the now useless block-update and prevent the crash from occurring.
 * <p/>
 * See: https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/4891
 */
@Mixin(LevelLightEngine.class)
public class WorldLightManagerMixin {
    @Inject(method = "onBlockEmissionIncrease", at = @At("HEAD"), cancellable = true)
    public void onBlockEmissionIncrease(net.minecraft.core.BlockPos blockPos, int lightLevel, CallbackInfo ci) {
        if (lightLevel == 0) {
            ci.cancel();
        }
    }
}
