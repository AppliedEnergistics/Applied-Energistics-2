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

import com.mojang.blaze3d.matrix.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;

import appeng.client.render.SpatialSkyRender;
import appeng.spatial.SpatialStorageDimensionIds;

@Mixin(value = WorldRenderer.class)
public class SkyRenderMixin {

    @Shadow
    private Minecraft mc;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At("HEAD"), cancellable = true)
    public void renderSky(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (mc.level.dimension() == SpatialStorageDimensionIds.WORLD_ID) {
            SpatialSkyRender.getInstance().render(matrices);
            ci.cancel();
        }
    }

}
