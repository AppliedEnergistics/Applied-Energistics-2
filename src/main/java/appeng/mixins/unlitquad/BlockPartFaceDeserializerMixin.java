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

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import net.minecraft.client.renderer.block.model.BlockElementFace.Deserializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.block.model.BlockElementFace;

import appeng.hooks.UnlitQuadHooks;

/**
 * This mixin will call the hook to deserialize the unlit property, but only if we are currently deserializing an AE2
 * model.
 */
@Mixin(Deserializer.class)
public class BlockPartFaceDeserializerMixin {

    @Inject(method = "deserialize", at = @At("RETURN"), cancellable = true, allow = 1, remap = false)
    public void onDeserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext,
            CallbackInfoReturnable<BlockElementFace> cri) {
        if (!UnlitQuadHooks.isUnlitExtensionEnabled()) {
            return; // Not in a model that activated the deserializer
        }

        BlockElementFace modelElement = cri.getReturnValue();
        cri.setReturnValue(UnlitQuadHooks.enhanceModelElementFace(modelElement, jsonElement));
    }

}
