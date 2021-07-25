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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;

import appeng.client.render.SpatialSkyRender;
import appeng.spatial.SpatialStorageDimensionIds;

@Mixin(value = LevelRenderer.class)
public class SkyRenderMixin {

    @Shadow
    private Minecraft minecraft;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    public void renderSky(PoseStack matrices, Matrix4f matrix, float f, Runnable runnable, CallbackInfo ci) {
        if (minecraft.level.dimension() == SpatialStorageDimensionIds.WORLD_ID) {
            SpatialSkyRender.getInstance().render(matrices);
            ci.cancel();
        }
    }

}
