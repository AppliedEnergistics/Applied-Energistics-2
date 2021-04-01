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

import appeng.hooks.UnlitQuadHooks;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.BlockModel;
import net.minecraft.client.renderer.model.BlockPart;
import net.minecraft.client.renderer.model.BlockPartFace;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin hooks into conversion from {@link BlockPartFace} to {@link BakedQuad} to apply our unlit extensions if the
 * block part face is an instance of our marker class {@link UnlitQuadHooks.UnlitBlockPartFace}.
 */
@Mixin(BlockModel.class)
public class BlockModelMixin {

    @Inject(method = "bakeFace", at = @At("RETURN"), cancellable = true, require = 1, allow = 1)
    private static void onBakeFace(BlockPart partIn, BlockPartFace partFaceIn, TextureAtlasSprite spriteIn,
            Direction directionIn, IModelTransform transformIn, ResourceLocation locationIn,
            CallbackInfoReturnable<BakedQuad> cri) {
        if (partFaceIn instanceof UnlitQuadHooks.UnlitBlockPartFace) {
            cri.setReturnValue(UnlitQuadHooks.makeUnlit(cri.getReturnValue()));
        }
    }

}
