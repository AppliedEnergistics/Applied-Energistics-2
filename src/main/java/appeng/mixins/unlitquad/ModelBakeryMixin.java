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

package appeng.mixins.unlitquad;

import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;

import appeng.hooks.UnlitQuadHooks;

/**
 * The only job of this mixin is to only enable the unlit extensions if the model is whitelisted for it, which is
 * decided in {@link UnlitQuadHooks}.
 */
@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(method = "loadModel", at = @At("HEAD"), allow = 1)
    protected void onBeginLoadModel(ResourceLocation location, CallbackInfo cri)
            throws IOException {
        UnlitQuadHooks.beginDeserializingModel(location);
    }

    @Inject(method = "loadModel", at = @At("RETURN"))
    protected void onEndLoadModel(ResourceLocation location, CallbackInfo cri) {
        UnlitQuadHooks.endDeserializingModel();
    }

}
