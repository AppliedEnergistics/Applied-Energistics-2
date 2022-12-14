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

import java.util.Map;

import com.mojang.datafixers.util.Pair;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import appeng.hooks.UnlitQuadHooks;

/**
 * The only job of this mixin is to only enable the unlit extensions if the model is whitelisted for it, which is
 * decided in {@link UnlitQuadHooks}.
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(method = "method_45898", at = @At("HEAD"), allow = 1, remap = false)
    private static void onBeginLoadModel(Map.Entry<ResourceLocation, Resource> entry,
            CallbackInfoReturnable<Pair<?, ?>> cri) {
        UnlitQuadHooks.beginDeserializingModel(entry.getKey());
    }

    @Inject(method = "method_45898", at = @At("RETURN"), remap = false)
    private static void onEndLoadModel(Map.Entry<ResourceLocation, Resource> entry,
            CallbackInfoReturnable<Pair<?, ?>> cri) {
        UnlitQuadHooks.endDeserializingModel();
    }

}
