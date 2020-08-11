package appeng.mixins.unlitquad;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import appeng.hooks.UnlitQuadHooks;

/**
 * This mixin hooks into conversion from {@link ModelElementFace} to
 * {@link BakedQuad} to apply our unlit extensions if the block part face is an
 * instance of our marker class
 * {@link appeng.hooks.UnlitQuadHooks.UnlitModelElementFace}.
 */
@Mixin(JsonUnbakedModel.class)
public class JsonUnbakedModelMixin {

    @Inject(method = "createQuad", at = @At("RETURN"), cancellable = true, require = 1, allow = 1)
    private static void onBakeFace(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side,
            ModelBakeSettings settings, Identifier id, CallbackInfoReturnable<BakedQuad> cri) {
        if (elementFace instanceof UnlitQuadHooks.UnlitModelElementFace) {
            cri.setReturnValue(UnlitQuadHooks.makeUnlit(cri.getReturnValue()));
        }
    }

}
