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
package appeng.mixins;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

import appeng.hooks.ItemRendererHooks;

/**
 * This mixin specifically targets rendering of items in the user interface to allow us to customize _only_ the UI
 * representation of an item, and none of the others (held items, in-world, etc.)
 */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "renderGuiItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"), cancellable = true)
    protected void renderGuiItem(PoseStack poseStack, ItemStack stack, int x, int y, BakedModel model,
            CallbackInfo ci) {
        if (ItemRendererHooks.onRenderGuiItemModel((ItemRenderer) (Object) this, poseStack, stack, x, y)) {
            ci.cancel();
        }
    }

}
